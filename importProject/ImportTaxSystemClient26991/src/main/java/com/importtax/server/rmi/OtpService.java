package com.importtax.server.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OtpService extends Remote {

    String generateOtp(String username) throws RemoteException;

    boolean validateOtp(String username, String otp) throws RemoteException;
}
