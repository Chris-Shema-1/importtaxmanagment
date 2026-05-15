package com.importtax.client.ui;

import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DashboardFrame - Modern professional dashboard interface for the Import Tax Management System.
 * Displays system overview, navigation menu, statistic cards, and quick-access features.
 *
 * @author Import Tax System Development Team
 * @version 1.0.0
 */
public class DashboardFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DashboardFrame.class);

    private final String username;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JLabel userGreetingLabel;
    private JLabel dateTimeLabel;
    private String activeMenuitem = "Dashboard";

    /**
     * Constructor - Creates and initializes the DashboardFrame.
     *
     * @param username The logged-in username
     */
    public DashboardFrame(String username) {
        this.username = username != null ? username : "Administrator";
        logger.info("Initializing DashboardFrame for user: {}", this.username);
        initializeFrame();
        setupLayout();
        startClockUpdate();
    }

    /**
     * Initializes the JFrame properties.
     */
    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        setLocationRelativeTo(null);
        setBackground(UIConstants.BACKGROUND_COLOR);
    }

    /**
     * Sets up the main layout with sidebar, header, and content area.
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        // Create sidebar
        sidebarPanel = createSidebar();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // Create content area with header and dashboard
        JPanel contentAreaPanel = new JPanel(new BorderLayout());
        contentAreaPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        // Create header
        JPanel headerPanel = createHeader();
        contentAreaPanel.add(headerPanel, BorderLayout.NORTH);

        // Create main content
        contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBackground(UIConstants.BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(UIConstants.BACKGROUND_COLOR);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentAreaPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(contentAreaPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    /**
     * Creates a modern sidebar navigation panel.
     *
     * @return The configured sidebar panel
     */
    private JPanel createSidebar() {
        RoundedPanel sidebar = new RoundedPanel(0, 0, new Color(35, 35, 45));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Logo section
        JLabel logoLabel = new JLabel("📋 IMPORT TAX");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoLabel.setForeground(UIConstants.PRIMARY_COLOR);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoLabel.setBorder(new EmptyBorder(0, 20, 10, 20));
        sidebar.add(logoLabel);

        JLabel sloganLabel = new JLabel("Management System");
        sloganLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sloganLabel.setForeground(UIConstants.TEXT_SECONDARY);
        sloganLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sloganLabel.setBorder(new EmptyBorder(0, 20, 20, 20));
        sidebar.add(sloganLabel);

        // Separator
        JPanel separator = new JPanel();
        separator.setBackground(UIConstants.BORDER_COLOR);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(separator);
        sidebar.add(Box.createVerticalStrut(10));

        // Navigation menu items
        String[][] navItems = {
            {"📊", "Dashboard"},
            {"👥", "Users"},
            {"📦", "Import Items"},
            {"💰", "Taxes"},
            {"🧾", "Invoices"},
            {"💳", "Payments"},
            {"📈", "Reports"},
            {"🔔", "Notifications"},
            {"⚙️", "Settings"}
        };

        for (String[] item : navItems) {
            JPanel navItemPanel = createNavMenuItem(item[0], item[1]);
            navItemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            sidebar.add(navItemPanel);
            sidebar.add(Box.createVerticalStrut(5));
        }

        // Add vertical spacer
        sidebar.add(Box.createVerticalGlue());

        // Separator before logout
        JPanel separator2 = new JPanel();
        separator2.setBackground(UIConstants.BORDER_COLOR);
        separator2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator2.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(separator2);
        sidebar.add(Box.createVerticalStrut(10));

        // Logout button
        RoundedButton logoutButton = createSidebarLogoutButton();
        logoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logoutButton);

        return sidebar;
    }

    /**
     * Creates a navigation menu item with hover effects.
     *
     * @param icon The icon emoji
     * @param label The menu label
     * @return The configured menu item panel
     */
    private JPanel createNavMenuItem(String icon, String label) {
        JPanel itemPanel = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                if (label.equals(activeMenuitem)) {
                    g.setColor(UIConstants.PRIMARY_COLOR);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        itemPanel.setBackground(new Color(35, 35, 45));
        itemPanel.setLayout(new MigLayout("insets 8 15 8 15, fillx", "[12][grow]", ""));
        itemPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        itemPanel.add(iconLabel);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textLabel.setForeground(label.equals(activeMenuitem) ? Color.WHITE : UIConstants.TEXT_COLOR);
        itemPanel.add(textLabel, "grow");

        // Hover effect
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.setBackground(new Color(50, 50, 60));
                itemPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!label.equals(activeMenuitem)) {
                    itemPanel.setBackground(new Color(35, 35, 45));
                }
                itemPanel.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                activeMenuitem = label;
                handleNavigation(label);
                sidebarPanel.repaint();
            }
        });

        return itemPanel;
    }

    /**
     * Creates the logout button for the sidebar.
     *
     * @return The configured logout button
     */
    private RoundedButton createSidebarLogoutButton() {
        RoundedButton logoutButton = new RoundedButton("🚪 LOGOUT");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setStateColors(new Color(220, 53, 69),
                new Color(240, 70, 85), new Color(200, 40, 55));
        logoutButton.addActionListener(e -> handleLogout());
        return logoutButton;
    }

    /**
     * Creates the professional header panel with user info and notifications.
     *
     * @return The configured header panel
     */
    private JPanel createHeader() {
        RoundedPanel headerPanel = new RoundedPanel(0, 0, new Color(45, 45, 55));
        headerPanel.setPreferredSize(new Dimension(0, 75));
        headerPanel.setLayout(new MigLayout("insets 15 25 15 25, fillx", "[grow][200]", ""));

        // Left section - Welcome message and user info
        JPanel leftSection = new JPanel();
        leftSection.setBackground(new Color(45, 45, 55));
        leftSection.setLayout(new MigLayout("", "[grow]", ""));

        userGreetingLabel = new JLabel("Welcome back, " + username + "! 👋");
        userGreetingLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        userGreetingLabel.setForeground(UIConstants.TEXT_COLOR);
        leftSection.add(userGreetingLabel, "wrap");

        dateTimeLabel = new JLabel("");
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateTimeLabel.setForeground(UIConstants.TEXT_SECONDARY);
        leftSection.add(dateTimeLabel);

        headerPanel.add(leftSection, "grow");

        // Right section - Notification and profile
        JPanel rightSection = new JPanel();
        rightSection.setBackground(new Color(45, 45, 55));
        rightSection.setLayout(new MigLayout("", "[center][center]", ""));

        JLabel notificationLabel = new JLabel("🔔");
        notificationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        notificationLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rightSection.add(notificationLabel, "gap 0 15 0 0");

        JLabel profileLabel = new JLabel("👤");
        profileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        profileLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rightSection.add(profileLabel);

        headerPanel.add(rightSection);

        return headerPanel;
    }

    /**
     * Creates the main content panel with dashboard cards and overview.
     *
     * @return The configured content panel
     */
    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        panel.setLayout(new MigLayout("insets 30 30 30 30, fillx", "[grow]", ""));

        // Dashboard title
        JLabel dashboardTitle = new JLabel("Dashboard Overview");
        dashboardTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        dashboardTitle.setForeground(UIConstants.TEXT_COLOR);
        panel.add(dashboardTitle, "wrap, gap 0 0 25 0");

        // Statistic cards
        JPanel cardsPanel = createStatisticCards();
        panel.add(cardsPanel, "grow, wrap, gap 0 0 35 0");

        // Quick actions section
        JLabel quickActionsTitle = new JLabel("Quick Actions");
        quickActionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        quickActionsTitle.setForeground(UIConstants.TEXT_COLOR);
        panel.add(quickActionsTitle, "wrap, gap 0 0 15 0");

        JPanel quickActionsPanel = createQuickActionsPanel();
        panel.add(quickActionsPanel, "grow, wrap, gap 0 0 35 0");

        // Recent activity section
        JLabel recentActivityTitle = new JLabel("Recent Activity");
        recentActivityTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recentActivityTitle.setForeground(UIConstants.TEXT_COLOR);
        panel.add(recentActivityTitle, "wrap, gap 0 0 15 0");

        JPanel activityPanel = createRecentActivityPanel();
        panel.add(activityPanel, "grow, wrap");

        return panel;
    }

    /**
     * Creates statistic cards with modern styling.
     *
     * @return The cards panel
     */
    private JPanel createStatisticCards() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        // Card data: title, value, icon, hex color
        String[][] cardData = {
            {"Total Imports", "1,234", "📦", "0078D7"},
            {"Pending Taxes", "$56,789", "💰", "FFC107"},
            {"Paid Taxes", "$128,450", "✓", "22B14C"},
            {"Total Revenue", "$185,239", "📊", "17A2B8"}
        };

        for (String[] data : cardData) {
            int colorValue = Integer.parseInt(data[3], 16);
            Color accentColor = new Color(colorValue);
            JPanel card = createStatisticCard(data[0], data[1], data[2], accentColor);
            cardsPanel.add(card);
        }

        return cardsPanel;
    }

    /**
     * Creates a single statistic card with accent color.
     *
     * @param title Card title
     * @param value Metric value
     * @param icon Icon emoji
     * @param accentColor Accent color
     * @return The configured card panel
     */
    private JPanel createStatisticCard(String title, String value, String icon, Color accentColor) {
        RoundedPanel card = new RoundedPanel(12, 12, new Color(50, 50, 60), accentColor, 2);
        card.setLayout(new MigLayout("insets 20, center", "[center]", ""));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        card.add(iconLabel, "wrap, gap 0 0 15 0");

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, "wrap");

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        card.add(titleLabel);

        return card;
    }

    /**
     * Creates quick actions panel with placeholder buttons.
     *
     * @return The quick actions panel
     */
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);

        String[] actions = {"New Import", "View Reports", "Process Payment", "User Management"};

        for (String action : actions) {
            RoundedButton button = new RoundedButton(action);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            button.addActionListener(e -> {
                if ("New Import".equals(action)) {
                    openImportItemsFrame();
                } else {
                    logger.info("Action clicked: {}", action);
                }
            });
            panel.add(button);
        }

        return panel;
    }

    /**
     * Creates recent activity panel placeholder.
     *
     * @return The activity panel
     */
    private JPanel createRecentActivityPanel() {
        RoundedPanel panel = new RoundedPanel(8, 8, new Color(50, 50, 60));
        panel.setLayout(new MigLayout("insets 15, fillx", "[grow]", ""));
        panel.setPreferredSize(new Dimension(0, 180));

        String[] activities = {
            "John Doe imported 5 items - 2 hours ago",
            "Tax report generated automatically - 5 hours ago",
            "Payment processed: $50,000 - Yesterday",
            "System backup completed - 2 days ago"
        };

        for (String activity : activities) {
            JLabel actLabel = new JLabel("• " + activity);
            actLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            actLabel.setForeground(UIConstants.TEXT_SECONDARY);
            panel.add(actLabel, "wrap");
        }

        return panel;
    }

    /**
     * Handles navigation menu items.
     *
     * @param item The selected navigation item
     */
    private void handleNavigation(String item) {
        logger.info("Navigation: {}", item);
        if ("Import Items".equals(item)) {
            openImportItemsFrame();
        }
    }

    private void openImportItemsFrame() {
        logger.info("Opening ImportItemFrame for user: {}", username);
        ImportItemFrame importItemFrame = new ImportItemFrame();
        importItemFrame.setVisible(true);
        dispose();
    }

    /**
     * Handles logout action.
     */
    private void handleLogout() {
        logger.info("User {} logging out", username);
        int confirmed = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            CurrentSession.clear();
            dispose();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        }
    }

    /**
     * Starts a background thread to update the date and time display.
     */
    private void startClockUpdate() {
        Thread clockThread = new Thread(() -> {
            while (true) {
                try {
                    LocalDateTime now = LocalDateTime.now();
                    String formattedDate = now.format(DateTimeFormatter.ofPattern(
                            "EEEE, MMMM dd, yyyy | HH:mm:ss"));
                    dateTimeLabel.setText(formattedDate);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.debug("Clock update thread interrupted", e);
                    break;
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }
}
