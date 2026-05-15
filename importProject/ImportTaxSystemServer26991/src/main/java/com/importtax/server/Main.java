package com.importtax.server;

import com.importtax.server.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Starting Import Tax System Server connection test.");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.createNativeQuery("SELECT 1", Integer.class).getSingleResult();
            LOGGER.info("Hibernate database connection tested successfully.");
        } catch (Exception exception) {
            LOGGER.error("Hibernate database connection test failed.", exception);
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
