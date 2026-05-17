package com.importtax.server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OtpService extends Remote {

    /**
     * Generate a 6-digit OTP for the given username, publish it via ActiveMQ,
     * and return the OTP so the client can display the verification dialog.
     * In a real system the OTP would only travel via the broker (email/SMS);
     * returning it here lets the demo work without a real mail server.
     */
    String generateOtp(String username) throws RemoteException;

    /** Validate the OTP entered by the user. Returns true if correct and not expired. */
    boolean validateOtp(String username, String otp) throws RemoteException;
}
