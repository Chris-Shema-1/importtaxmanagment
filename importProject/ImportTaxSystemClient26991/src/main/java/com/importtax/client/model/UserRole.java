package com.importtax.client.model;

/**
 * UserRole - Enum representing user roles in the Import Tax Management System.
 * Defines the different user roles and their display names.
 *
 * @author Import Tax System Development Team
 * @version 1.0.0
 */
public enum UserRole {
    ADMIN("Administrator"),
    FINANCE_OFFICER("Finance Officer"),
    CUSTOMS_OFFICER("Customs Officer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return name();
    }

    /**
     * Gets UserRole from display name.
     *
     * @param displayName The display name to convert
     * @return The corresponding UserRole
     * @throws IllegalArgumentException if displayName doesn't match any role
     */
    public static UserRole fromDisplayName(String displayName) {
        for (UserRole role : UserRole.values()) {
            if (role.displayName.equals(displayName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + displayName);
    }
}
