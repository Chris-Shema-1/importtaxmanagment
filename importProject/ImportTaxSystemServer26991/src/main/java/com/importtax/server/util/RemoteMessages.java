package com.importtax.server.util;

import java.rmi.RemoteException;

/**
 * Maps internal exceptions to client-safe RMI messages.
 */
public final class RemoteMessages {

    private RemoteMessages() {
    }

    public static RemoteException toRemoteException(String operation, Exception exception) {
        if (exception instanceof IllegalArgumentException illegal) {
            String message = illegal.getMessage();
            return new RemoteException(message != null ? message : "Invalid request.");
        }
        return new RemoteException(friendlyFailure(operation));
    }

    public static String friendlyFailure(String operation) {
        if (operation == null || operation.isBlank()) {
            return "Unable to complete the request. Please try again.";
        }
        return "Unable to complete " + operation + ". Please try again.";
    }
}
