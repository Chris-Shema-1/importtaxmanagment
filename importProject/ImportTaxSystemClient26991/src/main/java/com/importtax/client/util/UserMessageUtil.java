package com.importtax.client.util;

/**
 * Converts RMI/exception chains into short messages safe for JOptionPane.
 */
public final class UserMessageUtil {

    private UserMessageUtil() {
    }

    public static String friendly(Throwable error, String fallback) {
        if (error == null) {
            return fallback;
        }
        Throwable root = unwrap(error);
        if (root instanceof IllegalArgumentException illegal) {
            String message = illegal.getMessage();
            if (message != null && !message.isBlank()) {
                return message.trim();
            }
        }
        String message = root.getMessage();
        if (message != null && isUserSafe(message)) {
            return message.trim();
        }
        return fallback;
    }

    private static Throwable unwrap(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null && current.getCause() != current) {
            if (current instanceof IllegalArgumentException) {
                return current;
            }
            current = current.getCause();
        }
        return current;
    }

    private static boolean isUserSafe(String message) {
        String lower = message.toLowerCase();
        return !lower.contains("hibernate")
                && !lower.contains("org.")
                && !lower.contains("java.")
                && !lower.contains("sql")
                && !lower.contains("rmi")
                && !message.contains("Exception")
                && message.length() <= 200;
    }
}
