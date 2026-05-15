package com.importtax.client;

import com.formdev.flatlaf.FlatDarkLaf;
import com.importtax.client.ui.LoginFrame;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AppLauncher - Main entry point for the Import Tax Management System client application.
 * Initializes the application, applies FlatLaf theme, and launches the login interface.
 *
 * @author Import Tax System Development Team
 * @version 1.0.0
 */
public class AppLauncher {

    private static final Logger logger = LoggerFactory.getLogger(AppLauncher.class);

    /**
     * Main method - Application entry point.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater to ensure GUI operations run on the Event Dispatch Thread
        SwingUtilities.invokeLater(AppLauncher::initializeApplication);
    }

    /**
     * Initializes the application by setting up the theme and launching the UI.
     */
    private static void initializeApplication() {
        try {
            logger.info("===============================================");
            logger.info("Starting Import Tax Management System Client");
            logger.info("===============================================");

            // Apply FlatLaf Dark theme
            applyTheme();

            // Launch Login Frame
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);

            logger.info("Application launched successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize application", e);
            System.exit(1);
        }
    }

    /**
     * Applies the FlatLaf Dark theme to the application.
     */
    private static void applyTheme() {
        try {
            logger.info("Applying FlatLaf Dark theme");
            FlatDarkLaf.setup();
            logger.info("Theme applied successfully");
        } catch (Exception e) {
            logger.error("Failed to apply theme", e);
            // Continue with default theme if FlatLaf fails
        }
    }
}
