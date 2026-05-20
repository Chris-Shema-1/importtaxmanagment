package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.UIConstants;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public final class AboutDialog {

    private AboutDialog() {
    }

    public static void show(Component parent) {
        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                "About — " + UIConstants.APP_NAME, true);
        dlg.setLayout(new BorderLayout());

        JPanel content = new JPanel(new MigLayout("insets 28 32 20 32, fillx", "[grow]", "[][][][][][]"));
        content.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel title = new JLabel(UIConstants.APP_NAME);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UIConstants.TEXT_COLOR);
        content.add(title, "wrap");

        content.add(label("Student ID: " + UIConstants.STUDENT_ID), "wrap, gapbottom 12");
        content.add(label("Version: " + UIConstants.APP_VERSION), "wrap, gapbottom 16");

        content.add(section("Technologies"), "wrap");
        for (String tech : new String[]{
                "Java RMI", "Java Swing", "Hibernate ORM", "MySQL",
                "ActiveMQ", "Maven", "Brevo SMTP (OTP email)"
        }) {
            content.add(label("• " + tech), "wrap");
        }

        content.add(section("Server connection"), "wrap, gaptop 12");
        String status = connectionStatus();
        JLabel conn = label(status);
        conn.setForeground(status.startsWith("Connected")
                ? UIConstants.SUCCESS_COLOR : UIConstants.ERROR_COLOR);
        content.add(conn, "wrap");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackground(UIConstants.BACKGROUND_COLOR);
        JButton close = TaxPage.btn("Close", UIConstants.PRIMARY_COLOR);
        close.addActionListener(e -> dlg.dispose());
        buttons.add(close);

        dlg.add(content, BorderLayout.CENTER);
        dlg.add(buttons, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    private static String connectionStatus() {
        try {
            RmiConnection.initialize();
            RmiConnection.lookup(UIConstants.RMI_SERVICE_USER);
            return "Connected to RMI server at localhost:5000";
        } catch (Exception ex) {
            return "Not connected — start ImportTaxSystemServer26991";
        }
    }

    private static JLabel section(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_SUBHEADER);
        l.setForeground(UIConstants.TEXT_COLOR);
        return l;
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_REGULAR);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        return l;
    }
}
