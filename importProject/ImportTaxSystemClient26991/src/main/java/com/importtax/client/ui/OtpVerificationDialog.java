package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.UIConstants;
import com.importtax.server.rmi.OtpService;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Modal dialog shown after successful password authentication.
 * Generates an OTP via the server's OtpService (which publishes it through
 * ActiveMQ and simulates email delivery), then asks the user to enter it.
 */
public class OtpVerificationDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(OtpVerificationDialog.class);

    private final String username;
    private OtpService otpService;
    private boolean verified = false;

    private JTextField otpField;
    private JLabel     statusLabel;
    private JButton    verifyBtn;
    private JButton    resendBtn;

    public OtpVerificationDialog(Frame owner, String username) {
        super(owner, "Two-Factor Verification", true);
        this.username = username;
        initRmi();
        buildUi();
        setSize(420, 320);
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // Auto-generate OTP when dialog opens
        generateOtp();
    }

    /** Returns true if the user successfully verified the OTP. */
    public boolean isVerified() { return verified; }

    // ── RMI ────────────────────────────────────────────────────────────────
    private void initRmi() {
        try {
            RmiConnection.initialize();
            otpService = RmiConnection.lookup(UIConstants.RMI_SERVICE_OTP);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("OtpService unavailable", e);
        }
    }

    // ── UI ─────────────────────────────────────────────────────────────────
    private void buildUi() {
        JPanel root = new JPanel(new MigLayout("insets 32 36 24 36, fill", "[grow]", "[][][][]12[][]"));
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

        JLabel sub = new JLabel("<html><center>A 6-digit OTP has been sent to your registered email.<br>Enter it below to complete sign-in.</center></html>");
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
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) verifyOtp();
            }
        });
        root.add(otpField, "growx, h 52!, wrap, gapbottom 4");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(statusLabel, "growx, wrap");

        JPanel btnRow = new JPanel(new MigLayout("insets 0, fillx", "[grow][120!][120!]", "[44!]"));
        btnRow.setOpaque(false);
        resendBtn = TaxPage.btn("Resend OTP", UIConstants.BORDER_COLOR);
        verifyBtn = TaxPage.btn("Verify",     UIConstants.PRIMARY_COLOR);
        resendBtn.addActionListener(e -> generateOtp());
        verifyBtn.addActionListener(e -> verifyOtp());
        btnRow.add(new JLabel(), "grow");
        btnRow.add(resendBtn, "h 44!");
        btnRow.add(verifyBtn, "h 44!, gapleft 8");
        root.add(btnRow, "growx");

        setContentPane(root);
    }

    // ── Logic ───────────────────────────────────────────────────────────────
    private void generateOtp() {
        if (otpService == null) {
            setStatus("OTP service unavailable — check server is running", UIConstants.ERROR_COLOR);
            return;
        }
        setStatus("Sending OTP…", UIConstants.INFO_COLOR);
        verifyBtn.setEnabled(false);
        resendBtn.setEnabled(false);
        new SwingWorker<String, Void>() {
            protected String doInBackground() throws Exception {
                return otpService.generateOtp(username);
            }
            protected void done() {
                verifyBtn.setEnabled(true);
                resendBtn.setEnabled(true);
                try {
                    String otp = get();
                    // In a real system the OTP travels only via email.
                    // For demo purposes we show it in the status so the tester can enter it.
                    setStatus("OTP sent (demo): " + otp, UIConstants.SUCCESS_COLOR);
                    logger.info("OTP generated for username={}", username);
                } catch (Exception ex) {
                    setStatus("Failed to send OTP: " + ex.getMessage(), UIConstants.ERROR_COLOR);
                }
            }
        }.execute();
    }

    private void verifyOtp() {
        String entered = otpField.getText().trim();
        if (entered.isEmpty()) { setStatus("Please enter the OTP", UIConstants.WARNING_COLOR); return; }
        if (otpService == null) { setStatus("OTP service unavailable", UIConstants.ERROR_COLOR); return; }
        setStatus("Verifying…", UIConstants.INFO_COLOR);
        verifyBtn.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {
                return otpService.validateOtp(username, entered);
            }
            protected void done() {
                verifyBtn.setEnabled(true);
                try {
                    if (get()) {
                        verified = true;
                        dispose();
                    } else {
                        setStatus("Invalid or expired OTP — try again or resend", UIConstants.ERROR_COLOR);
                        otpField.selectAll();
                        otpField.requestFocus();
                    }
                } catch (Exception ex) {
                    setStatus("Verification error: " + ex.getMessage(), UIConstants.ERROR_COLOR);
                }
            }
        }.execute();
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
