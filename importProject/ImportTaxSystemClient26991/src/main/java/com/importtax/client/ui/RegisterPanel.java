package com.importtax.client.ui;

import com.importtax.client.model.UserRole;
import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.UIConstants;
import com.importtax.client.util.validators.RegistrationValidator;
import com.importtax.client.util.validators.ValidationResult;
import com.importtax.server.model.User;
import com.importtax.server.rmi.UserService;
import java.awt.*;
import java.awt.event.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RegisterPanel.class);

    private final AuthFrame    host;
    private JTextField         fullNameField;
    private JTextField         emailField;
    private JTextField         usernameField;
    private JPasswordField     passwordField;
    private JPasswordField     confirmPasswordField;
    private JComboBox<UserRole> roleComboBox;
    private RoundedButton      registerButton;
    private JLabel             statusLabel;
    private JLabel             usernameStatusLabel;
    private JLabel             emailStatusLabel;
    private JLabel             passwordStrengthLabel;
    private JPanel             strengthBar;
    private UserService        userService;
    private char               defaultEchoChar;
    private boolean            passwordVisible;

    public RegisterPanel(AuthFrame host) {
        this.host = host;
        initializeRmiService();
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);

        JScrollPane scroll = new JScrollPane(buildForm());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BACKGROUND_COLOR);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        setupKeyBindings();
        setupValidationListeners();
    }

    // ── RMI ────────────────────────────────────────────────────────────────
    private void initializeRmiService() {
        try {
            RmiConnection.initialize();
            userService = RmiConnection.lookup(UIConstants.RMI_SERVICE_USER);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("UserService unavailable for registration", e);
            userService = null;
        }
    }

    // ── Form ───────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel form = new JPanel(new MigLayout(
            "insets 40 48 40 48, fillx",
            "[fill]",
            "[]8[]28[]6[]14[]6[]14[]6[]10[]6[]6[]14[]6[]14[]6[]14[]20[]10[]16[]"
        ));
        form.setBackground(UIConstants.BACKGROUND_COLOR);

        // ── Header ──
        JLabel heading = new JLabel("Create Account");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(UIConstants.TEXT_COLOR);
        form.add(heading, "wrap");

        JLabel sub = new JLabel("Register a new user for the Import Tax Management System");
        sub.setFont(UIConstants.FONT_REGULAR);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(sub, "wrap");

        // ── Personal info ──
        form.add(sectionLabel("Personal Information"), "wrap");

        form.add(fieldLabel("Full Name"), "wrap");
        fullNameField = styledTextField();
        form.add(fullNameField, "h 46!, wrap");

        form.add(fieldLabel("Email Address"), "wrap");
        JPanel emailRow = inlineRow();
        emailField = styledTextField();
        emailStatusLabel = statusBadge();
        emailRow.add(emailField, "grow, h 46!");
        emailRow.add(emailStatusLabel, "w 60!, h 46!, gapleft 8");
        form.add(emailRow, "wrap");

        // ── Credentials ──
        form.add(sectionLabel("Account Credentials"), "wrap");

        form.add(fieldLabel("Username"), "wrap");
        JPanel usernameRow = inlineRow();
        usernameField = styledTextField();
        usernameStatusLabel = statusBadge();
        usernameRow.add(usernameField, "grow, h 46!");
        usernameRow.add(usernameStatusLabel, "w 60!, h 46!, gapleft 8");
        form.add(usernameRow, "wrap");

        form.add(fieldLabel("Password"), "wrap");
        JPanel pwRow = inlineRow();
        passwordField = styledPasswordField();
        defaultEchoChar = passwordField.getEchoChar();
        JButton eyeBtn = ghostButton("Show");
        eyeBtn.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            char echo = passwordVisible ? 0 : defaultEchoChar;
            passwordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
            eyeBtn.setText(passwordVisible ? "Hide" : "Show");
        });
        pwRow.add(passwordField, "grow, h 46!");
        pwRow.add(eyeBtn, "w 60!, h 46!, gapleft 8");
        form.add(pwRow, "wrap");

        strengthBar = buildStrengthBar();
        form.add(strengthBar, "h 4!, wrap, gaptop 6");
        passwordStrengthLabel = new JLabel(" ");
        passwordStrengthLabel.setFont(UIConstants.FONT_SMALL);
        passwordStrengthLabel.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(passwordStrengthLabel, "wrap");

        form.add(fieldLabel("Confirm Password"), "wrap");
        confirmPasswordField = styledPasswordField();
        form.add(confirmPasswordField, "h 46!, wrap");

        // ── Role ──
        form.add(sectionLabel("Access Level"), "wrap");

        form.add(fieldLabel("Role"), "wrap");
        roleComboBox = new JComboBox<>(UserRole.values());
        roleComboBox.setSelectedItem(null);
        roleComboBox.setFont(UIConstants.FONT_REGULAR);
        roleComboBox.setBackground(UIConstants.PANEL_COLOR);
        roleComboBox.setForeground(UIConstants.TEXT_COLOR);
        roleComboBox.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        form.add(roleComboBox, "h 46!, wrap");

        // ── Status ──
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        form.add(statusLabel, "wrap");

        // ── Primary CTA ──
        registerButton = new RoundedButton("Create Account");
        registerButton.setFont(UIConstants.FONT_BUTTON);
        registerButton.addActionListener(e -> handleRegistration());
        form.add(registerButton, "h 48!, wrap");

        // ── Switch link ──
        form.add(buildSwitchLink(), "wrap");

        // ── Footer ──
        JLabel footer = new JLabel("© 2026 Import Tax Management System");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        footer.setForeground(UIConstants.TEXT_MUTED);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        form.add(footer);

        return form;
    }

    // "Already have an account? Sign in"
    private JPanel buildSwitchLink() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        p.setOpaque(false);

        JLabel text = new JLabel("Already have an account?");
        text.setFont(UIConstants.FONT_SMALL);
        text.setForeground(UIConstants.TEXT_SECONDARY);

        JButton link = linkButton("Sign in");
        link.addActionListener(e -> host.showLogin());

        p.add(text);
        p.add(link);
        return p;
    }

    // ── Component helpers ──────────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(UIConstants.PRIMARY_COLOR);
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
            UIConstants.withAlpha(UIConstants.PRIMARY_COLOR, 60)));
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_LABEL);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        return l;
    }

    private JLabel statusBadge() {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(UIConstants.TEXT_SECONDARY);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBackground(UIConstants.PANEL_COLOR);
        l.setOpaque(true);
        l.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true));
        return l;
    }

    private JPanel inlineRow() {
        JPanel p = new JPanel(new MigLayout("insets 0, fillx", "[grow][pref]", "[]"));
        p.setOpaque(false);
        return p;
    }

    private JTextField styledTextField() {
        JTextField f = new JTextField();
        applyFieldStyle(f);
        return f;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        applyFieldStyle(f);
        return f;
    }

    private void applyFieldStyle(JTextField f) {
        f.setFont(UIConstants.FONT_REGULAR);
        f.setBackground(UIConstants.PANEL_COLOR);
        f.setForeground(UIConstants.TEXT_COLOR);
        f.setCaretColor(UIConstants.PRIMARY_COLOR);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIConstants.BORDER_COLOR, 1, true),
            new EmptyBorder(10, 14, 10, 14)));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIConstants.PRIMARY_COLOR, 2, true),
                    new EmptyBorder(9, 13, 9, 13)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIConstants.BORDER_COLOR, 1, true),
                    new EmptyBorder(10, 14, 10, 14)));
            }
        });
    }

    private JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setForeground(UIConstants.PRIMARY_COLOR);
        b.setBackground(UIConstants.PANEL_COLOR);
        b.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
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

    private JPanel buildStrengthBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 4, 0));
        bar.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            JPanel seg = new JPanel();
            seg.setBackground(UIConstants.BORDER_COLOR);
            seg.setOpaque(true);
            bar.add(seg);
        }
        return bar;
    }

    private void updateStrengthBar(int strength) {
        int filled = Math.min(4, strength);
        Color[] colors = { UIConstants.ERROR_COLOR, UIConstants.ERROR_COLOR,
                           UIConstants.WARNING_COLOR, UIConstants.SUCCESS_COLOR };
        for (int i = 0; i < 4; i++) {
            ((JPanel) strengthBar.getComponent(i))
                .setBackground(i < filled ? colors[filled - 1] : UIConstants.BORDER_COLOR);
        }
        strengthBar.repaint();
    }

    // ── Validation ─────────────────────────────────────────────────────────
    private void setupValidationListeners() {
        usernameField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { validateUsernameAsync(); }
        });
        emailField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { validateEmailAsync(); }
        });
        passwordField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { updatePasswordStrength(); }
        });
    }

    private void updatePasswordStrength() {
        String pw = new String(passwordField.getPassword());
        if (pw.isEmpty()) { passwordStrengthLabel.setText(" "); updateStrengthBar(0); return; }
        int s = 0;
        if (pw.length() >= 8)               s++;
        if (pw.matches(".*[A-Z].*"))        s++;
        if (pw.matches(".*[a-z].*"))        s++;
        if (pw.matches(".*[0-9].*"))        s++;
        if (pw.matches(".*[^A-Za-z0-9].*")) s++;
        updateStrengthBar(s);
        if (s <= 2) {
            passwordStrengthLabel.setText("Weak — add uppercase, numbers or symbols");
            passwordStrengthLabel.setForeground(UIConstants.ERROR_COLOR);
        } else if (s <= 4) {
            passwordStrengthLabel.setText("Good — consider adding a special character");
            passwordStrengthLabel.setForeground(UIConstants.WARNING_COLOR);
        } else {
            passwordStrengthLabel.setText("Strong password");
            passwordStrengthLabel.setForeground(UIConstants.SUCCESS_COLOR);
        }
    }

    private void validateUsernameAsync() {
        String username = usernameField.getText().trim();
        if (username.isEmpty() || userService == null) { usernameStatusLabel.setText(" "); return; }
        usernameStatusLabel.setText("…");
        usernameStatusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return userService.isUsernameUnique(username);
            }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    usernameStatusLabel.setText(ok ? "✓" : "Taken");
                    usernameStatusLabel.setForeground(ok ? UIConstants.SUCCESS_COLOR : UIConstants.ERROR_COLOR);
                } catch (Exception e) {
                    usernameStatusLabel.setText("?");
                    usernameStatusLabel.setForeground(UIConstants.TEXT_SECONDARY);
                }
            }
        }.execute();
    }

    private void validateEmailAsync() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || userService == null) { emailStatusLabel.setText(" "); return; }
        emailStatusLabel.setText("…");
        emailStatusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return userService.isEmailUnique(email);
            }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    emailStatusLabel.setText(ok ? "✓" : "Taken");
                    emailStatusLabel.setForeground(ok ? UIConstants.SUCCESS_COLOR : UIConstants.ERROR_COLOR);
                } catch (Exception e) {
                    emailStatusLabel.setText("?");
                    emailStatusLabel.setForeground(UIConstants.TEXT_SECONDARY);
                }
            }
        }.execute();
    }

    // ── Key bindings ───────────────────────────────────────────────────────
    private void setupKeyBindings() {
        InputMap  im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,  0), "register");
        am.put("register", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleRegistration(); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");
        am.put("back",     new AbstractAction() { public void actionPerformed(ActionEvent e) { host.showLogin(); } });
    }

    // ── Registration logic ─────────────────────────────────────────────────
    private void handleRegistration() {
        statusLabel.setText(" ");
        String fullName        = fullNameField.getText().trim();
        String email           = emailField.getText().trim();
        String username        = usernameField.getText().trim();
        String password        = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        UserRole role          = (UserRole) roleComboBox.getSelectedItem();

        ValidationResult v = RegistrationValidator.validateRegistration(
            fullName, email, username, password, confirmPassword, role);
        if (!v.isValid()) { showError(v.getErrorMessage()); return; }
        if (userService == null) {
            showError("Registration service unavailable — start the RMI server first."); return;
        }

        setLoading(true);
        User user = new User(fullName, email, username, password, role.name());
        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() throws Exception {
                return userService.registerUser(user);
            }
            @Override protected void done() {
                setLoading(false);
                try {
                    User registered = get();
                    logger.info("Registered userId={}, username={}", registered.getUserId(), registered.getUsername());
                    JOptionPane.showMessageDialog(host,
                        "Account created successfully. You can now sign in.",
                        "Account Created", JOptionPane.INFORMATION_MESSAGE);
                    host.showLoginWithUsername(username);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    showError(cause.getMessage());
                    logger.warn("Registration failed: {}", cause.getMessage());
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading) {
        host.setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        registerButton.setEnabled(!loading);
        statusLabel.setText(loading ? "Creating account…" : " ");
        statusLabel.setForeground(loading ? UIConstants.INFO_COLOR : UIConstants.ERROR_COLOR);
    }

    private void showError(String msg) {
        statusLabel.setText("⚠  " + (msg == null || msg.isBlank() ? "Registration failed" : msg));
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
    }
}
