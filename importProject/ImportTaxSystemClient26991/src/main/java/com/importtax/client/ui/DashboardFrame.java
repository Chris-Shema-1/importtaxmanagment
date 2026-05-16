package com.importtax.client.ui;

import javax.swing.JFrame;

/**
 * Thin launcher — delegates to AppShell which owns the full persistent UI.
 */
public class DashboardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public DashboardFrame(String username) {
        // Nothing — AppShell is the real frame
    }

    @Override
    public void setVisible(boolean visible) {
        // Intentionally empty — AppShell is launched directly from LoginPanel
    }

    @Override
    public void dispose() {
        // Intentionally empty
    }
}
