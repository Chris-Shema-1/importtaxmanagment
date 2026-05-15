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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RegisterFrame.class);

    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<UserRole> roleComboBox;
    private JButton showPasswordButton;
    private RoundedButton registerButton;
    private RoundedButton backButton;
    private JLabel statusLabel;
    private JLabel usernameStatusLabel;
    private JLabel emailStatusLabel;
    private JLabel passwordStrengthLabel;
    private UserService userService;
    private char defaultEchoChar;
    private boolean passwordVisible;

    public RegisterFrame() {
        logger.info("Initializing RegisterFrame");
        initializeRmiService();
        initializeFrame();
        setupLayout();
        setupKeyBindings();
        setupValidationListeners();
    }

    private void initializeRmiService() {
        try {
            RmiConnection.initialize();
            userService = RmiConnection.lookup(UIConstants.RMI_SERVICE_USER);
            logger.info("UserService RMI connection established");
        } catch (RemoteException | NotBoundException e) {
            logger.warn("UserService is unavailable for registration", e);
            userService = null;
        }
    }

    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " - Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 760));
        setSize(720, 820);
        setLocationRelativeTo(null);
        setBackground(UIConstants.BACKGROUND_COLOR);
    }

    private void setupLayout() {
        RoundedPanel mainPanel = new RoundedPanel(0, 0, UIConstants.BACKGROUND_COLOR);
        mainPanel.setLayout(new MigLayout("insets 34 48 34 48, fill, hidemode 3", "[grow,fill]", "[]"));

        JPanel headerPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow]", "[][]"));
        headerPanel.setOpaque(false);

        JLabel headerLabel = new JLabel("Create Account");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        headerLabel.setForeground(UIConstants.TEXT_COLOR);
        headerPanel.add(headerLabel, "wrap");

        JLabel subHeaderLabel = new JLabel("Register a new user for the Import Tax Management System");
        subHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subHeaderLabel.setForeground(UIConstants.TEXT_SECONDARY);
        headerPanel.add(subHeaderLabel);
        mainPanel.add(headerPanel, "wrap, gapbottom 24");

        mainPanel.add(createLabel("Full Name"), "wrap");
        fullNameField = createStyledTextField();
        mainPanel.add(fullNameField, "h 44!, wrap, gapbottom 12");

        mainPanel.add(createLabel("Email"), "wrap");
        JPanel emailPanel = createInlineStatusPanel();
        emailField = createStyledTextField();
        emailStatusLabel = createStatusDot();
        emailPanel.add(emailField, "grow, h 44!");
        emailPanel.add(emailStatusLabel, "w 28!, align center");
        mainPanel.add(emailPanel, "wrap, gapbottom 12");

        mainPanel.add(createLabel("Username"), "wrap");
        JPanel usernamePanel = createInlineStatusPanel();
        usernameField = createStyledTextField();
        usernameStatusLabel = createStatusDot();
        usernamePanel.add(usernameField, "grow, h 44!");
        usernamePanel.add(usernameStatusLabel, "w 28!, align center");
        mainPanel.add(usernamePanel, "wrap, gapbottom 12");

        mainPanel.add(createLabel("Password"), "wrap");
        JPanel passwordPanel = createInlineStatusPanel();
        passwordField = createStyledPasswordField();
        defaultEchoChar = passwordField.getEchoChar();
        showPasswordButton = createGhostButton("Show");
        showPasswordButton.addActionListener(e -> togglePasswordVisibility());
        passwordPanel.add(passwordField, "grow, h 44!");
        passwordPanel.add(showPasswordButton, "w 74!, h 40!");
        mainPanel.add(passwordPanel, "wrap, gapbottom 4");

        passwordStrengthLabel = new JLabel(" ");
        passwordStrengthLabel.setFont(UIConstants.FONT_SMALL);
        mainPanel.add(passwordStrengthLabel, "wrap, gapbottom 8");

        mainPanel.add(createLabel("Confirm Password"), "wrap");
        confirmPasswordField = createStyledPasswordField();
        mainPanel.add(confirmPasswordField, "h 44!, wrap, gapbottom 12");

        mainPanel.add(createLabel("Role"), "wrap");
        roleComboBox = new JComboBox<>(UserRole.values());
        roleComboBox.setSelectedItem(null);
        roleComboBox.setFont(UIConstants.FONT_REGULAR);
        roleComboBox.setBackground(UIConstants.PANEL_COLOR);
        roleComboBox.setForeground(UIConstants.TEXT_COLOR);
        roleComboBox.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        mainPanel.add(roleComboBox, "h 44!, wrap, gapbottom 14");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        mainPanel.add(statusLabel, "wrap, gapbottom 12");

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow][grow]", "[]"));
        buttonPanel.setOpaque(false);
        registerButton = new RoundedButton("Register");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerButton.setPreferredSize(new Dimension(0, 46));
        registerButton.addActionListener(e -> handleRegistration());
        buttonPanel.add(registerButton, "grow, h 46!, gapright 8");

        backButton = new RoundedButton("Back to Login");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backButton.setPreferredSize(new Dimension(0, 46));
        backButton.setStateColors(UIConstants.PRIMARY_DARK,
                UIConstants.PRIMARY_DARK.brighter(), UIConstants.PRIMARY_DARK.darker());
        backButton.addActionListener(e -> openLogin(null));
        buttonPanel.add(backButton, "grow, h 46!");
        mainPanel.add(buttonPanel, "wrap");

        setContentPane(mainPanel);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(UIConstants.TEXT_COLOR);
        return label;
    }

    private JPanel createInlineStatusPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 0, fillx", "[grow][pref]", "[]"));
        panel.setOpaque(false);
        return panel;
    }

    private JLabel createStatusDot() {
        JLabel label = new JLabel(" ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(UIConstants.TEXT_SECONDARY);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setBackground(UIConstants.PANEL_COLOR);
        field.setForeground(UIConstants.TEXT_COLOR);
        field.setCaretColor(UIConstants.TEXT_COLOR);
        field.setMargin(new Insets(8, 12, 8, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        field.addFocusListener(createFocusHighlighter(field));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setBackground(UIConstants.PANEL_COLOR);
        field.setForeground(UIConstants.TEXT_COLOR);
        field.setCaretColor(UIConstants.TEXT_COLOR);
        field.setMargin(new Insets(8, 12, 8, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        field.addFocusListener(createFocusHighlighter(field));
        return field;
    }

    private FocusAdapter createFocusHighlighter(JTextField field) {
        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UIConstants.PRIMARY_COLOR, 2, true),
                        new EmptyBorder(8, 12, 8, 12)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UIConstants.BORDER_COLOR, 1, true),
                        new EmptyBorder(8, 12, 8, 12)));
            }
        };
    }

    private JButton createGhostButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(UIConstants.PRIMARY_LIGHT);
        button.setBackground(UIConstants.PANEL_COLOR);
        button.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void setupKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "register");
        actionMap.put("register", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegistration();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");
        actionMap.put("back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openLogin(null);
            }
        });
    }

    private void setupValidationListeners() {
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateUsernameAsync();
            }
        });
        emailField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateEmailAsync();
            }
        });
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                updatePasswordStrength();
            }
        });
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        char echoChar = passwordVisible ? 0 : defaultEchoChar;
        passwordField.setEchoChar(echoChar);
        confirmPasswordField.setEchoChar(echoChar);
        showPasswordButton.setText(passwordVisible ? "Hide" : "Show");
    }

    private void updatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        if (password.isEmpty()) {
            passwordStrengthLabel.setText(" ");
            return;
        }

        int strength = 0;
        if (password.length() >= 8) {
            strength++;
        }
        if (password.matches(".*[A-Z].*")) {
            strength++;
        }
        if (password.matches(".*[a-z].*")) {
            strength++;
        }
        if (password.matches(".*[0-9].*")) {
            strength++;
        }
        if (password.matches(".*[^A-Za-z0-9].*")) {
            strength++;
        }

        if (strength <= 2) {
            passwordStrengthLabel.setText("Password strength: Weak");
            passwordStrengthLabel.setForeground(UIConstants.ERROR_COLOR);
        } else if (strength <= 4) {
            passwordStrengthLabel.setText("Password strength: Good");
            passwordStrengthLabel.setForeground(UIConstants.WARNING_COLOR);
        } else {
            passwordStrengthLabel.setText("Password strength: Strong");
            passwordStrengthLabel.setForeground(UIConstants.SUCCESS_COLOR);
        }
    }

    private void validateUsernameAsync() {
        String username = usernameField.getText().trim();
        if (username.isEmpty() || userService == null) {
            usernameStatusLabel.setText(" ");
            return;
        }
        usernameStatusLabel.setText("...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return userService.isUsernameUnique(username);
            }

            @Override
            protected void done() {
                try {
                    boolean unique = get();
                    usernameStatusLabel.setText(unique ? "OK" : "Taken");
                    usernameStatusLabel.setForeground(unique ? UIConstants.SUCCESS_COLOR : UIConstants.ERROR_COLOR);
                } catch (Exception e) {
                    usernameStatusLabel.setText("?");
                    usernameStatusLabel.setForeground(UIConstants.TEXT_SECONDARY);
                    logger.warn("Could not validate username uniqueness", e);
                }
            }
        }.execute();
    }

    private void validateEmailAsync() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || userService == null) {
            emailStatusLabel.setText(" ");
            return;
        }
        emailStatusLabel.setText("...");
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return userService.isEmailUnique(email);
            }

            @Override
            protected void done() {
                try {
                    boolean unique = get();
                    emailStatusLabel.setText(unique ? "OK" : "Taken");
                    emailStatusLabel.setForeground(unique ? UIConstants.SUCCESS_COLOR : UIConstants.ERROR_COLOR);
                } catch (Exception e) {
                    emailStatusLabel.setText("?");
                    emailStatusLabel.setForeground(UIConstants.TEXT_SECONDARY);
                    logger.warn("Could not validate email uniqueness", e);
                }
            }
        }.execute();
    }

    private void handleRegistration() {
        statusLabel.setText(" ");
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        UserRole role = (UserRole) roleComboBox.getSelectedItem();

        ValidationResult validation = RegistrationValidator.validateRegistration(
                fullName, email, username, password, confirmPassword, role);
        if (!validation.isValid()) {
            showError(validation.getErrorMessage());
            return;
        }
        if (userService == null) {
            showError("Registration service is unavailable. Start the RMI server and try again.");
            return;
        }

        setLoading(true);
        User user = new User(fullName, email, username, password, role.name());
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return userService.registerUser(user);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    User registeredUser = get();
                    logger.info("Registration completed for userId={}, username={}",
                            registeredUser.getUserId(), registeredUser.getUsername());
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                            "Registration successful. You can now sign in.",
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
        statusLabel.setText(loading ? "Creating account..." : " ");
        statusLabel.setForeground(loading ? UIConstants.INFO_COLOR : UIConstants.ERROR_COLOR);
    }

    private void showError(String message) {
        statusLabel.setText(message == null || message.isBlank() ? "Registration failed" : message);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
    }

    private void openLogin(String username) {
        logger.info("Opening LoginFrame from RegisterFrame");
        dispose();
        LoginFrame loginFrame = username == null ? new LoginFrame() : new LoginFrame(username);
        loginFrame.setVisible(true);
    }
}
