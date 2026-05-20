package com.importtax.server.constants;

/**
 * Notification types for business workflow events.
 */
public final class NotificationType {

    public static final String PAYMENT_CONFIRMED = "PAYMENT_CONFIRMED";
    public static final String IMPORT_CLEARED = "IMPORT_CLEARED";
    public static final String OTP = "OTP";

    public static final String STATUS_UNREAD = "UNREAD";

    private NotificationType() {
    }
}
