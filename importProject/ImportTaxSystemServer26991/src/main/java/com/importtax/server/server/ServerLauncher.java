package com.importtax.server.server;

import com.importtax.server.config.ServerConfig;
import com.importtax.server.rmi.impl.ImportItemServiceImpl;
import com.importtax.server.rmi.impl.InvoiceServiceImpl;
import com.importtax.server.rmi.impl.NotificationServiceImpl;
import com.importtax.server.rmi.impl.PaymentServiceImpl;
import com.importtax.server.rmi.impl.TaxServiceImpl;
import com.importtax.server.rmi.impl.UserServiceImpl;
import com.importtax.server.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

public final class ServerLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerLauncher.class);

    private static final String HOST = "localhost";

    private ServerLauncher() {
    }

    public static void main(String[] args) {
        LOGGER.info("Starting {} RMI server.", ServerConfig.APPLICATION_NAME);

        try {
            startRegistry();
            bindServices();
            registerShutdownHook();

            LOGGER.info("{} RMI server started successfully on port {}.", ServerConfig.APPLICATION_NAME,
                    ServerConfig.RMI_REGISTRY_PORT);
            LOGGER.info("Available services:");
            LOGGER.info("  rmi://{}:{}/userService", HOST, ServerConfig.RMI_REGISTRY_PORT);
            LOGGER.info("  rmi://{}:{}/importItemService", HOST, ServerConfig.RMI_REGISTRY_PORT);
            LOGGER.info("  rmi://{}:{}/taxService", HOST, ServerConfig.RMI_REGISTRY_PORT);
            LOGGER.info("  rmi://{}:{}/invoiceService", HOST, ServerConfig.RMI_REGISTRY_PORT);
            LOGGER.info("  rmi://{}:{}/paymentService", HOST, ServerConfig.RMI_REGISTRY_PORT);
            LOGGER.info("  rmi://{}:{}/notificationService", HOST, ServerConfig.RMI_REGISTRY_PORT);
        } catch (Exception exception) {
            LOGGER.error("Failed to start {} RMI server.", ServerConfig.APPLICATION_NAME, exception);
            HibernateUtil.shutdown();
            System.exit(1);
        }
    }

    private static void startRegistry() throws RemoteException {
        try {
            LocateRegistry.createRegistry(ServerConfig.RMI_REGISTRY_PORT);
            LOGGER.info("RMI registry started on port {}.", ServerConfig.RMI_REGISTRY_PORT);
        } catch (ExportException exception) {
            LocateRegistry.getRegistry(ServerConfig.RMI_REGISTRY_PORT).list();
            LOGGER.info("RMI registry already running on port {}; reusing existing registry.",
                    ServerConfig.RMI_REGISTRY_PORT);
        }
    }

    private static void bindServices() throws Exception {
        bind("userService", new UserServiceImpl());
        bind("importItemService", new ImportItemServiceImpl());
        bind("taxService", new TaxServiceImpl());
        bind("invoiceService", new InvoiceServiceImpl());
        bind("paymentService", new PaymentServiceImpl());
        bind("notificationService", new NotificationServiceImpl());
    }

    private static void bind(String serviceName, Remote service) throws Exception {
        String url = "rmi://" + HOST + ":" + ServerConfig.RMI_REGISTRY_PORT + "/" + serviceName;
        try {
            Naming.bind(url, service);
            LOGGER.info("Bound service: {}", url);
        } catch (AlreadyBoundException exception) {
            Naming.rebind(url, service);
            LOGGER.info("Rebound existing service: {}", url);
        }
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down Hibernate resources.");
            HibernateUtil.shutdown();
        }, "import-tax-rmi-shutdown"));
    }
}
