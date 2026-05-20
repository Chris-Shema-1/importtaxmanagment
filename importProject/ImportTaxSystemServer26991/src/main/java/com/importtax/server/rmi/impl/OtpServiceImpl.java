package com.importtax.server.rmi.impl;

import com.importtax.server.broker.NotificationBroker;
import com.importtax.server.dao.UserDao;
import com.importtax.server.dao.impl.UserDaoImpl;
import com.importtax.server.model.OtpValidationResult;
import com.importtax.server.model.User;
import com.importtax.server.rmi.OtpService;
import com.importtax.server.service.EmailService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class OtpServiceImpl extends UnicastRemoteObject implements OtpService {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OtpServiceImpl.class);
    private static final long OTP_TTL_MS = 5 * 60 * 1000L;
    private static final int MAX_OTP_ATTEMPTS = 3;

    private final SecureRandom random = new SecureRandom();
    private final UserDao userDao;
    private final EmailService emailService;

    private final Map<String, Long> otpExpiry = new ConcurrentHashMap<>();
    private final Map<String, String> otpValues = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> otpAttempts = new ConcurrentHashMap<>();

    public OtpServiceImpl() throws RemoteException {
        this(new UserDaoImpl(), new EmailService());
    }

    OtpServiceImpl(UserDao userDao, EmailService emailService) throws RemoteException {
        super();
        this.userDao = userDao;
        this.emailService = emailService;
    }

    @Override
    public String generateOtp(String username) throws RemoteException {
        if (username == null || username.isBlank()) {
            throw new RemoteException("Username is required to generate OTP");
        }
        String normalized = username.trim();
        storeOtp(normalized);
        LOGGER.info("OTP generated for username={}", normalized);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("OTP code issued for username={} (code omitted from logs)", normalized);
        }

        Optional<User> user = userDao.findByUsername(normalized);
        if (user.isPresent() && user.get().getEmail() != null && !user.get().getEmail().isBlank()) {
            String otp = otpValues.get(normalized);
            sendOtpByEmail(normalized, user.get().getEmail().trim(), otp);
            return otp;
        }
        String otp = otpValues.get(normalized);
        NotificationBroker.publishOtp(normalized, otp);
        LOGGER.info("OTP published to broker for username={} (no registered email yet)", normalized);
        return otp;
    }

    @Override
    public boolean sendLoginOtp(String username) throws RemoteException {
        if (username == null || username.isBlank()) {
            LOGGER.warn("sendLoginOtp rejected: username is blank");
            return false;
        }
        String normalized = username.trim();
        Optional<User> user = userDao.findByUsername(normalized);
        if (user.isEmpty()) {
            LOGGER.warn("sendLoginOtp failed: user not found for username={}", normalized);
            return false;
        }
        String email = user.get().getEmail();
        if (email == null || email.isBlank()) {
            LOGGER.warn("sendLoginOtp failed: no email on file for username={}", normalized);
            return false;
        }

        storeOtp(normalized);
        LOGGER.info("Login OTP generated for username={}", normalized);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Login OTP issued for username={} (code omitted from logs)", normalized);
        }
        return sendOtpByEmail(normalized, email.trim(), otpValues.get(normalized));
    }

    @Override
    public boolean sendRegistrationOtp(String username, String email) throws RemoteException {
        if (username == null || username.isBlank()) {
            LOGGER.warn("sendRegistrationOtp rejected: username is blank");
            return false;
        }
        if (email == null || email.isBlank()) {
            LOGGER.warn("sendRegistrationOtp rejected: email is blank");
            return false;
        }
        String normalized = username.trim();
        storeOtp(normalized);
        LOGGER.info("Registration OTP generated for username={}", normalized);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Registration OTP issued for username={} (code omitted from logs)", normalized);
        }
        return sendOtpByEmail(normalized, email.trim(), otpValues.get(normalized));
    }

    @Override
    public OtpValidationResult validateOtp(String username, String otp) throws RemoteException {
        if (username == null || otp == null) {
            LOGGER.warn("OTP validation failed: username or otp is null");
            return OtpValidationResult.notFound();
        }
        String normalized = username.trim();
        String entered = otp.trim();
        if (entered.isEmpty()) {
            return OtpValidationResult.invalid(remainingAttemptsFor(normalized));
        }

        Long expiry = otpExpiry.get(normalized);
        String stored = otpValues.get(normalized);
        if (expiry == null || stored == null) {
            LOGGER.warn("OTP validation failed: no active OTP for username={}", normalized);
            return OtpValidationResult.notFound();
        }

        if (Instant.now().toEpochMilli() > expiry) {
            clearOtp(normalized);
            LOGGER.warn("OTP expired for username={}", normalized);
            return OtpValidationResult.expired();
        }

        if (stored.equals(entered)) {
            clearOtp(normalized);
            LOGGER.info("OTP validated successfully for username={}", normalized);
            return OtpValidationResult.valid();
        }

        int failures = otpAttempts
                .computeIfAbsent(normalized, k -> new AtomicInteger(0))
                .incrementAndGet();
        LOGGER.warn("Invalid OTP attempt {}/{} for username={}", failures, MAX_OTP_ATTEMPTS, normalized);

        if (failures >= MAX_OTP_ATTEMPTS) {
            clearOtp(normalized);
            LOGGER.warn("OTP invalidated after max attempts for username={}", normalized);
            return OtpValidationResult.maxAttemptsExceeded();
        }

        return OtpValidationResult.invalid(MAX_OTP_ATTEMPTS - failures);
    }

    private int remainingAttemptsFor(String username) {
        AtomicInteger counter = otpAttempts.get(username);
        if (counter == null) {
            return MAX_OTP_ATTEMPTS;
        }
        return Math.max(0, MAX_OTP_ATTEMPTS - counter.get());
    }

    private void storeOtp(String username) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        otpExpiry.put(username, Instant.now().toEpochMilli() + OTP_TTL_MS);
        otpValues.put(username, otp);
        otpAttempts.put(username, new AtomicInteger(0));
    }

    private void clearOtp(String username) {
        otpExpiry.remove(username);
        otpValues.remove(username);
        otpAttempts.remove(username);
    }

    private boolean sendOtpByEmail(String username, String recipientEmail, String otp) {
        try {
            emailService.sendOtpEmail(recipientEmail, otp);
            LOGGER.info("OTP email dispatched for username={}", username);
            return true;
        } catch (MessagingException exception) {
            LOGGER.error("Failed to send OTP email for username={}: {}", username, exception.getMessage());
            clearOtp(username);
            return false;
        }
    }
}
