package com.importtax.client.ui;

import com.importtax.client.util.UIConstants;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;

public class DashboardPage extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AppShell shell;

    public DashboardPage(AppShell shell) {
        this.shell = shell;
        setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BACKGROUND_COLOR);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel p = new JPanel(new MigLayout(
            "insets 32 32 32 32, fillx",
            "[grow]",
            "[]24[]24[]24[]"
        ));
        p.setBackground(UIConstants.BACKGROUND_COLOR);

        // ── Greeting ──
        JLabel greeting = new JLabel("Good to see you back 👋");
        greeting.setFont(new Font("Segoe UI", Font.BOLD, 26));
        greeting.setForeground(UIConstants.TEXT_COLOR);
        p.add(greeting, "wrap");

        // ── Stat cards ──
        p.add(buildStatCards(), "growx, wrap");

        // ── Quick actions ──
        p.add(sectionLabel("Quick Actions"), "wrap, gapbottom 12");
        p.add(buildQuickActions(), "growx, wrap");

        // ── Recent activity ──
        p.add(sectionLabel("Recent Activity"), "wrap, gapbottom 12");
        p.add(buildActivity(), "growx, wrap");

        return p;
    }

    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        Object[][] cards = {
            {"Total Imports",  "1,234", new Color(0, 120, 215)},
            {"Pending Taxes",  "$56,789", new Color(255, 140, 0)},
            {"Paid Taxes",     "$128,450", new Color(34, 177, 76)},
            {"Total Revenue",  "$185,239", new Color(23, 162, 184)},
        };
        for (Object[] c : cards)
            row.add(statCard((String) c[0], (String) c[1], (Color) c[2]));
        return row;
    }

    private JPanel statCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new MigLayout("insets 22 20 22 20", "[grow]", "[]8[]")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PANEL_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, getHeight() - 4, getWidth(), 4, 0, 0);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 26));
        val.setForeground(accent);
        card.add(val, "wrap");

        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        card.add(lbl);
        return card;
    }

    private JPanel buildQuickActions() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);

        Object[][] actions = {
            {"New Import",       UIConstants.PRIMARY_COLOR,  AppShell.PAGE_IMPORTS},
            {"View Reports",     UIConstants.INFO_COLOR,     AppShell.PAGE_REPORTS},
            {"Process Payment",  UIConstants.SUCCESS_COLOR,  AppShell.PAGE_PAYMENTS},
            {"User Management",  UIConstants.ACCENT_COLOR,   AppShell.PAGE_USERS},
        };
        for (Object[] a : actions) {
            JButton btn = actionBtn((String) a[0], (Color) a[1]);
            String target = (String) a[2];
            btn.addActionListener(e -> shell.navigate(target));
            row.add(btn);
        }
        return row;
    }

    private JButton actionBtn(String text, Color color) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? color.darker()
                    : getModel().isRollover() ? color.brighter() : color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(UIConstants.FONT_BUTTON);
        b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(0, 44));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel buildActivity() {
        JPanel card = new JPanel(new MigLayout("insets 20 20 20 20, fillx", "[grow]", "")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PANEL_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        String[][] rows = {
            {"Import batch #1234 submitted",    "2 hours ago",   "#0078D7"},
            {"Tax report auto-generated",       "5 hours ago",   "#22B14C"},
            {"Payment processed — $50,000",     "Yesterday",     "#22B14C"},
            {"System backup completed",         "2 days ago",    "#6c757d"},
        };
        for (String[] row : rows) {
            card.add(activityRow(row[0], row[1], Color.decode(row[2])), "growx, wrap, gapbottom 4");
        }
        return card;
    }

    private JPanel activityRow(String text, String time, Color dot) {
        JPanel row = new JPanel(new MigLayout("insets 8 0 8 0, fillx", "[8!][grow][]", "[]"));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
            UIConstants.withAlpha(UIConstants.BORDER_COLOR, 80)));

        JPanel dotPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dot);
                g2.fillOval(0, (getHeight() - 8) / 2, 8, 8);
                g2.dispose();
            }
        };
        dotPanel.setOpaque(false);
        dotPanel.setPreferredSize(new Dimension(8, 8));
        row.add(dotPanel, "w 8!, h 8!, aligny center");

        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_REGULAR);
        label.setForeground(UIConstants.TEXT_COLOR);
        row.add(label, "grow, gapleft 12");

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(UIConstants.FONT_SMALL);
        timeLabel.setForeground(UIConstants.TEXT_MUTED);
        row.add(timeLabel);
        return row;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(UIConstants.TEXT_COLOR);
        return l;
    }
}
