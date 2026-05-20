package com.importtax.server.rmi;

import com.importtax.server.model.OtpValidationResult;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OtpService extends Remote {

    /**
     * Generate a 6-digit OTP for the given username (e.g. registration flow).
     * Sends email when an address is provided or the user already exists.
     */
    String generateOtp(String username) throws RemoteException;

    /**
     * Login flow: resolve the user's registered email, generate a new OTP, and send it via SMTP.
     * Invalidates any previous OTP for this username.
     */
    boolean sendLoginOtp(String username) throws RemoteException;

    /**
     * Registration flow: generate OTP and send to the email entered on the registration form.
     */
    boolean sendRegistrationOtp(String username, String email) throws RemoteException;

    /**
     * Validate OTP with expiration and attempt-limit enforcement.
     */
    OtpValidationResult validateOtp(String username, String otp) throws RemoteException;
}
