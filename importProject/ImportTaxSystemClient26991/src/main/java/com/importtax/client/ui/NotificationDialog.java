package com.importtax.client.ui;

import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.Notification;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class NotificationDialog extends JDialog {

    private final Notification editingNotification;
    private JTextArea messageArea;
    private JTextField recipientField;
    private JTextField sentAtField;
    private JComboBox<String> typeBox;
    private JComboBox<String> statusBox;
    private JLabel statusLabel;
    private RoundedButton saveButton;
    private RoundedButton cancelButton;
    private SaveAction saveAction;

    public NotificationDialog(JFrame owner, Notification notification) {
        super(owner, notification == null ? "New Notification" : "Edit Notification", true);
        this.editingNotification = notification;
        initialize(owner);
        setContentPane(createContent());
        populate();
    }

    public void setSaveAction(SaveAction saveAction) {
        this.saveAction = saveAction;
    }

    public void setLoading(boolean loading, String message) {
        setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        saveButton.setEnabled(!loading);
        cancelButton.setEnabled(!loading);
        statusLabel.setText(message);
        statusLabel.setForeground(loading ? UIConstants.INFO_COLOR : UIConstants.TEXT_SECONDARY);
    }

    public void showErrorDialog(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        JOptionPane.showMessageDialog(this, message, "Notification Error", JOptionPane.ERROR_MESSAGE);
    }

    private void initialize(JFrame owner) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 480);
        setMinimumSize(new Dimension(520, 440));
        setLocationRelativeTo(owner);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        RoundedPanel form = new RoundedPanel(8, 8, new Color(34, 44, 56));
        form.setLayout(new MigLayout("insets 24, fillx", "[120!][grow,fill]", ""));

        messageArea = new JTextArea();
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(UIConstants.FONT_REGULAR);
        messageArea.setBackground(UIConstants.PANEL_COLOR);
        messageArea.setForeground(UIConstants.TEXT_COLOR);
        messageArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        recipientField = field();
        sentAtField = field();
        sentAtField.setText(LocalDate.now().toString());
        typeBox = new JComboBox<>(new String[]{"OTP", "PAYMENT", "GENERAL", "ALERT"});
        statusBox = new JComboBox<>(new String[]{"PENDING", "SENT", "FAILED"});

        addRow(form, "Message", new JScrollPane(messageArea), "h 120!");
        addRow(form, "Recipient", recipientField, "h 40!");
        addRow(form, "Sent Date", sentAtField, "h 40!");
        addRow(form, "Type", typeBox, "h 40!");
        addRow(form, "Status", statusBox, "h 40!");

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(statusLabel, "span, wrap");
        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new MigLayout("insets 16 0 0 0, fillx", "[grow][110!][110!]", ""));
        buttons.setOpaque(false);
        cancelButton = button("Cancel", UIConstants.PRIMARY_DARK);
        cancelButton.addActionListener(e -> dispose());
        saveButton = button("Save", UIConstants.SUCCESS_COLOR);
        saveButton.addActionListener(e -> save());
        buttons.add(new JLabel(), "grow");
        buttons.add(cancelButton, "h 42!");
        buttons.add(saveButton, "h 42!");
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    private void addRow(JPanel panel, String label, java.awt.Component component, String constraints) {
        JLabel rowLabel = new JLabel(label);
        rowLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rowLabel.setForeground(UIConstants.TEXT_COLOR);
        panel.add(rowLabel, "aligny top");
        panel.add(component, constraints + ", wrap, gapbottom 8");
    }

    private JTextField field() {
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setBackground(UIConstants.PANEL_COLOR);
        field.setForeground(UIConstants.TEXT_COLOR);
        field.setCaretColor(UIConstants.TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private RoundedButton button(String text, Color color) {
        RoundedButton button = new RoundedButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setStateColors(color, color.brighter(), color.darker());
        return button;
    }

    private void populate() {
        if (editingNotification == null) {
            return;
        }
        messageArea.setText(editingNotification.getMessage());
        recipientField.setText(editingNotification.getRecipient());
        sentAtField.setText(editingNotification.getSentAt() == null ? "" : editingNotification.getSentAt().toString());
        typeBox.setSelectedItem(editingNotification.getNotificationType());
        statusBox.setSelectedItem(editingNotification.getStatus());
    }

    private void save() {
        try {
            Notification notification = new Notification();
            if (editingNotification != null) {
                notification.setNotificationId(editingNotification.getNotificationId());
            }
            notification.setMessage(required(messageArea.getText(), "Message"));
            notification.setRecipient(required(recipientField.getText(), "Recipient"));
            notification.setSentAt(parseDate(sentAtField.getText()));
            notification.setNotificationType(typeBox.getSelectedItem().toString());
            notification.setStatus(statusBox.getSelectedItem().toString());
            saveAction.save(this, notification);
        } catch (IllegalArgumentException ex) {
            showErrorDialog(ex.getMessage());
        }
    }

    private String required(String value, String label) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return trimmed;
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(required(value, "Sent date"));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Sent date must use yyyy-MM-dd format");
        }
    }

    @FunctionalInterface
    public interface SaveAction {
        void save(NotificationDialog dialog, Notification notification);
    }
}
