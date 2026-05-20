package com.importtax.server.constants;

/**
 * Canonical import item lifecycle statuses (uppercase).
 */
public final class ImportItemStatus {

    public static final String PENDING = "PENDING";
    public static final String PAID = "PAID";
    public static final String CLEARED = "CLEARED";
    public static final String HOLD = "HOLD";

    private ImportItemStatus() {
    }

    /** Legacy demo values mapped for transition checks only. */
    public static String canonicalFromDatabase(String raw) {
        if (raw == null || raw.isBlank()) {
            return PENDING;
        }
        String u = raw.trim().toUpperCase();
        if ("APPROVED".equals(u)) {
            return PAID;
        }
        return u;
    }

    public static boolean isKnown(String canonical) {
        return PENDING.equals(canonical)
                || PAID.equals(canonical)
                || CLEARED.equals(canonical)
                || HOLD.equals(canonical);
    }
}
