package com.importtax.client.util;

import com.importtax.client.model.UserRole;
import com.importtax.server.model.User;

public final class CurrentSession {

    private static User loggedInUser;

    private CurrentSession() {
    }

    public static synchronized void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    public static synchronized User getLoggedInUser() {
        return loggedInUser;
    }

    public static synchronized Long getLoggedInUserId() {
        return loggedInUser != null ? loggedInUser.getUserId() : null;
    }

    public static synchronized String getUsername() {
        return loggedInUser != null ? loggedInUser.getUsername() : null;
    }

    public static synchronized boolean isAdmin() {
        return loggedInUser != null && UserRole.ADMIN.name().equalsIgnoreCase(loggedInUser.getRole());
    }

    public static synchronized boolean isCustomsOfficer() {
        return loggedInUser != null && UserRole.CUSTOMS_OFFICER.name().equalsIgnoreCase(loggedInUser.getRole());
    }

    public static synchronized boolean isFinanceOfficer() {
        return loggedInUser != null && UserRole.FINANCE_OFFICER.name().equalsIgnoreCase(loggedInUser.getRole());
    }

    public static synchronized boolean hasRole(String role) {
        return loggedInUser != null && loggedInUser.getRole() != null && loggedInUser.getRole().equalsIgnoreCase(role);
    }

    public static synchronized boolean hasRole(UserRole role) {
        return role != null && loggedInUser != null && role.name().equalsIgnoreCase(loggedInUser.getRole());
    }

    public static synchronized void clear() {
        loggedInUser = null;
    }
}
