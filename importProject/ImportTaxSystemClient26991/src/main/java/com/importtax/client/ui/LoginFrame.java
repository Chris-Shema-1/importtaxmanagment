package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.User;
import com.importtax.server.rmi.OtpService;
import com.importtax.server.rmi.UserService;
import java.awt.*;
import java.awt.event.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(LoginFrame.class);

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JCheckBox      rememberMeCheckbox;
    private RoundedButton  loginButton;
    private RoundedButton  createAccountButton;
    private JLabel         statusLabel;
    private JLabel         dateTimeLabel;
    private UserService    userService;
    private OtpService     otpService;

    public LoginFrame() { this(null); }

    public LoginFrame(String prefilledUsername) {
        logger.info("Initializing LoginFrame");
        initializeRmiService();
        initializeFrame();
        setupLayout();
        if (prefilledUsername != null && !prefilledUsername.isBlank())
            usernameField.setText(prefilledUsername.trim());
        setupKeyBindings();
        startClockUpdate();
    }

    // ── RMI ────────────────────────────────────────────────────────────────
    private void initializeRmiService() {
        try {
            RmiConnection.initialize();
            userService = RmiConnection.lookup(UIConstants.RMI_SERVICE_USER);
            otpService = RmiConnection.lookup(UIConstants.RMI_SERVICE_OTP);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("RMI login services unavailable", e);
            userService = null;
            otpService = null;
        }
    }

    // ── Frame setup ────────────────────────────────────────────────────────
    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " — Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 660);
        setMinimumSize(new Dimension(860, 580));
        setLocationRelativeTo(null);
        setBackground(UIConstants.BACKGROUND_COLOR);
    }

    private void setupLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.add(buildBrandPanel(), BorderLayout.WEST);
        root.add(buildFormPanel(),  BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Left branding panel ────────────────────────────────────────────────
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // solid dark background matching original PRIMARY_DARK
                g2.setColor(UIConstants.PRIMARY_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // subtle top-left glow circle
                g2.setColor(UIConstants.withAlpha(UIConstants.PRIMARY_COLOR, 35));
                g2.fillOval(-80, -80, 300, 300);
                // bottom-right glow
                g2.setColor(UIConstants.withAlpha(UIConstants.PRIMARY_COLOR, 20));
                g2.fillOval(getWidth() - 160, getHeight() - 160, 260, 260);
                // right-edge accent stripe (original design detail)
                g2.setColor(UIConstants.PRIMARY_COLOR);
                g2.fillRect(getWidth() - 4, 0, 4, getHeight());
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setLayout(new MigLayout("fill, insets 48 40 48 40", "[center, grow]", "[grow][][][][][grow]"));

        // logo badge
        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.withAlpha(Color.WHITE, 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UIConstants.withAlpha(Color.WHITE, 50));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(84, 84));
        badge.setLayout(new MigLayout("fill, insets 0", "[center]", "[center]"));
        JLabel logoLetter = new JLabel("IT");
        logoLetter.setFont(new Font("Segoe UI", Font.BOLD, 34));
        logoLetter.setForeground(Color.WHITE);
        badge.add(logoLetter);
        panel.add(badge, "cell 0 1, wrap, gapbottom 26");

        JLabel title = new JLabel("IMPORT TAX");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        panel.add(title, "cell 0 2, wrap");

        JLabel subtitle = new JLabel("MANAGEMENT SYSTEM");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        subtitle.setForeground(UIConstants.PRIMARY_LIGHT);
        panel.add(subtitle, "cell 0 3, wrap, gapbottom 28");

        JLabel desc = new JLabel("<html><center>Enterprise-grade distributed<br/>tax processing platform</center></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(new Color(180, 180, 180));
        panel.add(desc, "cell 0 4, wrap, gapbottom 0");

        dateTimeLabel = new JLabel(" ");
        dateTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dateTimeLabel.setForeground(UIConstants.TEXT_MUTED);
        panel.add(dateTimeLabel, "cell 0 5");

        return panel;
    }

    // ── Right form panel ───────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        // outer centers the form card vertically
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel form = new JPanel(new MigLayout(
            "insets 48 52 40 52, fillx",
            "[fill]",
            "[]6[]32[]6[]18[]6[]16[]10[]20[]10[]20[]"
        ));
        form.setBackground(UIConstants.BACKGROUND_COLOR);
        form.setMaximumSize(new Dimension(460, Integer.MAX_VALUE));

        // ── Heading ──
        JLabel heading = new JLabel("Welcome back");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(UIConstants.TEXT_COLOR);
        form.add(heading, "wrap");

        JLabel subHeading = new JLabel("Sign in to your account");
        subHeading.setFont(UIConstants.FONT_REGULAR);
        subHeading.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(subHeading, "wrap");

        // ── Username ──
        form.add(fieldLabel("Username"), "wrap");
        usernameField = styledTextField("e.g. john_doe");
        form.add(usernameField, "h 46!, wrap");

        // ── Password ──
        form.add(fieldLabel("Password"), "wrap");
        passwordField = styledPasswordField();
        // password row: field + show/hide toggle
        JPanel pwRow = new JPanel(new MigLayout("insets 0, fillx", "[grow][36!]", "[46!]"));
        pwRow.setOpaque(false);
        pwRow.add(passwordField, "grow, h 46!");
        JButton eyeBtn = ghostIconButton("●");
        eyeBtn.setToolTipText("Show / hide password");
        eyeBtn.addActionListener(e -> {
            boolean visible = passwordField.getEchoChar() == 0;
            passwordField.setEchoChar(visible ? '●' : (char) 0);
            eyeBtn.setText(visible ? "●" : "○");
        });
        pwRow.add(eyeBtn, "h 46!, w 46!");
        form.add(pwRow, "wrap");

        // ── Remember me ──
        rememberMeCheckbox = new JCheckBox("Keep me signed in");
        rememberMeCheckbox.setFont(UIConstants.FONT_SMALL);
        rememberMeCheckbox.setOpaque(false);
        rememberMeCheckbox.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(rememberMeCheckbox, "wrap");

        // ── Status ──
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        form.add(statusLabel, "wrap");

        // ── Primary CTA ──
        loginButton = new RoundedButton("Sign In");
        loginButton.setFont(UIConstants.FONT_BUTTON);
        loginButton.addActionListener(e -> handleLogin());
        form.add(loginButton, "h 48!, wrap");

        // ── Divider ──
        form.add(buildDivider("or"), "wrap");

        // ── Secondary CTA ──
        createAccountButton = new RoundedButton("Create an Account");
        createAccountButton.setFont(UIConstants.FONT_BUTTON);
        createAccountButton.setStateColors(
            UIConstants.PANEL_COLOR,
            UIConstants.PANEL_COLOR.brighter(),
            UIConstants.PANEL_COLOR.darker());
        createAccountButton.addActionListener(e -> openRegisterFrame());
        form.add(createAccountButton, "h 44!, wrap");

        // ── Footer ──
        JLabel footer = new JLabel("© 2026 Import Tax Management System. All rights reserved.");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        footer.setForeground(UIConstants.TEXT_MUTED);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        form.add(footer);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        outer.add(form, gbc);
        return outer;
    }

    // ── Component helpers ──────────────────────────────────────────────────
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_LABEL);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        return l;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField f = new JTextField();
        applyFieldStyle(f);
        setPlaceholder(f, placeholder);
        return f;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setEchoChar('●');
        applyFieldStyle(f);
        return f;
    }

    private void applyFieldStyle(JTextField f) {
        f.setFont(UIConstants.FONT_REGULAR);
        f.setBackground(UIConstants.PANEL_COLOR);
        f.setForeground(UIConstants.TEXT_COLOR);
        f.setCaretColor(UIConstants.PRIMARY_LIGHT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
            new EmptyBorder(10, 14, 10, 14)));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2, true),
                    new EmptyBorder(9, 13, 9, 13)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
                    new EmptyBorder(10, 14, 10, 14)));
            }
        });
    }

    private void setPlaceholder(JTextField f, String placeholder) {
        f.setForeground(UIConstants.TEXT_MUTED);
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(UIConstants.TEXT_COLOR); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().isBlank()) { f.setText(placeholder); f.setForeground(UIConstants.TEXT_MUTED); }
            }
        });
    }

    private JButton ghostIconButton(String icon) {
        JButton b = new JButton(icon);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(UIConstants.TEXT_SECONDARY);
        b.setBackground(UIConstants.PANEL_COLOR);
        b.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel buildDivider(String label) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JSeparator l = new JSeparator(); l.setForeground(UIConstants.BORDER_COLOR);
        JSeparator r = new JSeparator(); r.setForeground(UIConstants.BORDER_COLOR);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(l, BorderLayout.WEST);
        p.add(lbl, BorderLayout.CENTER);
        p.add(r, BorderLayout.EAST);
        return p;
    }

    // ── Key bindings ───────────────────────────────────────────────────────
    private void setupKeyBindings() {
        InputMap  im = getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,  0), "login");
        am.put("login", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleLogin(); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        am.put("exit",  new AbstractAction() { public void actionPerformed(ActionEvent e) { System.exit(0); } });
    }

    // ── Login logic ────────────────────────────────────────────────────────
    private void handleLogin() {
        if (!loginButton.isEnabled()) return;
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || username.equals("e.g. john_doe")) {
            showError("Please enter your username"); usernameField.requestFocus(); return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password"); passwordField.requestFocus(); return;
        }
        if (userService == null || otpService == null) {
            showError("Login service unavailable — start the RMI server first.");
            return;
        }
        loginButton.setEnabled(false);
        setLoading(true, "Authenticating…");
        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() throws Exception {
                return userService.authenticateUser(username, password);
            }
            @Override protected void done() {
                try {
                    User u = get();
                    if (u == null) {
                        setLoading(false, " ");
                        showAuthError();
                        return;
                    }
                    logger.info("Credentials accepted for username={}", u.getUsername());
                    proceedWithOtp(u);
                } catch (Exception ex) {
                    setLoading(false, " ");
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (findCause(cause, IllegalArgumentException.class) != null) {
                        showAuthError();
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Invalid username or password.",
                                UIConstants.APP_NAME,
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        showError("Login service unavailable — start the RMI server first.");
                    }
                }
            }
        }.execute();
    }

    private void proceedWithOtp(User authenticatedUser) {
        String username = authenticatedUser.getUsername();
        setLoading(true, "Sending OTP to your email…");
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return otpService.sendLoginOtp(username);
            }
            @Override protected void done() {
                setLoading(false, " ");
                try {
                    if (!Boolean.TRUE.equals(get())) {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Could not send OTP to your registered email. Contact an administrator.",
                                "OTP Delivery Failed", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "OTP code sent to your email.",
                            "OTP Sent", JOptionPane.INFORMATION_MESSAGE);

                    OtpVerificationDialog otpDlg = OtpVerificationDialog.forLogin(LoginFrame.this, username);
                    otpDlg.setVisible(true);
                    if (!otpDlg.isVerified()) {
                        showError("Sign-in cancelled — OTP not verified.");
                        return;
                    }

                    CurrentSession.setLoggedInUser(authenticatedUser);
                    logger.info("OTP verified; opening dashboard for username={}", username);
                    new AppShell(username).setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    logger.warn("Failed to send login OTP", ex);
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Failed to send OTP. Check the server connection and try again.",
                            "OTP Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading, String message) {
        loginButton.setEnabled(!loading);
        createAccountButton.setEnabled(!loading);
        setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        statusLabel.setText(loading ? message : " ");
        statusLabel.setForeground(loading ? UIConstants.INFO_COLOR : UIConstants.ERROR_COLOR);
    }

    private void showError(String msg) {
        statusLabel.setText("⚠  " + msg);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
    }

    private void showSuccess(String msg) {
        statusLabel.setText("✓  " + msg);
        statusLabel.setForeground(UIConstants.SUCCESS_COLOR);
    }

    private void showAuthError() {
        showError("Invalid username or password");
        passwordField.selectAll();
        passwordField.requestFocus();
    }

    private Throwable findCause(Throwable t, Class<? extends Throwable> type) {
        while (t != null) { if (type.isInstance(t)) return t; t = t.getCause(); }
        return null;
    }

    private void openRegisterFrame() {
        new RegisterFrame().setVisible(true);
        dispose();
    }

    // ── Clock ──────────────────────────────────────────────────────────────
    private void startClockUpdate() {
        Thread t = new Thread(() -> {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String text = LocalDateTime.now().format(fmt);
                    SwingUtilities.invokeLater(() -> dateTimeLabel.setText(text));
                    Thread.sleep(60_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "login-clock");
        t.setDaemon(true);
        t.start();
    }
}
