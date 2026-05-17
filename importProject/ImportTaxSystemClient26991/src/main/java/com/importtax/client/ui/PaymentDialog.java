package com.importtax.client.ui;

import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.Payment;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PaymentDialog extends JDialog {

    private final Payment editingPayment;
    private final List<Invoice> invoices;
    private JComboBox<InvoiceOption> invoiceBox;
    private JTextField amountField;
    private JTextField dateField;
    private JTextField methodField;
    private JComboBox<String> statusBox;
    private JLabel statusLabel;
    private RoundedButton saveButton;
    private RoundedButton cancelButton;
    private SaveAction saveAction;

    public PaymentDialog(JFrame owner, Payment payment, List<Invoice> invoices) {
        super(owner, payment == null ? "Record Payment" : "Edit Payment", true);
        this.editingPayment = payment;
        this.invoices = invoices;
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
        JOptionPane.showMessageDialog(this, message, "Payment Error", JOptionPane.ERROR_MESSAGE);
    }

    private void initialize(JFrame owner) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 380);
        setMinimumSize(new Dimension(520, 340));
        setLocationRelativeTo(owner);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        RoundedPanel form = new RoundedPanel(8, 8, new Color(34, 44, 56));
        form.setLayout(new MigLayout("insets 24, fillx", "[120!][grow,fill]", ""));

        invoiceBox = new JComboBox<>();
        for (Invoice invoice : invoices) {
            invoiceBox.addItem(new InvoiceOption(invoice));
        }
        amountField = field();
        dateField = field();
        dateField.setText(LocalDate.now().toString());
        methodField = field();
        statusBox = new JComboBox<>(new String[]{"PENDING", "PAID", "FAILED"});

        addRow(form, "Invoice", invoiceBox);
        addRow(form, "Amount Paid", amountField);
        addRow(form, "Payment Date", dateField);
        addRow(form, "Method", methodField);
        addRow(form, "Status", statusBox);

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

    private void addRow(JPanel panel, String label, java.awt.Component component) {
        JLabel rowLabel = new JLabel(label);
        rowLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rowLabel.setForeground(UIConstants.TEXT_COLOR);
        panel.add(rowLabel);
        panel.add(component, "h 40!, wrap, gapbottom 8");
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
        if (editingPayment == null) {
            return;
        }
        amountField.setText(editingPayment.getAmountPaid() == null ? "" : editingPayment.getAmountPaid().toPlainString());
        dateField.setText(editingPayment.getPaymentDate() == null ? "" : editingPayment.getPaymentDate().toString());
        methodField.setText(editingPayment.getPaymentMethod());
        statusBox.setSelectedItem(editingPayment.getPaymentStatus());
        if (editingPayment.getInvoice() != null) {
            selectInvoice(editingPayment.getInvoice().getInvoiceId());
        }
    }

    private void save() {
        try {
            Payment payment = new Payment();
            if (editingPayment != null) {
                payment.setPaymentId(editingPayment.getPaymentId());
            }
            InvoiceOption option = (InvoiceOption) invoiceBox.getSelectedItem();
            if (option == null) {
                throw new IllegalArgumentException("Please select an invoice");
            }
            payment.setInvoice(option.invoice());
            payment.setAmountPaid(parseAmount(amountField.getText()));
            payment.setPaymentDate(parseDate(dateField.getText()));
            payment.setPaymentMethod(required(methodField.getText(), "Payment method"));
            payment.setPaymentStatus(statusBox.getSelectedItem().toString());
            saveAction.save(this, payment);
        } catch (IllegalArgumentException ex) {
            showErrorDialog(ex.getMessage());
        }
    }

    private void selectInvoice(Long invoiceId) {
        for (int i = 0; i < invoiceBox.getItemCount(); i++) {
            InvoiceOption option = invoiceBox.getItemAt(i);
            if (option.invoice().getInvoiceId().equals(invoiceId)) {
                invoiceBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private String required(String value, String label) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return trimmed;
    }

    private BigDecimal parseAmount(String value) {
        try {
            BigDecimal amount = new BigDecimal(required(value, "Amount"));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than 0");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Amount must be numeric");
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(required(value, "Payment date"));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Payment date must use yyyy-MM-dd format");
        }
    }

    private record InvoiceOption(Invoice invoice) {
        @Override
        public String toString() {
            return invoice.getInvoiceNumber() + "  |  " + invoice.getTotalTaxAmount();
        }
    }

    @FunctionalInterface
    public interface SaveAction {
        void save(PaymentDialog dialog, Payment payment);
    }
}
