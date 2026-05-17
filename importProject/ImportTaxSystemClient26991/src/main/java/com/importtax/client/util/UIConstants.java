package com.importtax.client.util;

import java.awt.Color;
import java.awt.Font;

/**
 * UIConstants - Centralized UI configuration and constants for the application.
 * Contains colors, fonts, dimensions, and other UI-related constants.
 *
 * @author Import Tax System Development Team
 * @version 1.0.0
 */
public class UIConstants {

    // Prevent instantiation
    private UIConstants() {
    }

    // ==================== Application Constants ====================
    public static final String APP_NAME = "Import Tax Management System";
    public static final String APP_TITLE = "Import Tax Client";
    public static final String APP_VERSION = "1.0.0";

    // ==================== Color Palette ====================
    // Primary Colors
    public static final Color PRIMARY_COLOR = new Color(16, 124, 141);
    public static final Color PRIMARY_DARK = new Color(11, 84, 96);
    public static final Color PRIMARY_LIGHT = new Color(120, 213, 224);

    // Accent Colors
    public static final Color ACCENT_COLOR = new Color(232, 168, 56);
    public static final Color SUCCESS_COLOR = new Color(58, 179, 126);
    public static final Color ERROR_COLOR = new Color(213, 88, 88);
    public static final Color WARNING_COLOR = new Color(235, 184, 62);
    public static final Color INFO_COLOR = new Color(86, 151, 230);

    // Neutral Colors
    public static final Color BACKGROUND_COLOR = new Color(18, 24, 31);
    public static final Color PANEL_COLOR = new Color(28, 38, 48);
    public static final Color BORDER_COLOR = new Color(49, 63, 77);
    public static final Color TEXT_COLOR = new Color(235, 240, 245);
    public static final Color TEXT_SECONDARY = new Color(150, 168, 182);

    // ==================== Font Definitions ====================
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 12);

    // ==================== Dimension Constants ====================
    // Window Dimensions
    public static final int LOGIN_WINDOW_WIDTH = 500;
    public static final int LOGIN_WINDOW_HEIGHT = 600;
    public static final int DASHBOARD_WINDOW_WIDTH = 1400;
    public static final int DASHBOARD_WINDOW_HEIGHT = 800;

    // Spacing & Padding
    public static final int PADDING_LARGE = 20;
    public static final int PADDING_MEDIUM = 15;
    public static final int PADDING_SMALL = 10;
    public static final int PADDING_TINY = 5;

    // Component Dimensions
    public static final int BUTTON_HEIGHT = 40;
    public static final int TEXT_FIELD_HEIGHT = 35;
    public static final int SIDEBAR_WIDTH = 250;
    public static final int HEADER_HEIGHT = 60;

    // Rounded Corners
    public static final int BORDER_RADIUS = 8;
    public static final int BORDER_RADIUS_LARGE = 15;
    public static final int BORDER_RADIUS_SMALL = 5;

    // ==================== Border Constants ====================
    public static final int BORDER_WIDTH = 1;
    public static final int BORDER_WIDTH_THICK = 2;

    // ==================== Animation & Timing ====================
    public static final int ANIMATION_DURATION = 300; // milliseconds
    public static final int HOVER_EFFECT_DELAY = 200; // milliseconds

    // ==================== RMI Configuration ====================
    public static final String RMI_SERVICE_USER = "userService";
    public static final String RMI_SERVICE_TAX = "taxService";
    public static final String RMI_SERVICE_IMPORT = "importItemService";
    public static final String RMI_SERVICE_INVOICE = "invoiceService";
    public static final String RMI_SERVICE_PAYMENT = "paymentService";
    public static final String RMI_SERVICE_NOTIFICATION = "notificationService";

    // ==================== Icons & Images ====================
    public static final String ICON_LOGIN_LOGO = "images/logo.png";
    public static final String ICON_DASHBOARD = "images/dashboard.png";
    public static final String ICON_USERS = "images/users.png";
    public static final String ICON_SETTINGS = "images/settings.png";
    public static final String ICON_LOGOUT = "images/logout.png";

    // ==================== Utility Methods ====================

    /**
     * Creates a transparent version of a color.
     *
     * @param color The source color
     * @param alpha The alpha value (0-255, where 0 is fully transparent and 255 is opaque)
     * @return A new Color with the specified alpha
     */
    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * Blends two colors together.
     *
     * @param color1 The first color
     * @param color2 The second color
     * @param blend The blend ratio (0.0 to 1.0, where 0.0 is color1 and 1.0 is color2)
     * @return The blended color
     */
    public static Color blend(Color color1, Color color2, float blend) {
        int red = (int) (color1.getRed() * (1 - blend) + color2.getRed() * blend);
        int green = (int) (color1.getGreen() * (1 - blend) + color2.getGreen() * blend);
        int blue = (int) (color1.getBlue() * (1 - blend) + color2.getBlue() * blend);
        int alpha = (int) (color1.getAlpha() * (1 - blend) + color2.getAlpha() * blend);
        return new Color(red, green, blue, alpha);
    }
}
