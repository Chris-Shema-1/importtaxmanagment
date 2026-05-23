package com.importtax.client.util;

import java.awt.Color;
import java.awt.Font;

public class UIConstants {

    private UIConstants() {}

    public static final String APP_NAME       = "Import Tax Management System";
    public static final String APP_TITLE      = APP_NAME;
    public static final String APP_VERSION    = "1.0.0";
    public static final String STUDENT_ID     = "26991";
    public static final String FOOTER_TAGLINE = APP_NAME + " — Distributed Java Application";

    // ── Modern dark palette ────────────────────────────────────────────────
    public static final Color BACKGROUND_COLOR = color(0x0F172A);
    public static final Color SIDEBAR_BG       = color(0x111827);
    public static final Color PANEL_COLOR      = color(0x1E293B);
    public static final Color HEADER_BG        = color(0x1E293B);
    public static final Color ROW_EVEN         = color(0x1E293B);
    public static final Color ROW_ALT          = color(0x253347);
    public static final Color SIDEBAR_HOVER    = color(0x334155);
    public static final Color SIDEBAR_ACTIVE   = color(0x334155);
    public static final Color SIDEBAR_BORDER   = color(0x334155);
    public static final Color BORDER_COLOR     = color(0x334155);

    public static final Color PRIMARY_COLOR    = color(0x3B82F6);
    public static final Color PRIMARY_DARK     = color(0x2563EB);
    public static final Color PRIMARY_LIGHT    = color(0x60A5FA);
    public static final Color SUCCESS_COLOR    = color(0x10B981);
    public static final Color WARNING_COLOR    = color(0xF59E0B);
    public static final Color ERROR_COLOR      = color(0xEF4444);
    public static final Color INFO_COLOR       = color(0x3B82F6);
    public static final Color ACCENT_COLOR     = color(0xF59E0B);

    public static final Color TEXT_COLOR       = color(0xF8FAFC);
    public static final Color TEXT_SECONDARY   = color(0xCBD5E1);
    public static final Color TEXT_MUTED       = color(0x94A3B8);

    public static final Color BADGE_PENDING_BG    = withAlpha(WARNING_COLOR, 40);
    public static final Color BADGE_PAID_BG       = withAlpha(PRIMARY_COLOR, 45);
    public static final Color BADGE_CLEARED_BG    = withAlpha(SUCCESS_COLOR, 45);
    public static final Color BADGE_HOLD_BG       = withAlpha(ERROR_COLOR, 40);

    // ── Fonts ──────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD,  26);
    public static final Font FONT_HEADER    = new Font("Segoe UI", Font.BOLD,  18);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_REGULAR   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON    = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_LABEL     = new Font("Segoe UI", Font.BOLD,  11);
    public static final Font FONT_STAT      = new Font("Segoe UI", Font.BOLD,  28);

    // ── Dimensions ─────────────────────────────────────────────────────────
    public static final int BORDER_RADIUS       = 12;
    public static final int BORDER_RADIUS_LARGE = 16;
    public static final int BORDER_RADIUS_SMALL = 8;
    public static final int SIDEBAR_WIDTH       = 248;
    public static final int SIDEBAR_COLLAPSED   = 72;
    public static final int HEADER_HEIGHT       = 64;
    public static final int INPUT_HEIGHT        = 42;
    public static final int BUTTON_HEIGHT       = 40;
    public static final int TABLE_ROW_HEIGHT    = 44;
    public static final int DASHBOARD_WINDOW_WIDTH  = 1440;
    public static final int DASHBOARD_WINDOW_HEIGHT = 900;

    // ── RMI ────────────────────────────────────────────────────────────────
    public static final String RMI_SERVICE_USER         = "userService";
    public static final String RMI_SERVICE_TAX          = "taxService";
    public static final String RMI_SERVICE_IMPORT       = "importItemService";
    public static final String RMI_SERVICE_INVOICE      = "invoiceService";
    public static final String RMI_SERVICE_PAYMENT      = "paymentService";
    public static final String RMI_SERVICE_NOTIFICATION = "notificationService";
    public static final String RMI_SERVICE_OTP          = "otpService";
    public static final String RMI_SERVICE_REPORT       = "reportService";

    public static Color color(int rgb) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static Color blend(Color c1, Color c2, float t) {
        return new Color(
                (int) (c1.getRed()   * (1 - t) + c2.getRed()   * t),
                (int) (c1.getGreen() * (1 - t) + c2.getGreen() * t),
                (int) (c1.getBlue()  * (1 - t) + c2.getBlue()  * t),
                (int) (c1.getAlpha() * (1 - t) + c2.getAlpha() * t));
    }
}
