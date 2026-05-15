package com.importtax.server.rmi.impl;

import com.importtax.server.dao.UserDao;
import com.importtax.server.dao.impl.UserDaoImpl;
import com.importtax.server.model.User;
import com.importtax.server.rmi.UserService;
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
        return execute("login", () -> sanitize(userDao.login(username, password).orElse(null)));
    }

    @Override
    public User registerUser(User user) throws RemoteException {
        return execute("registerUser", () -> {
            validateRegistration(user);

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
            newUser.setPassword(user.getPassword());
            newUser.setRole(user.getRole());
            newUser.setCreatedAt(LocalDate.now());

            return sanitize(userDao.save(newUser));
        });
    }

    @Override
    public User authenticateUser(String username, String password) throws RemoteException {
        return execute("authenticateUser", () -> {
            String normalizedUsername = username == null ? "" : username.trim();
            String rawPassword = password == null ? "" : password;

            if (normalizedUsername.isEmpty() || rawPassword.isEmpty()) {
                throw new IllegalArgumentException("Username and password are required");
            }

            User user = userDao.login(normalizedUsername, rawPassword)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
            return sanitize(user);
        });
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
        if (isBlank(user.getFullName())) {
            throw new IllegalArgumentException("Full Name is required");
        }
        if (isBlank(user.getEmail()) || !user.getEmail().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("A valid email is required");
        }
        if (isBlank(user.getUsername()) || user.getUsername().trim().length() < 4) {
            throw new IllegalArgumentException("Username must be at least 4 characters");
        }
        if (isBlank(user.getPassword()) || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
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
