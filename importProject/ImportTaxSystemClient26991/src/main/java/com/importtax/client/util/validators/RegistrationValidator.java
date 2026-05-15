package com.importtax.client.util.validators;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RegistrationValidator - Provides comprehensive validation for user registration.
 * Includes validation for empty fields, email format, password requirements,
 * username constraints, and confirmation password matching.
 *
 * @author Import Tax System Development Team
 * @version 1.0.0
 */
public class RegistrationValidator {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationValidator.class);

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private static final int MIN_USERNAME_LENGTH = 4;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private RegistrationValidator() {
        // Utility class - no instantiation
    }

    /**
     * Validates the full registration form.
     *
     * @param fullName User's full name
     * @param email User's email
     * @param username User's username
     * @param password User's password
     * @param confirmPassword Confirmation password
     * @param role Selected user role
     * @return ValidationResult with success status and error message if invalid
     */
    public static ValidationResult validateRegistration(String fullName, String email,
            String username, String password, String confirmPassword, Object role) {

        // Trim all inputs
        fullName = fullName != null ? fullName.trim() : "";
        email = email != null ? email.trim() : "";
        username = username != null ? username.trim() : "";
        password = password != null ? password.trim() : "";
        confirmPassword = confirmPassword != null ? confirmPassword.trim() : "";

        // Validate empty fields
        if (fullName.isEmpty()) {
            logger.warn("Validation failed: Full Name is empty");
            return ValidationResult.error("Full Name is required");
        }

        if (email.isEmpty()) {
            logger.warn("Validation failed: Email is empty");
            return ValidationResult.error("Email is required");
        }

        if (username.isEmpty()) {
            logger.warn("Validation failed: Username is empty");
            return ValidationResult.error("Username is required");
        }

        if (password.isEmpty()) {
            logger.warn("Validation failed: Password is empty");
            return ValidationResult.error("Password is required");
        }

        if (confirmPassword.isEmpty()) {
            logger.warn("Validation failed: Confirm Password is empty");
            return ValidationResult.error("Please confirm your password");
        }

        if (role == null) {
            logger.warn("Validation failed: Role not selected");
            return ValidationResult.error("Please select a role");
        }

        // Validate email format
        if (!isValidEmail(email)) {
            logger.warn("Validation failed: Invalid email format - {}", email);
            return ValidationResult.error("Invalid email format");
        }

        // Validate username length
        if (username.length() < MIN_USERNAME_LENGTH) {
            logger.warn("Validation failed: Username too short - {}", username);
            return ValidationResult.error("Username must be at least " + MIN_USERNAME_LENGTH + " characters");
        }

        // Validate password length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            logger.warn("Validation failed: Password too short");
            return ValidationResult.error("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            logger.warn("Validation failed: Passwords do not match");
            return ValidationResult.error("Passwords do not match");
        }

        logger.debug("Registration validation successful");
        return ValidationResult.success();
    }

    /**
     * Validates email format.
     *
     * @param email Email address to validate
     * @return true if email is valid, false otherwise
     */
    private static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return emailPattern.matcher(email).matches();
    }

    /**
     * Validates username uniqueness (client-side placeholder).
     * Actual check is done on server.
     *
     * @param username Username to check
     * @return ValidationResult
     */
    public static ValidationResult validateUsernameUniqueness(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error("Username cannot be empty");
        }
        // Actual check is performed server-side
        return ValidationResult.success();
    }

    /**
     * Validates email uniqueness (client-side placeholder).
     * Actual check is done on server.
     *
     * @param email Email to check
     * @return ValidationResult
     */
    public static ValidationResult validateEmailUniqueness(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.error("Email cannot be empty");
        }
        // Actual check is performed server-side
        return ValidationResult.success();
    }
}
