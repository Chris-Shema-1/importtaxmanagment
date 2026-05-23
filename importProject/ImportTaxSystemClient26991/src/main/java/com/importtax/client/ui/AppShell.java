package com.importtax.client.ui;



import com.importtax.client.util.CurrentSession;

import com.importtax.client.util.NavIcons;

import com.importtax.client.util.UIConstants;


import java.awt.*;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;

import java.util.List;

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

    private JPanel  navPanel;

    private final List<NavEntry> navEntries = new ArrayList<>();

    private boolean sidebarCollapsed;

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

        setSize(UIConstants.DASHBOARD_WINDOW_WIDTH, UIConstants.DASHBOARD_WINDOW_HEIGHT);

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

        bar.setBackground(UIConstants.HEADER_BG);

        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));

        JMenu help = new JMenu("Help");

        help.setFont(UIConstants.FONT_REGULAR);

        help.setForeground(UIConstants.TEXT_COLOR);

        JMenuItem about = new JMenuItem("About System");

        about.setFont(UIConstants.FONT_REGULAR);

        about.addActionListener(e -> AboutDialog.show(this));

        help.add(about);

        bar.add(help);

        return bar;

    }



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

        refreshNavStyles();

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



    private JPanel buildSidebar() {

        sidebarPanel = new JPanel(new MigLayout("fill, insets 0", "[fill]", "[][][grow][]")) {

            @Override

            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setColor(UIConstants.SIDEBAR_BG);

                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(UIConstants.SIDEBAR_BORDER);

                g2.fillRect(getWidth() - 1, 0, 1, getHeight());

                g2.dispose();

            }

        };

        sidebarPanel.setOpaque(false);

        updateSidebarWidth();



        JPanel logo = new JPanel(new MigLayout(

                sidebarCollapsed ? "insets 16 12 12 12" : "insets 20 18 16 18", "[grow]", "[][]"));

        logo.setOpaque(false);

        JLabel appName = new JLabel(sidebarCollapsed ? "IT" : "IMPORT TAX");

        appName.setFont(new Font("Segoe UI", Font.BOLD, sidebarCollapsed ? 14 : 15));

        appName.setForeground(UIConstants.TEXT_COLOR);

        logo.add(appName, "wrap");

        if (!sidebarCollapsed) {

            JLabel appSub = new JLabel("Management System");

            appSub.setFont(UIConstants.FONT_SMALL);

            appSub.setForeground(UIConstants.TEXT_MUTED);

            logo.add(appSub);

        }

        sidebarPanel.add(logo, "wrap");



        JButton collapseBtn = new JButton(sidebarCollapsed ? "\uE76C" : "\uE712");

        collapseBtn.setFont(NavIcons.iconFont(14));

        collapseBtn.setForeground(UIConstants.TEXT_SECONDARY);

        collapseBtn.setToolTipText(sidebarCollapsed ? "Expand sidebar" : "Collapse sidebar");

        collapseBtn.setBorderPainted(false);

        collapseBtn.setContentAreaFilled(false);

        collapseBtn.setFocusPainted(false);

        collapseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        collapseBtn.addActionListener(e -> toggleSidebar());

        sidebarPanel.add(collapseBtn,

                sidebarCollapsed ? "alignx center, wrap, gapbottom 4" : "alignx right, wrap, gapright 8");



        navPanel = new JPanel(new MigLayout(

                sidebarCollapsed ? "insets 4 6 4 6, fillx" : "insets 6 8 6 8, fillx", "[fill]", ""));

        navPanel.setOpaque(false);

        String[] pages = {PAGE_DASHBOARD, PAGE_USERS, PAGE_IMPORTS, PAGE_TAXES,

                PAGE_INVOICES, PAGE_PAYMENTS, PAGE_REPORTS, PAGE_NOTIFICATIONS, PAGE_SETTINGS};

        for (String p : pages) {

            if (PAGE_USERS.equals(p) && !CurrentSession.isAdmin()) {

                continue;

            }

            if (PAGE_PAYMENTS.equals(p) && CurrentSession.isCustomsOfficer()) {

                continue;

            }

            navPanel.add(navItem(p), "wrap, h 46!");

        }

        sidebarPanel.add(navPanel, "grow, wrap");



        JPanel bottom = new JPanel(new MigLayout(

                sidebarCollapsed ? "insets 12 8 16 8, fillx" : "insets 14 16 18 16, fillx",

                sidebarCollapsed ? "[center]" : "[36!][grow]", "[][]"));

        bottom.setOpaque(false);

        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.SIDEBAR_BORDER));



        JLabel avatar = new JLabel(initials(username));

        avatar.setFont(new Font("Segoe UI", Font.BOLD, 11));

        avatar.setForeground(UIConstants.TEXT_COLOR);

        avatar.setHorizontalAlignment(SwingConstants.CENTER);

        avatar.setOpaque(true);

        avatar.setBackground(UIConstants.withAlpha(UIConstants.PRIMARY_COLOR, 50));

        if (!sidebarCollapsed) {

            bottom.add(avatar, "h 36!, w 36!, spany 2");

            JLabel nameLabel = new JLabel(username);

            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

            nameLabel.setForeground(UIConstants.TEXT_COLOR);

            bottom.add(nameLabel, "wrap, gapleft 10");

            JButton logoutBtn = linkBtn("Sign out");

            logoutBtn.addActionListener(e -> handleLogout());

            bottom.add(logoutBtn, "gapleft 10");

        } else {
            avatar.setToolTipText(username);
            JPopupMenu menu = new JPopupMenu();
            JMenuItem signOut = new JMenuItem("Sign out");
            signOut.addActionListener(e -> handleLogout());
            menu.add(signOut);
            avatar.setComponentPopupMenu(menu);
            bottom.add(avatar, "w 36!, h 36!");
        }

        sidebarPanel.add(bottom, "growx");

        return sidebarPanel;

    }



    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        navEntries.clear();
        JPanel root = (JPanel) getContentPane();
        root.remove(sidebarPanel);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.revalidate();
        root.repaint();
    }



    private void updateSidebarWidth() {

        sidebarPanel.setPreferredSize(new Dimension(

                sidebarCollapsed ? UIConstants.SIDEBAR_COLLAPSED : UIConstants.SIDEBAR_WIDTH, 0));

    }



    private JPanel navItem(String label) {

        boolean active = label.equals(activePage);

        JPanel p = new JPanel(new MigLayout(

                sidebarCollapsed ? "insets 0, fillx" : "insets 0 12 0 8, fillx",

                sidebarCollapsed ? "[center]" : "[28!][grow]", "[fill]")) {

            @Override

            protected void paintComponent(Graphics g) {

                boolean isActive = label.equals(activePage);

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isActive) {

                    g2.setColor(UIConstants.SIDEBAR_ACTIVE);

                    g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 10, 10);

                    g2.setColor(UIConstants.PRIMARY_COLOR);

                    g2.fillRoundRect(4, 8, 3, getHeight() - 16, 4, 4);

                } else if (getMousePosition() != null) {

                    g2.setColor(UIConstants.withAlpha(UIConstants.SIDEBAR_HOVER, 180));

                    g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 10, 10);

                }

                g2.dispose();

                super.paintComponent(g);

            }

        };

        p.setOpaque(false);

        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        p.setToolTipText(sidebarCollapsed ? label : null);



        JLabel icon = new JLabel(NavIcons.glyph(label));

        icon.setFont(NavIcons.iconFont(16));

        icon.setForeground(active ? UIConstants.PRIMARY_LIGHT : UIConstants.TEXT_MUTED);

        p.add(icon, sidebarCollapsed ? "alignx center" : "");



        if (!sidebarCollapsed) {

            JLabel text = new JLabel(label);

            text.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));

            text.setForeground(active ? UIConstants.TEXT_COLOR : UIConstants.TEXT_SECONDARY);

            p.add(text, "grow");

        }



        p.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override

            public void mouseEntered(java.awt.event.MouseEvent e) {

                p.repaint();

            }



            @Override

            public void mouseExited(java.awt.event.MouseEvent e) {

                p.repaint();

            }



            @Override

            public void mouseClicked(java.awt.event.MouseEvent e) {

                navigate(label);

            }

        });



        navEntries.add(new NavEntry(label, p));

        return p;

    }



    private void refreshNavStyles() {

        for (NavEntry entry : navEntries) {

            entry.panel().repaint();

        }

        if (navPanel != null) {

            navPanel.repaint();

        }

    }



    private JPanel buildMain() {

        JPanel main = new JPanel(new BorderLayout());

        main.setBackground(UIConstants.BACKGROUND_COLOR);

        main.add(buildHeader(), BorderLayout.NORTH);

        main.add(cardPanel, BorderLayout.CENTER);

        return main;

    }



    private JPanel buildHeader() {

        JPanel h = new JPanel(new MigLayout("insets 0 28 0 28, fillx", "[grow][]", "[" + UIConstants.HEADER_HEIGHT + "!]"));

        h.setBackground(UIConstants.HEADER_BG);

        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));



        headerTitle = new JLabel(PAGE_DASHBOARD);

        headerTitle.setFont(UIConstants.FONT_HEADER);

        headerTitle.setForeground(UIConstants.TEXT_COLOR);

        h.add(headerTitle, "grow");



        clockLabel = new JLabel(" ");

        clockLabel.setFont(UIConstants.FONT_SMALL);

        clockLabel.setForeground(UIConstants.TEXT_MUTED);

        h.add(clockLabel);

        return h;

    }



    private JButton linkBtn(String text) {

        JButton b = new JButton(text);

        b.setFont(UIConstants.FONT_SMALL);

        b.setForeground(UIConstants.TEXT_MUTED);

        b.setBorderPainted(false);

        b.setContentAreaFilled(false);

        b.setFocusPainted(false);

        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.setMargin(new Insets(0, 0, 0, 0));

        b.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override

            public void mouseEntered(java.awt.event.MouseEvent e) {

                b.setForeground(UIConstants.PRIMARY_LIGHT);

            }



            @Override

            public void mouseExited(java.awt.event.MouseEvent e) {

                b.setForeground(UIConstants.TEXT_MUTED);

            }

        });

        return b;

    }



    private String initials(String name) {

        if (name == null || name.isBlank()) {

            return "?";

        }

        String[] parts = name.trim().split("\\s+");

        if (parts.length == 1) {

            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();

        }

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

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                }

            }

        }, "shell-clock");

        t.setDaemon(true);

        t.start();

    }



    private record NavEntry(String label, JPanel panel) {

    }

}


