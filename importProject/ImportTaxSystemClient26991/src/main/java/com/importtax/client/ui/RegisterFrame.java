package com.importtax.client.ui;

import com.importtax.client.model.UserRole;
import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
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

public class RegisterFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final UserRole[] REGISTRATION_ROLES = {
        UserRole.FINANCE_OFFICER, UserRole.CUSTOMS_OFFICER
    };
    private static final Logger logger = LoggerFactory.getLogger(RegisterFrame.class);

    private JTextField     fullNameField;
    private JTextField     emailField;
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<UserRole> roleComboBox;
    private RoundedButton  registerButton;
    private RoundedButton  backButton;
    private JLabel         statusLabel;
    private JLabel         usernameStatusLabel;
    private JLabel         emailStatusLabel;
    private JLabel         passwordStrengthLabel;
    private JPanel         strengthBar;
    private UserService    userService;
    private char           defaultEchoChar;
    private boolean        passwordVisible;

    public RegisterFrame() {
        logger.info("Initializing RegisterFrame");
        initializeRmiService();
        initializeFrame();
        setupLayout();
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

    // ── Frame ──────────────────────────────────────────────────────────────
    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " — Create Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(580, 720));
        setSize(640, 800);
        setLocationRelativeTo(null);
        setBackground(UIConstants.BACKGROUND_COLOR);
    }

    // ── Layout ─────────────────────────────────────────────────────────────
    private void setupLayout() {
        // scrollable so it works on smaller screens
        JPanel content = new JPanel(new MigLayout(
            "insets 40 48 40 48, fillx",
            "[fill]",
            "[]8[]28[]6[]14[]6[]14[]6[]10[]6[]6[]14[]6[]14[]6[]14[]20[]10[]16[]"
        ));
        content.setBackground(UIConstants.BACKGROUND_COLOR);

        // ── Header ──
        JLabel heading = new JLabel("Create Account");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(UIConstants.TEXT_COLOR);
        content.add(heading, "wrap");

        JLabel subHeading = new JLabel("Register a new user for the Import Tax Management System");
        subHeading.setFont(UIConstants.FONT_REGULAR);
        subHeading.setForeground(UIConstants.TEXT_SECONDARY);
        content.add(subHeading, "wrap");

        // ── Section: Personal info ──
        content.add(sectionLabel("Personal Information"), "wrap");

        content.add(fieldLabel("Full Name"), "wrap");
        fullNameField = styledTextField();
        content.add(fullNameField, "h 46!, wrap");

        content.add(fieldLabel("Email Address"), "wrap");
        JPanel emailRow = inlineRow();
        emailField = styledTextField();
        emailStatusLabel = statusBadge();
        emailRow.add(emailField, "grow, h 46!");
        emailRow.add(emailStatusLabel, "w 60!, h 46!, gapleft 8");
        content.add(emailRow, "wrap");

        // ── Section: Account credentials ──
        content.add(sectionLabel("Account Credentials"), "wrap");

        content.add(fieldLabel("Username"), "wrap");
        JPanel usernameRow = inlineRow();
        usernameField = styledTextField();
        usernameStatusLabel = statusBadge();
        usernameRow.add(usernameField, "grow, h 46!");
        usernameRow.add(usernameStatusLabel, "w 60!, h 46!, gapleft 8");
        content.add(usernameRow, "wrap");

        content.add(fieldLabel("Password"), "wrap");
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
        content.add(pwRow, "wrap");

        // password strength bar
        strengthBar = buildStrengthBar();
        content.add(strengthBar, "h 4!, wrap, gaptop 6");
        passwordStrengthLabel = new JLabel(" ");
        passwordStrengthLabel.setFont(UIConstants.FONT_SMALL);
        passwordStrengthLabel.setForeground(UIConstants.TEXT_SECONDARY);
        content.add(passwordStrengthLabel, "wrap");

        content.add(fieldLabel("Confirm Password"), "wrap");
        confirmPasswordField = styledPasswordField();
        content.add(confirmPasswordField, "h 46!, wrap");

        // ── Section: Role ──
        content.add(sectionLabel("Access Level"), "wrap");

        content.add(fieldLabel("Role"), "wrap");
        roleComboBox = new JComboBox<>(REGISTRATION_ROLES);
        roleComboBox.setSelectedItem(null);
        roleComboBox.setFont(UIConstants.FONT_REGULAR);
        roleComboBox.setBackground(UIConstants.PANEL_COLOR);
        roleComboBox.setForeground(UIConstants.TEXT_COLOR);
        roleComboBox.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        roleComboBox.setPreferredSize(new Dimension(0, 46));
        content.add(roleComboBox, "h 46!, wrap");

        // ── Status ──
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        content.add(statusLabel, "wrap");

        // ── Buttons ──
        registerButton = new RoundedButton("Create Account");
        registerButton.setFont(UIConstants.FONT_BUTTON);
        registerButton.addActionListener(e -> handleRegistration());
        content.add(registerButton, "h 48!, wrap");

        backButton = new RoundedButton("Back to Sign In");
        backButton.setFont(UIConstants.FONT_BUTTON);
        backButton.setStateColors(
            UIConstants.PANEL_COLOR,
            UIConstants.PANEL_COLOR.brighter(),
            UIConstants.PANEL_COLOR.darker());
        backButton.addActionListener(e -> openLogin(null));
        content.add(backButton, "h 44!, wrap");

        // ── Footer ──
        JLabel footer = new JLabel("© 2026 Import Tax Management System. All rights reserved.");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        footer.setForeground(UIConstants.TEXT_MUTED);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(footer);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BACKGROUND_COLOR);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(scroll);
    }

    // ── Component helpers ──────────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(UIConstants.PRIMARY_LIGHT);
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
        f.setCaretColor(UIConstants.PRIMARY_LIGHT);
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
        b.setForeground(UIConstants.PRIMARY_LIGHT);
        b.setBackground(UIConstants.PANEL_COLOR);
        b.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // 4-segment strength bar
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
        // strength 0-5 mapped to 0-4 segments
        int filled = Math.min(4, strength);
        Color[] colors = { UIConstants.ERROR_COLOR, UIConstants.ERROR_COLOR,
                           UIConstants.WARNING_COLOR, UIConstants.SUCCESS_COLOR };
        for (int i = 0; i < 4; i++) {
            Component seg = strengthBar.getComponent(i);
            ((JPanel) seg).setBackground(i < filled ? colors[filled - 1] : UIConstants.BORDER_COLOR);
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
        if (pw.isEmpty()) {
            passwordStrengthLabel.setText(" ");
            updateStrengthBar(0);
            return;
        }
        int s = 0;
        if (pw.length() >= 8)              s++;
        if (pw.matches(".*[A-Z].*"))       s++;
        if (pw.matches(".*[a-z].*"))       s++;
        if (pw.matches(".*[0-9].*"))       s++;
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
        InputMap  im = getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,  0), "register");
        am.put("register", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleRegistration(); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");
        am.put("back",     new AbstractAction() { public void actionPerformed(ActionEvent e) { openLogin(null); } });
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
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                        "Account created successfully. You can now sign in.",
                        "Account Created", JOptionPane.INFORMATION_MESSAGE);
                    openLogin(username);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    showError(cause.getMessage());
                    logger.warn("Registration failed: {}", cause.getMessage());
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading) {
        setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        registerButton.setEnabled(!loading);
        backButton.setEnabled(!loading);
        statusLabel.setText(loading ? "Creating account…" : " ");
        statusLabel.setForeground(loading ? UIConstants.INFO_COLOR : UIConstants.ERROR_COLOR);
    }

    private void showError(String msg) {
        statusLabel.setText("⚠  " + (msg == null || msg.isBlank() ? "Registration failed" : msg));
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
    }

    private void openLogin(String username) {
        logger.info("Opening LoginFrame from RegisterFrame");
        dispose();
        LoginFrame lf = username == null ? new LoginFrame() : new LoginFrame(username);
        lf.setVisible(true);
    }
}
