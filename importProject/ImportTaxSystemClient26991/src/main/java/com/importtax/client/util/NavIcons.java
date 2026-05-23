package com.importtax.client.util;

import com.importtax.client.ui.AppShell;

import java.awt.Font;
import java.util.Map;

/**
 * Navigation glyphs (Segoe UI Symbol) — no extra icon dependencies required.
 */
public final class NavIcons {

    private NavIcons() {
    }

    public static final Map<String, String> GLYPHS = Map.ofEntries(
            Map.entry(AppShell.PAGE_DASHBOARD,     "\uE80F"),
            Map.entry(AppShell.PAGE_USERS,         "\uE77B"),
            Map.entry(AppShell.PAGE_IMPORTS,       "\uE7BF"),
            Map.entry(AppShell.PAGE_TAXES,         "\uE9D9"),
            Map.entry(AppShell.PAGE_INVOICES,      "\uE8A1"),
            Map.entry(AppShell.PAGE_PAYMENTS,      "\uE8C7"),
            Map.entry(AppShell.PAGE_REPORTS,       "\uE9F9"),
            Map.entry(AppShell.PAGE_NOTIFICATIONS, "\uEA8F"),
            Map.entry(AppShell.PAGE_SETTINGS,      "\uE713")
    );

    public static String glyph(String pageLabel) {
        return GLYPHS.getOrDefault(pageLabel, "\uE700");
    }

    public static Font iconFont(int size) {
        Font mdl = new Font("Segoe MDL2 Assets", Font.PLAIN, size);
        if ("Segoe MDL2 Assets".equals(mdl.getFamily())) {
            return mdl;
        }
        return new Font("Segoe UI Symbol", Font.PLAIN, size);
    }
}
