package com.importtax.client.util;

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

    public static synchronized void clear() {
        loggedInUser = null;
    }
}
