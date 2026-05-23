package com.importtax.client.util;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Applies global FlatLaf + custom dark theme defaults for the whole application.
 */
public final class ThemeManager {

    private ThemeManager() {
    }

    public static void apply() {
        try {
            FlatDarkLaf.setup();
        } catch (Exception ignored) {
            // continue with defaults
        }

        UIManager.put("Component.arc", UIConstants.BORDER_RADIUS);
        UIManager.put("Button.arc", UIConstants.BORDER_RADIUS);
        UIManager.put("TextComponent.arc", UIConstants.BORDER_RADIUS_SMALL);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.trackArc", 999);

        UIManager.put("Panel.background", UIConstants.BACKGROUND_COLOR);
        UIManager.put("Viewport.background", UIConstants.BACKGROUND_COLOR);
        UIManager.put("ScrollPane.background", UIConstants.BACKGROUND_COLOR);

        UIManager.put("Table.background", UIConstants.PANEL_COLOR);
        UIManager.put("Table.foreground", UIConstants.TEXT_COLOR);
        UIManager.put("Table.selectionBackground", UIConstants.PRIMARY_COLOR);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", UIConstants.SIDEBAR_BG);
        UIManager.put("TableHeader.foreground", UIConstants.TEXT_SECONDARY);
        UIManager.put("TableHeader.bottomSeparatorColor", UIConstants.BORDER_COLOR);

        UIManager.put("TextField.background", UIConstants.PANEL_COLOR);
        UIManager.put("TextField.foreground", UIConstants.TEXT_COLOR);
        UIManager.put("TextField.caretForeground", UIConstants.PRIMARY_LIGHT);
        UIManager.put("TextField.selectionBackground", UIConstants.PRIMARY_COLOR);
        UIManager.put("PasswordField.background", UIConstants.PANEL_COLOR);
        UIManager.put("PasswordField.foreground", UIConstants.TEXT_COLOR);
        UIManager.put("ComboBox.background", UIConstants.PANEL_COLOR);
        UIManager.put("ComboBox.foreground", UIConstants.TEXT_COLOR);
        UIManager.put("ComboBox.selectionBackground", UIConstants.PRIMARY_COLOR);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);

        UIManager.put("Button.background", UIConstants.PANEL_COLOR);
        UIManager.put("Button.foreground", UIConstants.TEXT_COLOR);
        UIManager.put("MenuBar.background", UIConstants.HEADER_BG);
        UIManager.put("Menu.background", UIConstants.PANEL_COLOR);
        UIManager.put("MenuItem.background", UIConstants.PANEL_COLOR);
        UIManager.put("OptionPane.background", UIConstants.BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", UIConstants.TEXT_COLOR);

        UIManager.put("Component.focusWidth", 2);
        UIManager.put("Component.focusColor", UIConstants.PRIMARY_COLOR);
    }

    public static void styleOptionPane() {
        for (Component c : getOptionPaneComponents()) {
            if (c instanceof JButton btn) {
                btn.setFont(UIConstants.FONT_BUTTON);
                btn.setFocusPainted(false);
                btn.setBorder(new EmptyBorder(10, 18, 10, 18));
            }
        }
    }

    private static Component[] getOptionPaneComponents() {
        Window[] windows = Window.getWindows();
        for (Window w : windows) {
            if (w instanceof JDialog dlg && dlg.getContentPane() instanceof JOptionPane) {
                return ((JPanel) ((JOptionPane) dlg.getContentPane()).getComponent(0)).getComponents();
            }
        }
        return new Component[0];
    }
}
