package com.importtax.client.util.validators;

/**
 * ValidationResult - Represents the result of a validation operation.
 * Includes success status and error message if validation fails.
 *
 * @author Import Tax System Development Team
 * @version 1.0.0
 */
public class ValidationResult {

    private final boolean valid;
    private final String errorMessage;

    /**
     * Creates a successful validation result.
     */
    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    /**
     * Creates a failed validation result with error message.
     *
     * @param errorMessage The validation error message
     */
    public static ValidationResult error(String errorMessage) {
        return new ValidationResult(false, errorMessage);
    }

    /**
     * Constructor.
     *
     * @param valid Whether validation passed
     * @param errorMessage Error message (null if valid)
     */
    private ValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
