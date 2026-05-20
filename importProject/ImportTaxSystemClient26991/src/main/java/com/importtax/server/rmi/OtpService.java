package com.importtax.server.rmi;

import com.importtax.server.model.OtpValidationResult;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OtpService extends Remote {

    String generateOtp(String username) throws RemoteException;

    boolean sendLoginOtp(String username) throws RemoteException;

    boolean sendRegistrationOtp(String username, String email) throws RemoteException;

    OtpValidationResult validateOtp(String username, String otp) throws RemoteException;
}
