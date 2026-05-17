package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.ImportItem;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.Notification;
import com.importtax.server.model.Payment;
import com.importtax.server.model.Tax;
import com.importtax.server.model.User;
import com.importtax.server.rmi.ImportItemService;
import com.importtax.server.rmi.InvoiceService;
import com.importtax.server.rmi.NotificationService;
import com.importtax.server.rmi.PaymentService;
import com.importtax.server.rmi.TaxService;
import com.importtax.server.rmi.UserService;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(DashboardFrame.class);

    private final String username;
    private JLabel greetingLabel;
    private JLabel dateTimeLabel;
    private JLabel statusLabel;
    private JLabel importsMetric;
    private JLabel taxMetric;
    private JLabel paymentsMetric;
    private JLabel usersMetric;
    private JPanel activityPanel;

    private UserService userService;
    private ImportItemService importItemService;
    private TaxService taxService;
    private InvoiceService invoiceService;
    private PaymentService paymentService;
    private NotificationService notificationService;

    public DashboardFrame(String username) {
        this.username = username != null ? username : "Operator";
        logger.info("Initializing DashboardFrame for user: {}", this.username);
        initializeServices();
        initializeFrame();
        setContentPane(createContent());
        startClockUpdate();
        loadOverview();
    }

    private void initializeServices() {
        try {
            RmiConnection.initialize();
            userService = RmiConnection.lookup(UIConstants.RMI_SERVICE_USER);
            importItemService = RmiConnection.lookup(UIConstants.RMI_SERVICE_IMPORT);
            taxService = RmiConnection.lookup(UIConstants.RMI_SERVICE_TAX);
            invoiceService = RmiConnection.lookup(UIConstants.RMI_SERVICE_INVOICE);
            paymentService = RmiConnection.lookup(UIConstants.RMI_SERVICE_PAYMENT);
            notificationService = RmiConnection.lookup(UIConstants.RMI_SERVICE_NOTIFICATION);
        } catch (Exception ex) {
            logger.warn("Some dashboard services are unavailable", ex);
        }
    }

    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 920);
        setMinimumSize(new Dimension(1280, 760));
        setLocationRelativeTo(null);
        setBackground(UIConstants.BACKGROUND_COLOR);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.add(createSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.add(createHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new MigLayout("insets 28, fillx, wrap 1", "[grow]", ""));
        body.setOpaque(false);
        body.add(createHeroPanel(), "growx");
        body.add(createMetricsPanel(), "growx");
        body.add(createActionStrip(), "growx");
        body.add(createActivityPanel(), "grow");

        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIConstants.BACKGROUND_COLOR);
        scrollPane.setBackground(UIConstants.BACKGROUND_COLOR);
        main.add(scrollPane, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        return root;
    }

    private JPanel createSidebar() {
        RoundedPanel sidebar = new RoundedPanel(0, 0, new Color(13, 20, 28));
        sidebar.setPreferredSize(new Dimension(290, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(24, 18, 24, 18));

        JLabel logo = new JLabel("Import Tax Hub");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(UIConstants.TEXT_COLOR);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);

        JLabel subtitle = new JLabel("Distributed finance operations");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        subtitle.setBorder(new EmptyBorder(6, 0, 20, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(subtitle);

        String[][] items = {
                {"Overview", "Live summary of the platform", "overview"},
                {"Users", "Review registered operators", "users"},
                {"Import Items", "Manage imported goods", "imports"},
                {"Taxes", "Maintain tax profiles", "taxes"},
                {"Invoices", "Issue and review invoices", "invoices"},
                {"Payments", "Record invoice payments", "payments"},
                {"Reports", "Export operational reports", "reports"},
                {"Notifications", "OTP and workflow messages", "notifications"}
        };

        for (String[] item : items) {
            sidebar.add(navButton(item[0], item[1], item[2]));
            sidebar.add(Box.createVerticalStrut(10));
        }

        sidebar.add(Box.createVerticalGlue());

        RoundedButton logout = new RoundedButton("Logout");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setStateColors(UIConstants.ERROR_COLOR,
                UIConstants.ERROR_COLOR.brighter(), UIConstants.ERROR_COLOR.darker());
        logout.addActionListener(e -> logout());
        sidebar.add(logout);

        return sidebar;
    }

    private RoundedPanel navButton(String title, String subtitle, String action) {
        RoundedPanel panel = new RoundedPanel(10, 10, new Color(24, 33, 42));
        panel.setLayout(new MigLayout("insets 14, fillx", "[grow]", "[][]"));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(UIConstants.TEXT_COLOR);
        panel.add(titleLabel, "wrap");

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(subtitleLabel);

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openModule(action);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panel.setBackground(new Color(31, 44, 56));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                panel.setBackground(new Color(24, 33, 42));
            }
        });
        return panel;
    }

    private JPanel createHeader() {
        RoundedPanel header = new RoundedPanel(0, 0, new Color(20, 30, 39));
        header.setPreferredSize(new Dimension(0, 90));
        header.setLayout(new MigLayout("insets 18 28, fillx", "[grow][pref]", ""));

        JPanel left = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
        left.setOpaque(false);
        greetingLabel = new JLabel("Welcome back, " + username);
        greetingLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        greetingLabel.setForeground(UIConstants.TEXT_COLOR);
        left.add(greetingLabel, "wrap");

        dateTimeLabel = new JLabel(" ");
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateTimeLabel.setForeground(UIConstants.TEXT_SECONDARY);
        left.add(dateTimeLabel);
        header.add(left, "grow");

        statusLabel = new JLabel("Connected to RMI services");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(UIConstants.SUCCESS_COLOR);
        header.add(statusLabel);
        return header;
    }

    private RoundedPanel createHeroPanel() {
        RoundedPanel hero = new RoundedPanel(18, 18, new Color(22, 34, 46));
        hero.setLayout(new MigLayout("insets 26, fillx", "[grow][220!]", "[][][]"));

        JLabel headline = new JLabel("Operational command center");
        headline.setFont(new Font("Segoe UI", Font.BOLD, 30));
        headline.setForeground(UIConstants.TEXT_COLOR);
        hero.add(headline, "wrap");

        JLabel copy = new JLabel("<html>Track imports, enforce tax rules, issue invoices, "
                + "capture payments, and review system activity from one place.</html>");
        copy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        copy.setForeground(UIConstants.TEXT_SECONDARY);
        hero.add(copy, "width 70%, wrap, gapbottom 16");

        JPanel heroActions = new JPanel(new MigLayout("insets 0", "[][12!][grow]", ""));
        heroActions.setOpaque(false);
        RoundedButton newImport = new RoundedButton("Create Import");
        newImport.setStateColors(UIConstants.PRIMARY_COLOR,
                UIConstants.PRIMARY_COLOR.brighter(), UIConstants.PRIMARY_DARK);
        newImport.addActionListener(e -> openModule("imports"));
        RoundedButton recordPayment = new RoundedButton("Record Payment");
        recordPayment.setStateColors(UIConstants.ACCENT_COLOR,
                UIConstants.ACCENT_COLOR.brighter(), UIConstants.ACCENT_COLOR.darker());
        recordPayment.addActionListener(e -> openModule("payments"));
        heroActions.add(newImport, "w 150!, h 42!");
        heroActions.add(recordPayment, "w 150!, h 42!");
        hero.add(heroActions, "wrap");

        JLabel badge = new JLabel("RMI + Hibernate + Swing");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        badge.setForeground(UIConstants.ACCENT_COLOR);
        hero.add(badge, "cell 1 0, alignx right");

        return hero;
    }

    private JPanel createMetricsPanel() {
        JPanel cards = new JPanel(new java.awt.GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        importsMetric = metricValueCard(cards, "Imports", "Loading...", UIConstants.PRIMARY_COLOR);
        taxMetric = metricValueCard(cards, "Tax Profiles", "Loading...", UIConstants.ACCENT_COLOR);
        paymentsMetric = metricValueCard(cards, "Payments", "Loading...", UIConstants.SUCCESS_COLOR);
        usersMetric = metricValueCard(cards, "Users", "Loading...", UIConstants.INFO_COLOR);
        return cards;
    }

    private JLabel metricValueCard(JPanel container, String label, String initialValue, Color accent) {
        RoundedPanel card = new RoundedPanel(14, 14, new Color(24, 36, 48), accent, 2);
        card.setLayout(new MigLayout("insets 18", "[grow]", "[][]"));
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelComponent.setForeground(UIConstants.TEXT_SECONDARY);
        JLabel valueComponent = new JLabel(initialValue);
        valueComponent.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueComponent.setForeground(accent);
        card.add(labelComponent, "wrap");
        card.add(valueComponent);
        container.add(card);
        return valueComponent;
    }

    private RoundedPanel createActionStrip() {
        RoundedPanel strip = new RoundedPanel(14, 14, new Color(24, 36, 48));
        strip.setLayout(new MigLayout("insets 18, fillx", "[grow][grow][grow][grow]", ""));

        strip.add(actionTile("Manage Taxes", "Keep rates and descriptions up to date", () -> openModule("taxes")), "grow");
        strip.add(actionTile("Issue Invoice", "Create or revise tax invoices", () -> openModule("invoices")), "grow");
        strip.add(actionTile("Review Notifications", "OTP and workflow audit trail", () -> openModule("notifications")), "grow");
        strip.add(actionTile("Export Reports", "Generate CSV summaries", () -> openModule("reports")), "grow");
        return strip;
    }

    private RoundedPanel actionTile(String title, String description, Runnable action) {
        RoundedPanel tile = new RoundedPanel(12, 12, new Color(30, 44, 57));
        tile.setLayout(new MigLayout("insets 16", "[grow]", "[][]"));
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(UIConstants.TEXT_COLOR);
        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(UIConstants.TEXT_SECONDARY);
        tile.add(titleLabel, "wrap");
        tile.add(descLabel);
        tile.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }
        });
        return tile;
    }

    private RoundedPanel createActivityPanel() {
        RoundedPanel panel = new RoundedPanel(14, 14, new Color(24, 36, 48));
        panel.setLayout(new MigLayout("insets 20, fillx, wrap 1", "[grow]", ""));

        JLabel title = new JLabel("Recent operational activity");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UIConstants.TEXT_COLOR);
        panel.add(title, "wrap, gapbottom 10");

        activityPanel = new JPanel(new MigLayout("insets 0, fillx, wrap 1", "[grow]", ""));
        activityPanel.setOpaque(false);
        panel.add(activityPanel, "growx");
        return panel;
    }

    private void loadOverview() {
        if (userService == null || importItemService == null || taxService == null
                || invoiceService == null || paymentService == null || notificationService == null) {
            statusLabel.setText("Some services are unavailable. Check the RMI server.");
            statusLabel.setForeground(UIConstants.WARNING_COLOR);
            renderActivity(List.of());
            return;
        }

        setLoading(true);
        new SwingWorker<Void, Void>() {
            List<User> users;
            List<ImportItem> items;
            List<Tax> taxes;
            List<Invoice> invoices;
            List<Payment> payments;
            List<Notification> notifications;

            @Override
            protected Void doInBackground() throws Exception {
                users = userService.findAll();
                items = importItemService.findAllItems();
                taxes = taxService.findAll();
                invoices = invoiceService.findAll();
                payments = paymentService.findAll();
                notifications = notificationService.findAll();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    importsMetric.setText(String.valueOf(items.size()));
                    taxMetric.setText(String.valueOf(taxes.size()));
                    paymentsMetric.setText(payments.size() + " / " + invoices.size());
                    usersMetric.setText(String.valueOf(users.size()));
                    renderActivity(notifications.isEmpty() ? synthesizeActivity(items, payments) : notifications);
                    statusLabel.setText("Dashboard refreshed successfully");
                    statusLabel.setForeground(UIConstants.SUCCESS_COLOR);
                } catch (Exception ex) {
                    statusLabel.setText(extractError(ex, "Could not load dashboard data"));
                    statusLabel.setForeground(UIConstants.ERROR_COLOR);
                    renderActivity(List.of());
                } finally {
                    setLoading(false);
                }
            }
        }.execute();
    }

    private List<Notification> synthesizeActivity(List<ImportItem> items, List<Payment> payments) {
        java.util.ArrayList<Notification> generated = new java.util.ArrayList<>();
        for (ImportItem item : items.stream().limit(3).toList()) {
            Notification n = new Notification();
            n.setNotificationType("IMPORT");
            n.setRecipient(item.getImporterName());
            n.setStatus(item.getStatus());
            n.setMessage("Import item " + item.getItemName() + " is " + item.getStatus());
            n.setSentAt(item.getImportDate());
            generated.add(n);
        }
        for (Payment payment : payments.stream().limit(3).toList()) {
            Notification n = new Notification();
            n.setNotificationType("PAYMENT");
            n.setRecipient(payment.getInvoice() == null ? "Invoice" : payment.getInvoice().getInvoiceNumber());
            n.setStatus(payment.getPaymentStatus());
            n.setMessage("Payment of " + payment.getAmountPaid() + " via " + payment.getPaymentMethod());
            n.setSentAt(payment.getPaymentDate());
            generated.add(n);
        }
        return generated;
    }

    private void renderActivity(List<Notification> notifications) {
        activityPanel.removeAll();
        if (notifications.isEmpty()) {
            activityPanel.add(activityLabel("No recent activity has been recorded yet."));
        } else {
            for (Notification notification : notifications.stream().limit(6).toList()) {
                activityPanel.add(activityCard(notification), "growx, gapbottom 10");
            }
        }
        activityPanel.revalidate();
        activityPanel.repaint();
    }

    private RoundedPanel activityCard(Notification notification) {
        RoundedPanel card = new RoundedPanel(10, 10, new Color(29, 43, 55));
        card.setLayout(new MigLayout("insets 14, fillx", "[grow][pref]", "[][]"));
        JLabel message = new JLabel("<html><b>" + safe(notification.getNotificationType())
                + "</b>  " + safe(notification.getMessage()) + "</html>");
        message.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        message.setForeground(UIConstants.TEXT_COLOR);
        JLabel status = new JLabel(safe(notification.getStatus()));
        status.setFont(new Font("Segoe UI", Font.BOLD, 11));
        status.setForeground(colorForStatus(notification.getStatus()));
        JLabel meta = new JLabel(safe(notification.getRecipient()) + "  |  " + safe(notification.getSentAt()));
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        meta.setForeground(UIConstants.TEXT_SECONDARY);
        card.add(message, "grow");
        card.add(status, "alignx right, wrap");
        card.add(meta, "span");
        return card;
    }

    private JLabel activityLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(UIConstants.TEXT_SECONDARY);
        return label;
    }

    private Color colorForStatus(String status) {
        if (status == null) {
            return UIConstants.TEXT_SECONDARY;
        }
        return switch (status.toUpperCase()) {
            case "PAID", "CLEARED", "SENT" -> UIConstants.SUCCESS_COLOR;
            case "FAILED", "HOLD" -> UIConstants.ERROR_COLOR;
            default -> UIConstants.WARNING_COLOR;
        };
    }

    private void openModule(String action) {
        switch (action) {
            case "overview" -> loadOverview();
            case "users" -> openFrame(new UserManagementFrame());
            case "imports" -> openFrame(new ImportItemFrame());
            case "taxes" -> openFrame(new TaxFrame());
            case "invoices" -> openFrame(new InvoiceFrame());
            case "payments" -> openFrame(new PaymentFrame());
            case "reports" -> openFrame(new ReportsFrame());
            case "notifications" -> openFrame(new NotificationFrame());
            default -> JOptionPane.showMessageDialog(this, "Module is not available yet.");
        }
    }

    private void openFrame(JFrame frame) {
        frame.setVisible(true);
        dispose();
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this, "Logout from the current session?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        CurrentSession.clear();
        dispose();
        new LoginFrame().setVisible(true);
    }

    private void setLoading(boolean loading) {
        setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private void startClockUpdate() {
        Thread clockThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    dateTimeLabel.setText(LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy | HH:mm:ss")));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "dashboard-clock");
        clockThread.setDaemon(true);
        clockThread.start();
    }

    private String extractError(Exception ex, String fallback) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() == null ? fallback : cause.getMessage();
    }

    private String safe(Object value) {
        return value == null ? "-" : value.toString();
    }
}
