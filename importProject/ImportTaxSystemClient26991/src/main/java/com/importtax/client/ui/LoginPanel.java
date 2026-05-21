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
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(LoginPanel.class);

    private final AuthFrame    host;
    private JTextField         usernameField;
    private JPasswordField     passwordField;
    private JCheckBox          rememberMeCheckbox;
    private RoundedButton      loginButton;
    private JLabel             statusLabel;
    private UserService        userService;
    private OtpService         otpService;

    public LoginPanel(AuthFrame host) {
        this.host = host;
        initializeRmiService();
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);
        add(buildFormPanel(), BorderLayout.CENTER);
        setupKeyBindings();
    }

    void prefillUsername(String username) {
        if (username != null && !username.isBlank())
            usernameField.setText(username.trim());
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

    // ── Form panel ─────────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel form = new JPanel(new MigLayout(
            "insets 48 52 36 52, fillx",
            "[fill]", "[]6[]32[]6[]18[]6[]16[]10[]20[]16[]"
        ));
        form.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel heading = new JLabel("Welcome back");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(UIConstants.TEXT_COLOR);
        form.add(heading, "wrap");

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(UIConstants.FONT_REGULAR);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(sub, "wrap");

        form.add(fieldLabel("Username"), "wrap");
        usernameField = styledTextField();
        form.add(usernameField, "h 46!, wrap");

        form.add(fieldLabel("Password"), "wrap");
        JPanel pwRow = new JPanel(new MigLayout("insets 0, fillx", "[grow][46!]", "[46!]"));
        pwRow.setOpaque(false);
        passwordField = styledPasswordField();
        JButton eyeBtn = ghostButton("●");
        eyeBtn.setToolTipText("Show / hide password");
        eyeBtn.addActionListener(e -> {
            boolean vis = passwordField.getEchoChar() == 0;
            passwordField.setEchoChar(vis ? '●' : (char) 0);
            eyeBtn.setText(vis ? "●" : "○");
        });
        pwRow.add(passwordField, "grow, h 46!");
        pwRow.add(eyeBtn, "h 46!, w 46!");
        form.add(pwRow, "wrap");

        rememberMeCheckbox = new JCheckBox("Keep me signed in");
        rememberMeCheckbox.setFont(UIConstants.FONT_SMALL);
        rememberMeCheckbox.setOpaque(false);
        rememberMeCheckbox.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(rememberMeCheckbox, "wrap");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        form.add(statusLabel, "wrap");

        loginButton = new RoundedButton("Sign In");
        loginButton.setFont(UIConstants.FONT_BUTTON);
        loginButton.addActionListener(e -> handleLogin());
        form.add(loginButton, "h 48!, wrap");

        // ── Switch link ──
        form.add(buildSwitchLink(), "wrap");

        // ── Footer ──
        JLabel footer = new JLabel("© 2026 Import Tax Management System");
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

    // "Don't have an account? Sign up"
    private JPanel buildSwitchLink() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        p.setOpaque(false);

        JLabel text = new JLabel("Don't have an account?");
        text.setFont(UIConstants.FONT_SMALL);
        text.setForeground(UIConstants.TEXT_SECONDARY);

        JButton link = linkButton("Sign up");
        link.addActionListener(e -> host.showRegister());

        p.add(text);
        p.add(link);
        return p;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_LABEL);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        return l;
    }

    private JTextField styledTextField() {
        JTextField f = new JTextField();
        applyFieldStyle(f);
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
        f.setCaretColor(UIConstants.PRIMARY_COLOR);
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

    private JButton ghostButton(String icon) {
        JButton b = new JButton(icon);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(UIConstants.TEXT_SECONDARY);
        b.setBackground(UIConstants.PANEL_COLOR);
        b.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton linkButton(String text) {
        JButton b = new JButton("<html><u>" + text + "</u></html>");
        b.setFont(UIConstants.FONT_SMALL);
        b.setForeground(UIConstants.PRIMARY_COLOR);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(0, 0, 0, 0));
        return b;
    }

    // ── Key bindings ───────────────────────────────────────────────────────
    private void setupKeyBindings() {
        InputMap  im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
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
        if (username.isEmpty()) { showError("Please enter your username"); usernameField.requestFocus(); return; }
        if (password.isEmpty()) { showError("Please enter your password"); passwordField.requestFocus(); return; }
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
                    if (isInvalidCredentials(ex)) {
                        showAuthError();
                        JOptionPane.showMessageDialog(host,
                                "Invalid username or password.",
                                UIConstants.APP_NAME,
                                JOptionPane.ERROR_MESSAGE);
                    } else if (isConnectionFailure(ex)) {
                        showError("Login service unavailable — start the RMI server first.");
                    } else {
                        showError("Invalid username or password");
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
                        JOptionPane.showMessageDialog(host,
                                "Could not send OTP to your registered email. Contact an administrator.",
                                "OTP Delivery Failed", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    JOptionPane.showMessageDialog(host,
                            "OTP code sent to your email.",
                            "OTP Sent", JOptionPane.INFORMATION_MESSAGE);

                    Frame owner = (Frame) SwingUtilities.getWindowAncestor(LoginPanel.this);
                    OtpVerificationDialog otpDlg = OtpVerificationDialog.forLogin(owner, username);
                    otpDlg.setVisible(true);
                    if (!otpDlg.isVerified()) {
                        showError("Sign-in cancelled — OTP not verified.");
                        return;
                    }

                    CurrentSession.setLoggedInUser(authenticatedUser);
                    logger.info("OTP verified; opening dashboard for username={}", username);
                    new AppShell(username).setVisible(true);
                    host.dispose();
                } catch (Exception ex) {
                    logger.warn("Failed to send login OTP", ex);
                    JOptionPane.showMessageDialog(host,
                            "Failed to send OTP. Check the server connection and try again.",
                            "OTP Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading, String message) {
        loginButton.setEnabled(!loading);
        host.setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
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

    private boolean isInvalidCredentials(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String message = current.getMessage();
            if (message != null
                    && message.toLowerCase(java.util.Locale.ROOT).contains("invalid username or password")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isConnectionFailure(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof java.rmi.ConnectException
                    || current instanceof java.rmi.ConnectIOException
                    || current instanceof java.rmi.NotBoundException
                    || current instanceof java.net.ConnectException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase(java.util.Locale.ROOT);
                if (lower.contains("connection refused")
                        || lower.contains("connect to")
                        || lower.contains("not bound")
                        || lower.contains("no such object")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

}
