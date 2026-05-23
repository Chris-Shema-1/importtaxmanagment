package com.importtax.client;

import com.importtax.client.ui.AuthFrame;
import com.importtax.client.util.ThemeManager;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AppLauncher - Main entry point for the Import Tax Management System client application.
 */
public class AppLauncher {

    private static final Logger logger = LoggerFactory.getLogger(AppLauncher.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppLauncher::initializeApplication);
    }

    private static void initializeApplication() {
        try {
            logger.info("===============================================");
            logger.info("Starting Import Tax Management System Client");
            logger.info("===============================================");

            ThemeManager.apply();

            AuthFrame authFrame = new AuthFrame();
            authFrame.setVisible(true);

            logger.info("Application launched successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize application", e);
            System.exit(1);
        }
    }
}
