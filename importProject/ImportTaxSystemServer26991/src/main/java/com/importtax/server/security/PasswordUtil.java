package com.importtax.server.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password hashing and verification with plain-text detection for migration.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hashPassword(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password is required");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(String rawPassword, String stored) {
        if (rawPassword == null || stored == null) {
            return false;
        }
        if (isHashed(stored)) {
            try {
                return BCrypt.checkpw(rawPassword, stored);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        return rawPassword.equals(stored);
    }

    public static boolean isHashed(String value) {
        return value != null && value.startsWith("$2");
    }
}
