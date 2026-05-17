package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.Notification;
import com.importtax.server.model.User;
import com.importtax.server.rmi.NotificationService;
import com.importtax.server.rmi.UserService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(LoginFrame.class);

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheckbox;
    private RoundedButton loginButton;
    private RoundedButton createAccountButton;
    private RoundedButton exitButton;
    private JLabel statusLabel;
    private JLabel dateTimeLabel;
    private UserService userService;
    private NotificationService notificationService;

    public LoginFrame() {
        this(null);
    }

    public LoginFrame(String prefilledUsername) {
        logger.info("Initializing LoginFrame");
        initializeRmiService();
        initializeFrame();
        setupLayout();
        if (prefilledUsername != null && !prefilledUsername.isBlank()) {
            usernameField.setText(prefilledUsername.trim());
        }
        setupKeyBindings();
        startClockUpdate();
    }

    private void initializeRmiService() {
        try {
            RmiConnection.initialize();
            userService = RmiConnection.lookup(UIConstants.RMI_SERVICE_USER);
            try {
                notificationService = RmiConnection.lookup(UIConstants.RMI_SERVICE_NOTIFICATION);
            } catch (RemoteException | NotBoundException ignored) {
                notificationService = null;
            }
            logger.info("UserService RMI connection established");
        } catch (RemoteException | NotBoundException e) {
            logger.warn("UserService is unavailable for login", e);
            userService = null;
            notificationService = null;
        }
    }

    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setBackground(UIConstants.BACKGROUND_COLOR);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        mainPanel.add(createBrandingPanel(), BorderLayout.WEST);
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(UIConstants.PRIMARY_DARK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(UIConstants.PRIMARY_COLOR);
                g2d.fillRect(getWidth() - 4, 0, 4, getHeight());
            }
        };
        panel.setPreferredSize(new Dimension(400, 650));
        panel.setLayout(new MigLayout("center, center, insets 40", "[center]", "[center]"));
        panel.setBackground(UIConstants.PRIMARY_DARK);

        JLabel logoLabel = new JLabel("IT");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 72));
        logoLabel.setForeground(Color.WHITE);
        panel.add(logoLabel, "wrap, gap 0 0 20 0");

        JLabel titleLabel = new JLabel("IMPORT TAX");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, "wrap");

        JLabel subtitleLabel = new JLabel("MANAGEMENT SYSTEM");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subtitleLabel.setForeground(UIConstants.PRIMARY_LIGHT);
        panel.add(subtitleLabel, "wrap, gap 0 0 30 0");

        JLabel descLabel = new JLabel("<html><center>Enterprise-grade distributed<br/>tax processing platform</center></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(180, 180, 180));
        panel.add(descLabel);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 50 50 30 50, fillx, filly", "[fill]", ""));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(UIConstants.TEXT_COLOR);
        panel.add(welcomeLabel, "wrap, gap 0 0 10 0");

        JLabel subHeaderLabel = new JLabel("Sign in to your account");
        subHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subHeaderLabel.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(subHeaderLabel, "wrap, gap 0 0 30 0");

        panel.add(createLabel("Username"), "wrap");
        usernameField = createStyledTextField();
        panel.add(usernameField, "grow, h 42!, wrap, gap 0 0 15 0");

        panel.add(createLabel("Password"), "wrap");
        passwordField = createStyledPasswordField();
        panel.add(passwordField, "grow, h 42!, wrap, gap 0 0 15 0");

        rememberMeCheckbox = new JCheckBox("Remember me");
        rememberMeCheckbox.setFont(UIConstants.FONT_REGULAR);
        rememberMeCheckbox.setBackground(UIConstants.BACKGROUND_COLOR);
        rememberMeCheckbox.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(rememberMeCheckbox, "wrap, gap 0 0 20 0");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        panel.add(statusLabel, "wrap, gap 0 0 10 0");

        JPanel buttonPanel = new JPanel(new MigLayout("fillx, insets 0", "[50%][50%]", ""));
        buttonPanel.setOpaque(false);
        loginButton = new RoundedButton("SIGN IN");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton, "grow, h 45!, gap 0 5 0 0");

        exitButton = new RoundedButton("EXIT");
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        exitButton.setStateColors(UIConstants.PRIMARY_DARK,
                UIConstants.PRIMARY_DARK.brighter(), UIConstants.PRIMARY_DARK.darker());
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton, "grow, h 45!");
        panel.add(buttonPanel, "grow, wrap, gap 0 0 18 0");

        createAccountButton = new RoundedButton("CREATE ACCOUNT");
        createAccountButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createAccountButton.setStateColors(UIConstants.ACCENT_COLOR,
                UIConstants.ACCENT_COLOR.brighter(), UIConstants.ACCENT_COLOR.darker());
        createAccountButton.addActionListener(e -> openRegisterFrame());
        panel.add(createAccountButton, "grow, h 42!, wrap, gap 0 0 24 0");

        dateTimeLabel = new JLabel(" ");
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dateTimeLabel.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(dateTimeLabel, "wrap, gap 0 0 10 0");

        JLabel footerLabel = new JLabel("(c) 2026 Import Tax Management System. All rights reserved.");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        footerLabel.setForeground(new Color(100, 100, 100));
        panel.add(footerLabel);
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(UIConstants.TEXT_COLOR);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        styleTextField(field);
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        styleTextField(field);
        return field;
    }

    private void styleTextField(JTextField field) {
        field.setFont(UIConstants.FONT_REGULAR);
        field.setBackground(UIConstants.PANEL_COLOR);
        field.setForeground(UIConstants.TEXT_COLOR);
        field.setCaretColor(UIConstants.TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBackground(UIConstants.PANEL_COLOR.brighter());
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2, true),
                        new EmptyBorder(10, 12, 10, 12)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBackground(UIConstants.PANEL_COLOR);
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                        new EmptyBorder(10, 12, 10, 12)));
            }
        });
    }

    private void setupKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "login");
        actionMap.put("login", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        actionMap.put("exit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private void handleLogin() {
        if (!loginButton.isEnabled()) {
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            showError("Please enter your username");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }
        if (userService == null) {
            showError("Login service is unavailable. Start the RMI server and try again.");
            return;
        }

        setLoading(true);
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return userService.authenticateUser(username, password);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    User authenticatedUser = get();
                    if (authenticatedUser == null) {
                        showAuthenticationError();
                        return;
                    }
                    if (!verifyOtp(authenticatedUser)) {
                        showError("OTP verification failed");
                        return;
                    }
                    logger.info("User authenticated successfully: id={}, username={}",
                            authenticatedUser.getUserId(), authenticatedUser.getUsername());
                    CurrentSession.setLoggedInUser(authenticatedUser);
                    showSuccess("Login successful. Welcome, " + authenticatedUser.getUsername());
                    DashboardFrame dashboardFrame = new DashboardFrame(authenticatedUser.getUsername());
                    dashboardFrame.setVisible(true);
                    dispose();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (findCause(cause, IllegalArgumentException.class) != null) {
                        logger.warn("Authentication rejected for username '{}'", username);
                        showAuthenticationError();
                    } else {
                        logger.warn("Login request failed: {}", cause.getMessage());
                        showError("Login service is unavailable. Start the RMI server and try again.");
                    }
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        createAccountButton.setEnabled(!loading);
        exitButton.setEnabled(!loading);
        setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        statusLabel.setText(loading ? "Authenticating..." : " ");
        statusLabel.setForeground(loading ? UIConstants.INFO_COLOR : UIConstants.ERROR_COLOR);
    }

    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showAuthenticationError() {
        showError("Invalid username or password");
        passwordField.selectAll();
        passwordField.requestFocus();
    }

    private boolean verifyOtp(User authenticatedUser) {
        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        persistNotification("OTP", authenticatedUser.getUsername(),
                "Simulated OTP generated for login: " + otp, "SENT");

        JOptionPane.showMessageDialog(this,
                "OTP simulation for " + authenticatedUser.getUsername() + ": " + otp
                        + "\nEnter this code in the next prompt to complete sign-in.",
                "OTP Verification", JOptionPane.INFORMATION_MESSAGE);

        String input = JOptionPane.showInputDialog(this,
                "Enter the 6-digit OTP code:", "OTP Verification", JOptionPane.QUESTION_MESSAGE);
        boolean verified = otp.equals(input == null ? "" : input.trim());
        persistNotification("OTP", authenticatedUser.getUsername(),
                verified ? "OTP validated successfully" : "OTP validation failed", verified ? "SENT" : "FAILED");
        return verified;
    }

    private void persistNotification(String type, String recipient, String message, String status) {
        if (notificationService == null) {
            return;
        }
        try {
            Notification notification = new Notification();
            notification.setNotificationType(type);
            notification.setRecipient(recipient);
            notification.setMessage(message);
            notification.setStatus(status);
            notification.setSentAt(java.time.LocalDate.now());
            notificationService.save(notification);
        } catch (Exception ex) {
            logger.debug("Could not persist login notification", ex);
        }
    }

    private Throwable findCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return current;
            }
            current = current.getCause();
        }
        return null;
    }

    private void showSuccess(String message) {
        statusLabel.setText("Success: " + message);
        statusLabel.setForeground(UIConstants.SUCCESS_COLOR);
    }

    private void openRegisterFrame() {
        logger.info("Opening RegisterFrame from LoginFrame");
        RegisterFrame registerFrame = new RegisterFrame();
        registerFrame.setVisible(true);
        dispose();
    }

    private void startClockUpdate() {
        Thread clockThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    LocalDateTime now = LocalDateTime.now();
                    String formattedDate = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
                    dateTimeLabel.setText(formattedDate);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.debug("Clock update thread interrupted", e);
                }
            }
        }, "login-clock");
        clockThread.setDaemon(true);
        clockThread.start();
    }
}
