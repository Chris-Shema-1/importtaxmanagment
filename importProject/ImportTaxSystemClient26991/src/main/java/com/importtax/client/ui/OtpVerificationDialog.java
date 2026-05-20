package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.OtpValidationResult;
import com.importtax.server.rmi.OtpService;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Modal OTP verification dialog for login and registration flows.
 * Login OTP is sent via Brevo SMTP on the server; the code is never shown in the UI.
 */
public class OtpVerificationDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(OtpVerificationDialog.class);
    private static final int RESEND_COOLDOWN_SEC = 30;

    public enum Mode {
        LOGIN,
        REGISTRATION
    }

    private final Frame ownerFrame;
    private final String username;
    private final String registrationEmail;
    private final Mode mode;
    private final boolean skipInitialSend;

    private OtpService otpService;
    private boolean verified = false;

    private JTextField otpField;
    private JLabel     statusLabel;
    private JButton    verifyBtn;
    private JButton    resendBtn;
    private JButton    cancelBtn;
    private Timer      resendCooldownTimer;
    private int        resendCooldownRemaining;

    /** Login: OTP already sent by {@link #sendLoginOtp} before the dialog opens. */
    public static OtpVerificationDialog forLogin(Frame owner, String username) {
        return new OtpVerificationDialog(owner, username, Mode.LOGIN, null, true);
    }

    /** Registration: sends OTP to the email entered on the form when the dialog opens. */
    public static OtpVerificationDialog forRegistration(Frame owner, String username, String email) {
        return new OtpVerificationDialog(owner, username, Mode.REGISTRATION, email, false);
    }

    private OtpVerificationDialog(Frame owner, String username, Mode mode, String registrationEmail,
                                  boolean skipInitialSend) {
        super(owner, "Two-Factor Verification", true);
        this.ownerFrame = owner;
        this.username = username;
        this.mode = mode;
        this.registrationEmail = registrationEmail;
        this.skipInitialSend = skipInitialSend;
        initRmi();
        buildUi();
        setSize(440, 360);
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        if (!skipInitialSend) {
            sendOtp(false);
        } else {
            setStatus("Enter the code sent to your registered email.", UIConstants.TEXT_SECONDARY);
        }
    }

    public boolean isVerified() {
        return verified;
    }

    private void initRmi() {
        try {
            RmiConnection.initialize();
            otpService = RmiConnection.lookup(UIConstants.RMI_SERVICE_OTP);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("OtpService unavailable", e);
        }
    }

    private void buildUi() {
        JPanel root = new JPanel(new MigLayout("insets 32 36 20 36, fill", "[grow]", "[][][][]12[][][]"));
        root.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel icon = new JLabel("🔐");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(icon, "growx, wrap, gapbottom 8");

        JLabel title = new JLabel("Verify Your Identity");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UIConstants.TEXT_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(title, "growx, wrap");

        JLabel sub = new JLabel("<html><center>A 6-digit OTP has been sent to your registered email.<br>"
                + "Enter it below to continue.</center></html>");
        sub.setFont(UIConstants.FONT_SMALL);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(sub, "growx, wrap, gapbottom 16");

        otpField = new JTextField();
        otpField.setFont(new Font("Segoe UI", Font.BOLD, 22));
        otpField.setHorizontalAlignment(SwingConstants.CENTER);
        otpField.setBackground(UIConstants.PANEL_COLOR);
        otpField.setForeground(UIConstants.TEXT_COLOR);
        otpField.setCaretColor(UIConstants.PRIMARY_COLOR);
        otpField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 14, 10, 14)));
        otpField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    verifyOtp();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancel();
                }
            }
        });
        root.add(otpField, "growx, h 52!, wrap, gapbottom 4");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(statusLabel, "growx, wrap");

        JPanel btnRow = new JPanel(new MigLayout("insets 0, fillx", "[grow][100!][100!][100!]", "[44!]"));
        btnRow.setOpaque(false);
        cancelBtn = TaxPage.btn("Cancel", UIConstants.BORDER_COLOR);
        resendBtn = TaxPage.btn("Resend OTP", UIConstants.BORDER_COLOR);
        verifyBtn = TaxPage.btn("Verify", UIConstants.PRIMARY_COLOR);
        cancelBtn.addActionListener(e -> cancel());
        resendBtn.addActionListener(e -> sendOtp(true));
        verifyBtn.addActionListener(e -> verifyOtp());
        btnRow.add(new JLabel(), "grow");
        btnRow.add(cancelBtn, "h 44!");
        btnRow.add(resendBtn, "h 44!, gapleft 6");
        btnRow.add(verifyBtn, "h 44!, gapleft 6");
        root.add(btnRow, "growx");

        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "cancel");
        root.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        setContentPane(root);
    }

    private void sendOtp(boolean isResend) {
        if (otpService == null) {
            setStatus("OTP service unavailable — check server is running", UIConstants.ERROR_COLOR);
            JOptionPane.showMessageDialog(this,
                    "OTP service unavailable. Start the RMI server and try again.",
                    "OTP Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (resendCooldownRemaining > 0) {
            return;
        }

        setStatus(isResend ? "Resending OTP…" : "Sending OTP…", UIConstants.INFO_COLOR);
        setButtonsEnabled(false);
        setWaitCursor(true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (mode == Mode.LOGIN) {
                    return otpService.sendLoginOtp(username);
                }
                return otpService.sendRegistrationOtp(username, registrationEmail);
            }

            @Override
            protected void done() {
                setWaitCursor(false);
                setButtonsEnabled(true);
                try {
                    if (Boolean.TRUE.equals(get())) {
                        setStatus("OTP sent to your email.", UIConstants.SUCCESS_COLOR);
                        if (isResend) {
                            JOptionPane.showMessageDialog(OtpVerificationDialog.this,
                                    "A new OTP has been sent to your email.",
                                    "OTP Sent", JOptionPane.INFORMATION_MESSAGE);
                        }
                        startResendCooldown();
                        otpField.requestFocus();
                    } else {
                        String msg = "Could not send OTP. Check your email address or try again later.";
                        setStatus(msg, UIConstants.ERROR_COLOR);
                        JOptionPane.showMessageDialog(OtpVerificationDialog.this,
                                msg, "OTP Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    logger.warn("OTP send failed", ex);
                    String msg = "Failed to send OTP. Check the server connection and try again.";
                    setStatus(msg, UIConstants.ERROR_COLOR);
                    JOptionPane.showMessageDialog(OtpVerificationDialog.this,
                            msg, "OTP Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void handleValidationFailure(OtpValidationResult result) {
        String status = result != null ? result.getStatus() : OtpValidationResult.STATUS_INVALID;
        String dialogMsg;
        String statusMsg;
        boolean forceClose = false;

        switch (status) {
            case OtpValidationResult.STATUS_EXPIRED -> {
                dialogMsg = "OTP expired. Please login again.";
                statusMsg = dialogMsg;
                forceClose = mode == Mode.LOGIN;
            }
            case OtpValidationResult.STATUS_MAX_ATTEMPTS -> {
                dialogMsg = "Maximum OTP attempts exceeded. Please login again.";
                statusMsg = dialogMsg;
                forceClose = mode == Mode.LOGIN;
            }
            case OtpValidationResult.STATUS_NOT_FOUND -> {
                dialogMsg = "No active OTP found. Request a new code or login again.";
                statusMsg = dialogMsg;
                forceClose = mode == Mode.LOGIN;
            }
            default -> {
                int remaining = result != null ? result.getRemainingAttempts() : 0;
                if (remaining > 0) {
                    dialogMsg = "Incorrect OTP. " + remaining + " attempt(s) remaining.";
                    statusMsg = dialogMsg;
                } else {
                    dialogMsg = "Incorrect OTP. Request a new code if needed.";
                    statusMsg = dialogMsg;
                }
            }
        }

        setStatus(statusMsg, UIConstants.ERROR_COLOR);
        JOptionPane.showMessageDialog(this, dialogMsg, "Verification Failed", JOptionPane.ERROR_MESSAGE);
        otpField.selectAll();
        otpField.requestFocus();

        if (forceClose) {
            verified = false;
            stopResendCooldown();
            dispose();
        }
    }

    private void verifyOtp() {
        String entered = otpField.getText().trim();
        if (entered.isEmpty()) {
            setStatus("Please enter the OTP", UIConstants.WARNING_COLOR);
            JOptionPane.showMessageDialog(this,
                    "Please enter the 6-digit OTP from your email.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (otpService == null) {
            setStatus("OTP service unavailable", UIConstants.ERROR_COLOR);
            return;
        }

        setStatus("Verifying…", UIConstants.INFO_COLOR);
        setButtonsEnabled(false);
        setWaitCursor(true);

        new SwingWorker<OtpValidationResult, Void>() {
            @Override
            protected OtpValidationResult doInBackground() throws Exception {
                return otpService.validateOtp(username, entered);
            }

            @Override
            protected void done() {
                setWaitCursor(false);
                setButtonsEnabled(true);
                try {
                    OtpValidationResult result = get();
                    if (result != null && result.isValid()) {
                        verified = true;
                        stopResendCooldown();
                        dispose();
                        return;
                    }
                    handleValidationFailure(result);
                } catch (Exception ex) {
                    logger.warn("OTP verification request failed", ex);
                    String msg = "Could not verify OTP. Check the server connection and try again.";
                    setStatus(msg, UIConstants.ERROR_COLOR);
                    JOptionPane.showMessageDialog(OtpVerificationDialog.this,
                            msg, "Verification Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void cancel() {
        verified = false;
        stopResendCooldown();
        dispose();
    }

    private void setButtonsEnabled(boolean enabled) {
        verifyBtn.setEnabled(enabled);
        cancelBtn.setEnabled(enabled);
        resendBtn.setEnabled(enabled && resendCooldownRemaining <= 0);
    }

    private void startResendCooldown() {
        resendCooldownRemaining = RESEND_COOLDOWN_SEC;
        resendBtn.setEnabled(false);
        if (resendCooldownTimer != null) {
            resendCooldownTimer.stop();
        }
        resendCooldownTimer = new Timer(1000, e -> {
            resendCooldownRemaining--;
            if (resendCooldownRemaining <= 0) {
                resendBtn.setText("Resend OTP");
                resendBtn.setEnabled(true);
                resendCooldownTimer.stop();
            } else {
                resendBtn.setText("Resend (" + resendCooldownRemaining + "s)");
            }
        });
        resendBtn.setText("Resend (" + resendCooldownRemaining + "s)");
        resendCooldownTimer.start();
    }

    private void stopResendCooldown() {
        if (resendCooldownTimer != null) {
            resendCooldownTimer.stop();
        }
        resendCooldownRemaining = 0;
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private void setWaitCursor(boolean waiting) {
        Cursor cursor = Cursor.getPredefinedCursor(waiting ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR);
        setCursor(cursor);
        if (ownerFrame != null) {
            ownerFrame.setCursor(cursor);
        }
    }
}
