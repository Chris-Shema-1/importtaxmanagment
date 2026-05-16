package com.importtax.client.ui;

import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.User;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import net.miginfocom.swing.MigLayout;

public class SettingsPage extends JPanel {

    private static final long serialVersionUID = 1L;

    public SettingsPage(AppShell shell) {
        setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new MigLayout("insets 28 28 28 28, fillx", "[grow]", "[][][][]"));
        root.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.TEXT_COLOR);
        JLabel sub = new JLabel("Your account and session information");
        sub.setFont(UIConstants.FONT_REGULAR);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        JPanel hdr = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
        hdr.setOpaque(false);
        hdr.add(title, "wrap");
        hdr.add(sub);
        root.add(hdr, "growx, wrap, gapbottom 24");

        root.add(buildProfileCard(), "growx, wrap, gapbottom 16");
        root.add(buildSessionCard(), "growx, wrap");
        return root;
    }

    private JPanel buildProfileCard() {
        User u = CurrentSession.getLoggedInUser();

        JPanel card = new JPanel(new MigLayout("insets 24 28 24 28, fillx", "[160!][grow]", "")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PANEL_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        sectionTitle(card, "Profile", "span, wrap, gapbottom 16");

        row(card, "Full Name",  u != null ? u.getFullName()  : "--");
        row(card, "Username",   u != null ? u.getUsername()  : "--");
        row(card, "Email",      u != null ? u.getEmail()     : "--");
        row(card, "Role",       u != null ? u.getRole()      : "--");
        row(card, "Member Since", u != null && u.getCreatedAt() != null ? u.getCreatedAt().toString() : "--");

        return card;
    }

    private JPanel buildSessionCard() {
        JPanel card = new JPanel(new MigLayout("insets 24 28 24 28, fillx", "[160!][grow]", "")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PANEL_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        sectionTitle(card, "Session", "span, wrap, gapbottom 16");
        row(card, "Status",  "Active");
        row(card, "Server",  "localhost:5000");
        row(card, "Version", UIConstants.APP_VERSION);

        return card;
    }

    private void sectionTitle(JPanel card, String text, String constraints) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(UIConstants.TEXT_COLOR);
        card.add(l, constraints);
    }

    private void row(JPanel card, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        card.add(lbl, "aligny center, gapy 6 0");

        JTextField field = new JTextField(value);
        field.setFont(UIConstants.FONT_REGULAR);
        field.setForeground(UIConstants.TEXT_SECONDARY);
        field.setBackground(UIConstants.BACKGROUND_COLOR);
        field.setEditable(false);
        field.setFocusable(false);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIConstants.BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        card.add(field, "h 38!, growx, wrap, gapbottom 4");
    }
}
