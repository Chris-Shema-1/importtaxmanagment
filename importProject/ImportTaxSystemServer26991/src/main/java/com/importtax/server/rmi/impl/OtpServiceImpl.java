package com.importtax.server.rmi.impl;

import com.importtax.server.broker.NotificationBroker;
import com.importtax.server.rmi.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OtpServiceImpl extends UnicastRemoteObject implements OtpService {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OtpServiceImpl.class);
    private static final long OTP_TTL_MS = 5 * 60 * 1000L; // 5 minutes

    private final SecureRandom random = new SecureRandom();

    // username -> [otp, expiryEpochMs]
    private final Map<String, long[]> otpStore = new ConcurrentHashMap<>();
    private final Map<String, String> otpValues = new ConcurrentHashMap<>();

    public OtpServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String generateOtp(String username) throws RemoteException {
        if (username == null || username.isBlank()) {
            throw new RemoteException("Username is required to generate OTP");
        }
        String otp = String.format("%06d", random.nextInt(1_000_000));
        long expiry = Instant.now().toEpochMilli() + OTP_TTL_MS;
        otpStore.put(username, new long[]{expiry});
        otpValues.put(username, otp);

        // Publish via ActiveMQ — consumer logs the simulated email
        NotificationBroker.publishOtp(username, otp);
        LOGGER.info("OTP generated for username={}", username);
        return otp;
    }

    @Override
    public boolean validateOtp(String username, String otp) throws RemoteException {
        if (username == null || otp == null) return false;
        long[] entry = otpStore.get(username);
        String stored = otpValues.get(username);
        if (entry == null || stored == null) return false;
        if (Instant.now().toEpochMilli() > entry[0]) {
            otpStore.remove(username);
            otpValues.remove(username);
            LOGGER.warn("OTP expired for username={}", username);
            return false;
        }
        boolean valid = stored.equals(otp.trim());
        if (valid) {
            otpStore.remove(username);
            otpValues.remove(username);
            LOGGER.info("OTP validated successfully for username={}", username);
        } else {
            LOGGER.warn("Invalid OTP attempt for username={}", username);
        }
        return valid;
    }
}
