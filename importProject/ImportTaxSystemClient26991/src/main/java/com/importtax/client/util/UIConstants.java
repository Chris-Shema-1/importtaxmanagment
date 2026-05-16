package com.importtax.client.util;

import java.awt.Color;
import java.awt.Font;

public class UIConstants {

    private UIConstants() {}

    public static final String APP_NAME    = "Import Tax Management System";
    public static final String APP_TITLE   = "Import Tax Client";
    public static final String APP_VERSION = "1.0.0";

    // ── Primary blue ───────────────────────────────────────────────────────
    public static final Color PRIMARY_COLOR = new Color(0,   120, 215);
    public static final Color PRIMARY_DARK  = new Color(0,   100, 180);
    public static final Color PRIMARY_LIGHT = new Color(100, 180, 255);

    // ── Sidebar 900-shade (dark slate-navy) ────────────────────────────────
    public static final Color SIDEBAR_BG     = new Color(15,  23,  42);
    public static final Color SIDEBAR_HOVER  = new Color(30,  41,  59);
    public static final Color SIDEBAR_ACTIVE = new Color(30,  41,  59);
    public static final Color SIDEBAR_BORDER = new Color(51,  65,  85);

    // ── Accent / semantic (original) ──────────────────────────────────────
    public static final Color ACCENT_COLOR   = new Color(255, 140,  0);
    public static final Color SUCCESS_COLOR  = new Color(34,  177, 76);
    public static final Color ERROR_COLOR    = new Color(220,  53, 69);
    public static final Color WARNING_COLOR  = new Color(255, 193,  7);
    public static final Color INFO_COLOR     = new Color(23,  162, 184);

    // ── Surface (light theme) ──────────────────────────────────────────────
    public static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    public static final Color PANEL_COLOR      = new Color(255, 255, 255);
    public static final Color BORDER_COLOR     = new Color(220, 224, 230);
    public static final Color TEXT_COLOR       = new Color(30,  36,  46);
    public static final Color TEXT_SECONDARY   = new Color(90,  100, 115);
    public static final Color TEXT_MUTED       = new Color(160, 168, 180);

    // ── Fonts ──────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font FONT_HEADER    = new Font("Segoe UI", Font.BOLD,  18);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_REGULAR   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON    = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_LABEL     = new Font("Segoe UI", Font.BOLD,  11);

    // ── Dimensions ─────────────────────────────────────────────────────────
    public static final int BORDER_RADIUS       = 8;
    public static final int BORDER_RADIUS_LARGE = 15;
    public static final int BORDER_RADIUS_SMALL = 5;
    public static final int SIDEBAR_WIDTH       = 250;
    public static final int HEADER_HEIGHT       = 60;
    public static final int DASHBOARD_WINDOW_WIDTH  = 1400;
    public static final int DASHBOARD_WINDOW_HEIGHT = 800;

    // ── RMI ────────────────────────────────────────────────────────────────
    public static final String RMI_SERVICE_USER         = "userService";
    public static final String RMI_SERVICE_TAX          = "taxService";
    public static final String RMI_SERVICE_IMPORT       = "importItemService";
    public static final String RMI_SERVICE_INVOICE      = "invoiceService";
    public static final String RMI_SERVICE_PAYMENT      = "paymentService";
    public static final String RMI_SERVICE_NOTIFICATION = "notificationService";

    // ── Helpers ────────────────────────────────────────────────────────────
    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static Color blend(Color c1, Color c2, float t) {
        return new Color(
            (int)(c1.getRed()   * (1-t) + c2.getRed()   * t),
            (int)(c1.getGreen() * (1-t) + c2.getGreen() * t),
            (int)(c1.getBlue()  * (1-t) + c2.getBlue()  * t),
            (int)(c1.getAlpha() * (1-t) + c2.getAlpha() * t)
        );
    }
}
