package com.importtax.server.model;

import java.io.Serializable;

/**
 * Result of OTP validation for RMI clients (login and registration).
 */
public class OtpValidationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_VALID = "VALID";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_INVALID = "INVALID";
    public static final String STATUS_MAX_ATTEMPTS = "MAX_ATTEMPTS";
    public static final String STATUS_NOT_FOUND = "NOT_FOUND";

    private boolean valid;
    private String status;
    private int remainingAttempts;

    public OtpValidationResult() {
    }

    public OtpValidationResult(boolean valid, String status, int remainingAttempts) {
        this.valid = valid;
        this.status = status;
        this.remainingAttempts = remainingAttempts;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }
}
