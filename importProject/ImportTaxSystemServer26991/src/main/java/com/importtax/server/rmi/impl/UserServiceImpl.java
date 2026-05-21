package com.importtax.server.rmi.impl;

import com.importtax.server.dao.UserDao;
import com.importtax.server.dao.impl.UserDaoImpl;
import com.importtax.server.model.User;
import com.importtax.server.rmi.UserService;
import com.importtax.server.security.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.time.LocalDate;

public class UserServiceImpl extends AbstractRemoteCrudService<User> implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;

    public UserServiceImpl() throws RemoteException {
        this(new UserDaoImpl());
    }

    public UserServiceImpl(UserDao userDao) throws RemoteException {
        super(userDao, LOGGER, "UserService");
        this.userDao = userDao;
    }

    @Override
    public User login(String username, String password) throws RemoteException {
        return execute("login", () -> sanitize(authenticateCredentials(username, password)));
    }

    @Override
    public User registerUser(User user) throws RemoteException {
        return execute("registerUser", () -> {
            validateRegistration(user);
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                throw new SecurityException("Public registration of Administrator accounts is not permitted.");
            }

            String fullName = user.getFullName().trim();
            String email = user.getEmail().trim().toLowerCase();
            String username = user.getUsername().trim();

            if (userDao.findByUsername(username).isPresent()) {
                throw new IllegalArgumentException("Username is already taken");
            }
            if (userDao.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Email is already registered");
            }

            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setPassword(PasswordUtil.hashPassword(user.getPassword()));
            newUser.setRole(user.getRole());
            newUser.setCreatedAt(LocalDate.now());

            User saved = userDao.save(newUser);
            LOGGER.info("User registered username={}", username);
            return sanitize(saved);
        });
    }

    @Override
    public User authenticateUser(String username, String password) throws RemoteException {
        return execute("authenticateUser", () -> {
            String normalizedUsername = username == null ? "" : username.trim();
            String rawPassword = password == null ? "" : password;

            if (normalizedUsername.isEmpty() || rawPassword.isEmpty()) {
                LOGGER.warn("Authentication failed: missing credentials");
                throw new IllegalArgumentException("Username and password are required");
            }

            User user = authenticateCredentials(normalizedUsername, rawPassword);
            if (user == null) {
                LOGGER.warn("Authentication failed for usernameOrEmail={}", normalizedUsername);
                throw new IllegalArgumentException("Invalid username or password");
            }
            LOGGER.info("Authentication successful for username={}", user.getUsername());
            return sanitize(user);
        });
    }

    /**
     * Verifies credentials with BCrypt or legacy plain text (auto-upgrades on success).
     */
    private User authenticateCredentials(String usernameOrEmail, String rawPassword) {
        User user = userDao.findByUsernameOrEmail(usernameOrEmail).orElse(null);
        if (user == null) {
            return null;
        }

        String stored = user.getPassword();
        if (!PasswordUtil.verifyPassword(rawPassword, stored)) {
            return null;
        }

        if (!PasswordUtil.isHashed(stored)) {
            user.setPassword(PasswordUtil.hashPassword(rawPassword));
            userDao.update(user);
            LOGGER.info("Password upgraded to BCrypt for username={}", user.getUsername());
        }

        return user;
    }

    @Override
    public boolean isUsernameUnique(String username) throws RemoteException {
        return execute("isUsernameUnique", () -> {
            String normalizedUsername = username == null ? "" : username.trim();
            return !normalizedUsername.isEmpty() && userDao.findByUsername(normalizedUsername).isEmpty();
        });
    }

    @Override
    public boolean isEmailUnique(String email) throws RemoteException {
        return execute("isEmailUnique", () -> {
            String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
            return !normalizedEmail.isEmpty() && userDao.findByEmail(normalizedEmail).isEmpty();
        });
    }

    @Override
    public User getUserById(Long userId) throws RemoteException {
        return execute("getUserById", () -> {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return sanitize(user);
        });
    }

    @Override
    public User findByUsername(String username) throws RemoteException {
        return execute("findByUsername", () -> sanitize(userDao.findByUsername(username).orElse(null)));
    }

    @Override
    public User updateUserSecure(User user, Long callerUserId) throws RemoteException {
        return execute("updateUserSecure", () -> {
            User caller = userDao.findById(callerUserId).orElse(null);
            if (caller == null || !"ADMIN".equalsIgnoreCase(caller.getRole())) {
                throw new SecurityException("Access denied. Administrator privileges required.");
            }
            if (user == null || user.getUserId() == null) {
                throw new IllegalArgumentException("User data and User ID are required");
            }
            User existing = userDao.findById(user.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            existing.setFullName(user.getFullName());
            existing.setEmail(user.getEmail());
            existing.setUsername(user.getUsername());
            existing.setRole(user.getRole());
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                existing.setPassword(PasswordUtil.hashPassword(user.getPassword()));
            }
            return sanitize(userDao.update(existing));
        });
    }

    @Override
    public void deleteUserSecure(Long userIdToDelete, Long callerUserId) throws RemoteException {
        execute("deleteUserSecure", () -> {
            User caller = userDao.findById(callerUserId).orElse(null);
            if (caller == null || !"ADMIN".equalsIgnoreCase(caller.getRole())) {
                throw new SecurityException("Access denied. Administrator privileges required.");
            }
            if (userIdToDelete == null) {
                throw new IllegalArgumentException("User ID to delete is required");
            }
            if (userIdToDelete.equals(callerUserId)) {
                throw new IllegalArgumentException("You cannot delete your own account.");
            }
            User existing = userDao.findById(userIdToDelete)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            userDao.delete(existing);
            return null;
        });
    }

    private User sanitize(User user) {
        if (user != null) {
            user.setPassword(null);
            user.setImportItems(null);
        }
        return user;
    }

    private void validateRegistration(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Registration data is required");
        }
        String fullName = user.getFullName() == null ? "" : user.getFullName().trim();
        if (fullName.isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (fullName.length() < 2) {
            throw new IllegalArgumentException("Full name must be at least 2 characters");
        }

        String email = user.getEmail() == null ? "" : user.getEmail().trim();
        if (email.isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email format is invalid");
        }

        String username = user.getUsername() == null ? "" : user.getUsername().trim();
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (username.length() < 4) {
            throw new IllegalArgumentException("Username must be at least 4 characters");
        }
        if (username.contains(" ")) {
            throw new IllegalArgumentException("Username cannot contain spaces");
        }

        if (isBlank(user.getPassword()) || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (isBlank(user.getRole())) {
            throw new IllegalArgumentException("Role is required");
        }
        if (!isAllowedRole(user.getRole())) {
            throw new IllegalArgumentException("Please select a valid role");
        }
    }

    private boolean isAllowedRole(String role) {
        return "ADMIN".equals(role)
                || "FINANCE_OFFICER".equals(role)
                || "CUSTOMS_OFFICER".equals(role);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
