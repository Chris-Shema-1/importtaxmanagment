package com.importtax.server.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HibernateUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateUtil.class);
    private static final String DB_URL_KEY = "hibernate.connection.url";
    private static final String DB_USER_KEY = "hibernate.connection.username";
    private static final String DB_PASSWORD_KEY = "hibernate.connection.password";
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        if (SESSION_FACTORY != null && !SESSION_FACTORY.isClosed()) {
            SESSION_FACTORY.close();
            LOGGER.info("Hibernate SessionFactory closed.");
        }
    }

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
            applyOverride(configuration, DB_URL_KEY, "IMPORT_TAX_DB_URL", "importtax.db.url");
            applyOverride(configuration, DB_USER_KEY, "IMPORT_TAX_DB_USER", "importtax.db.user");
            applyOverride(configuration, DB_PASSWORD_KEY, "IMPORT_TAX_DB_PASSWORD", "importtax.db.password");
            LOGGER.info("Using database URL: {}", configuration.getProperty(DB_URL_KEY));
            LOGGER.info("Using database user: {}", configuration.getProperty(DB_USER_KEY));
            return configuration.buildSessionFactory();
        } catch (Exception exception) {
            LOGGER.error("Unable to initialize Hibernate SessionFactory.", exception);
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static void applyOverride(Configuration configuration, String hibernateKey,
                                      String environmentKey, String systemPropertyKey) {
        String override = System.getProperty(systemPropertyKey);
        if (override == null || override.isBlank()) {
            override = System.getenv(environmentKey);
        }
        if (override != null && !override.isBlank()) {
            configuration.setProperty(hibernateKey, override);
        }
    }
}
