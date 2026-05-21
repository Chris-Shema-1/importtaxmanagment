package com.importtax.client.ui;

import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.UIConstants;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class AppShell extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final String PAGE_DASHBOARD     = "Dashboard";
    public static final String PAGE_USERS         = "Users";
    public static final String PAGE_IMPORTS       = "Import Items";
    public static final String PAGE_TAXES         = "Taxes";
    public static final String PAGE_INVOICES      = "Invoices";
    public static final String PAGE_PAYMENTS      = "Payments";
    public static final String PAGE_REPORTS       = "Reports";
    public static final String PAGE_NOTIFICATIONS = "Notifications";
    public static final String PAGE_SETTINGS      = "Settings";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     cardPanel  = new JPanel(cardLayout);
    private final String     username;

    private JLabel  headerTitle;
    private JLabel  clockLabel;
    private JPanel  sidebarPanel;
    private String  activePage = PAGE_DASHBOARD;

    private DashboardPage     dashboardPage;
    private UsersPage         usersPage;
    private ImportItemPage    importItemPage;
    private TaxPage           taxPage;
    private InvoicePage       invoicePage;
    private PaymentPage       paymentPage;
    private ReportsPage       reportsPage;
    private NotificationsPage notificationsPage;
    private SettingsPage      settingsPage;

    public AppShell(String username) {
        this.username = username != null ? username : "User";
        setTitle(UIConstants.APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1440, 900);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setBackground(UIConstants.BACKGROUND_COLOR);

        dashboardPage     = new DashboardPage(this);
        usersPage         = new UsersPage(this);
        importItemPage    = new ImportItemPage(this);
        taxPage           = new TaxPage(this);
        invoicePage       = new InvoicePage(this);
        paymentPage       = new PaymentPage(this);
        reportsPage       = new ReportsPage(this);
        notificationsPage = new NotificationsPage(this);
        settingsPage      = new SettingsPage(this);

        cardPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        cardPanel.add(dashboardPage,     PAGE_DASHBOARD);
        cardPanel.add(usersPage,         PAGE_USERS);
        cardPanel.add(importItemPage,    PAGE_IMPORTS);
        cardPanel.add(taxPage,           PAGE_TAXES);
        cardPanel.add(invoicePage,       PAGE_INVOICES);
        cardPanel.add(paymentPage,       PAGE_PAYMENTS);
        cardPanel.add(reportsPage,       PAGE_REPORTS);
        cardPanel.add(notificationsPage, PAGE_NOTIFICATIONS);
        cardPanel.add(settingsPage,      PAGE_SETTINGS);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMain(),    BorderLayout.CENTER);
        setJMenuBar(buildMenuBar());
        setContentPane(root);

        navigate(PAGE_DASHBOARD);
        startClock();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu help = new JMenu("Help");
        help.setFont(UIConstants.FONT_REGULAR);
        JMenuItem about = new JMenuItem("About System");
        about.setFont(UIConstants.FONT_REGULAR);
        about.addActionListener(e -> AboutDialog.show(this));
        help.add(about);
        bar.add(help);
        return bar;
    }

    // ── Navigation ─────────────────────────────────────────────────────────
    public void navigate(String page) {
        if (PAGE_USERS.equals(page) && !CurrentSession.isAdmin()) {
            com.importtax.client.util.UserMessageUtil.showAccessDenied(this);
            return;
        }
        if (PAGE_PAYMENTS.equals(page) && CurrentSession.isCustomsOfficer()) {
            com.importtax.client.util.UserMessageUtil.showAccessDenied(this);
            return;
        }

        activePage = page;
        headerTitle.setText(page);
        cardLayout.show(cardPanel, page);
        if (sidebarPanel != null) sidebarPanel.repaint();
        switch (page) {
            case PAGE_DASHBOARD     -> dashboardPage.reload();
            case PAGE_IMPORTS       -> importItemPage.reload();
            case PAGE_USERS         -> usersPage.reload();
            case PAGE_TAXES         -> taxPage.reload();
            case PAGE_INVOICES      -> invoicePage.reload();
            case PAGE_PAYMENTS      -> paymentPage.reload();
            case PAGE_REPORTS       -> reportsPage.reload();
            case PAGE_NOTIFICATIONS -> notificationsPage.reload();
        }
    }

    // ── Sidebar ────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        sidebarPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIConstants.SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UIConstants.SIDEBAR_BORDER);
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());
                g2.dispose();
            }
        };
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        sidebarPanel.setLayout(new MigLayout("fill, insets 0", "[fill]", "[][grow][]"));
        sidebarPanel.setOpaque(false);

        // Logo
        JPanel logo = new JPanel(new MigLayout("insets 24 20 20 20", "[grow]", "[][]"));
        logo.setOpaque(false);
        JLabel appName = new JLabel("IMPORT TAX");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        appName.setForeground(Color.WHITE);
        logo.add(appName, "wrap");
        JLabel appSub = new JLabel("Management System");
        appSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        appSub.setForeground(UIConstants.withAlpha(Color.WHITE, 120));
        logo.add(appSub);
        sidebarPanel.add(logo, "wrap");

        // Nav
        JPanel nav = new JPanel(new MigLayout("insets 8 0 8 0, fillx", "[fill]", ""));
        nav.setOpaque(false);
        String[] pages = {PAGE_DASHBOARD, PAGE_USERS, PAGE_IMPORTS, PAGE_TAXES,
                          PAGE_INVOICES, PAGE_PAYMENTS, PAGE_REPORTS, PAGE_NOTIFICATIONS, PAGE_SETTINGS};
        for (String p : pages) {
            if (PAGE_USERS.equals(p) && !CurrentSession.isAdmin()) {
                continue;
            }
            if (PAGE_PAYMENTS.equals(p) && CurrentSession.isCustomsOfficer()) {
                continue;
            }
            nav.add(navItem(p), "wrap, h 44!");
        }
        sidebarPanel.add(nav, "grow, wrap");

        // User + logout
        JPanel bottom = new JPanel(new MigLayout("insets 16 16 20 16, fillx", "[36!][grow]", "[][]"));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
            UIConstants.SIDEBAR_BORDER));

        JLabel avatar = new JLabel(initials(username));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        avatar.setForeground(Color.WHITE);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(UIConstants.withAlpha(Color.WHITE, 30));
        bottom.add(avatar, "h 36!, w 36!, spany 2");

        JLabel nameLabel = new JLabel(username);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(Color.WHITE);
        bottom.add(nameLabel, "wrap, gapleft 10");

        JButton logoutBtn = linkBtn("Sign out");
        logoutBtn.addActionListener(e -> handleLogout());
        bottom.add(logoutBtn, "gapleft 10");
        sidebarPanel.add(bottom, "growx");

        return sidebarPanel;
    }

    private JPanel navItem(String label) {
        JPanel p = new JPanel(new MigLayout("insets 0 16 0 16, fillx", "[3!][grow]", "[fill]")) {
            @Override protected void paintComponent(Graphics g) {
                boolean active = label.equals(activePage);
                Graphics2D g2 = (Graphics2D) g.create();
                if (active) {
                    g2.setColor(UIConstants.SIDEBAR_ACTIVE);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(UIConstants.PRIMARY_COLOR);
                    g2.fillRect(0, 0, 3, getHeight());
                } else if (getMousePosition() != null) {
                    g2.setColor(UIConstants.SIDEBAR_HOVER);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel bar = new JPanel(); bar.setOpaque(false);
        p.add(bar);

        JLabel text = new JLabel(label);
        text.setFont(new Font("Segoe UI", label.equals(activePage) ? Font.BOLD : Font.PLAIN, 13));
        text.setForeground(label.equals(activePage) ? Color.WHITE : UIConstants.withAlpha(Color.WHITE, 160));
        p.add(text, "grow");

        p.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { p.repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { p.repaint(); }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { navigate(label); }
        });
        return p;
    }

    // ── Main area ──────────────────────────────────────────────────────────
    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIConstants.BACKGROUND_COLOR);
        main.add(buildHeader(), BorderLayout.NORTH);
        main.add(cardPanel,     BorderLayout.CENTER);
        return main;
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new MigLayout("insets 0 28 0 28, fillx", "[grow][]", "[64!]"));
        h.setBackground(UIConstants.PANEL_COLOR);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));

        headerTitle = new JLabel(PAGE_DASHBOARD);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerTitle.setForeground(UIConstants.TEXT_COLOR);
        h.add(headerTitle, "grow");

        clockLabel = new JLabel(" ");
        clockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clockLabel.setForeground(UIConstants.TEXT_SECONDARY);
        h.add(clockLabel);
        return h;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private JButton linkBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setForeground(UIConstants.withAlpha(Color.WHITE, 160));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(0, 0, 0, 0));
        return b;
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private void handleLogout() {
        int ok = JOptionPane.showConfirmDialog(this,
            "Sign out of " + username + "?", "Sign Out", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            CurrentSession.clear();
            dispose();
            new AuthFrame().setVisible(true);
        }
    }

    private void startClock() {
        Thread t = new Thread(() -> {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, MMM dd  HH:mm");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String s = LocalDateTime.now().format(fmt);
                    SwingUtilities.invokeLater(() -> clockLabel.setText(s));
                    Thread.sleep(30_000);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "shell-clock");
        t.setDaemon(true);
        t.start();
    }
}
