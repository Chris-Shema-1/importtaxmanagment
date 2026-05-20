package com.importtax.client.rmi;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RmiConnection - Manages RMI connections to the Import Tax System server.
 * Provides utilities for looking up remote services from the RMI registry.
 *
 * @author Import Tax System Development Team
 * @version 1.0.0
 */
public class RmiConnection {

    private static final Logger logger = LoggerFactory.getLogger(RmiConnection.class);

    private static final String RMI_HOST = getSetting("importtax.rmi.host", "IMPORT_TAX_RMI_HOST", "localhost");
    private static final int RMI_PORT = Integer.parseInt(
            getSetting("importtax.rmi.port", "IMPORT_TAX_RMI_PORT", "5000"));
    private static final String RMI_URL = String.format("rmi://%s:%d/", RMI_HOST, RMI_PORT);

    private static Registry registry;

    private RmiConnection() {
        // Private constructor to prevent instantiation
    }

    /**
     * Initializes the RMI connection to the remote registry.
     * Must be called before attempting to look up any remote services.
     *
     * @throws RemoteException if unable to connect to the RMI registry
     */
    public static synchronized void initialize() throws RemoteException {
        try {
            if (registry == null) {
                logger.info("Connecting to RMI registry at: {}", RMI_URL);
                registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
                registry.list(); // Test connection
                logger.info("Successfully connected to RMI registry");
            }
        } catch (RemoteException e) {
            logger.error("Failed to connect to RMI registry at {}", RMI_URL, e);
            registry = null;
            throw e;
        }
    }

    /**
     * Looks up a remote service by name from the RMI registry.
     *
     * @param serviceName The name of the service to look up
     * @return The remote service object
     * @throws RemoteException if a communication error occurs
     * @throws NotBoundException if the service is not bound in the registry
     */
    public static <T extends Remote> T lookup(String serviceName)
            throws RemoteException, NotBoundException {
        if (registry == null) {
            throw new RemoteException("RMI registry not initialized. Call initialize() first.");
        }

        try {
            logger.debug("Looking up service: {}", serviceName);
            @SuppressWarnings("unchecked")
            T service = (T) registry.lookup(serviceName);
            logger.info("Successfully looked up service: {}", serviceName);
            return service;
        } catch (NotBoundException e) {
            logger.error("Service not found in registry: {}", serviceName, e);
            throw e;
        } catch (RemoteException e) {
            logger.error("Remote exception while looking up service: {}", serviceName, e);
            throw e;
        }
    }

    /**
     * Checks if the RMI registry is connected and accessible.
     *
     * @return true if connected, false otherwise
     */
    public static boolean isConnected() {
        if (registry == null) {
            return false;
        }

        try {
            registry.list();
            return true;
        } catch (RemoteException e) {
            logger.error("RMI connection check failed", e);
            return false;
        }
    }

    /**
     * Gets the current RMI registry instance.
     *
     * @return The Registry object, or null if not initialized
     */
    public static Registry getRegistry() {
        return registry;
    }

    /**
     * Gets the RMI URL for reference.
     *
     * @return The RMI connection URL
     */
    public static String getRmiUrl() {
        return RMI_URL;
    }

    /**
     * Closes the RMI connection (if applicable).
     */
    public static synchronized void close() {
        registry = null;
        logger.info("RMI connection closed");
    }

    private static String getSetting(String systemPropertyKey, String environmentKey, String defaultValue) {
        String value = System.getProperty(systemPropertyKey);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentKey);
        }
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
