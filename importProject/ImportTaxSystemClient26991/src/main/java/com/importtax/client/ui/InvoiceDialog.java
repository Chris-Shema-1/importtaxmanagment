package com.importtax.client.ui;

import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.Invoice;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
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

public class InvoiceDialog extends JDialog {

    private final Invoice editingInvoice;
    private JTextField invoiceNumberField;
    private JTextField totalAmountField;
    private JTextField issueDateField;
    private JLabel statusLabel;
    private RoundedButton saveButton;
    private RoundedButton cancelButton;
    private SaveAction saveAction;

    public InvoiceDialog(JFrame owner, Invoice invoice) {
        super(owner, invoice == null ? "Add Invoice" : "Edit Invoice", true);
        this.editingInvoice = invoice;
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
        JOptionPane.showMessageDialog(this, message, "Invoice Error", JOptionPane.ERROR_MESSAGE);
    }

    private void initialize(JFrame owner) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 340);
        setMinimumSize(new Dimension(480, 320));
        setLocationRelativeTo(owner);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        RoundedPanel form = new RoundedPanel(8, 8, new Color(34, 44, 56));
        form.setLayout(new MigLayout("insets 24, fillx", "[120!][grow,fill]", ""));

        JLabel title = new JLabel(editingInvoice == null ? "Create Invoice" : "Update Invoice");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UIConstants.TEXT_COLOR);
        form.add(title, "span, wrap, gapbottom 16");

        invoiceNumberField = createField();
        totalAmountField = createField();
        issueDateField = createField();
        issueDateField.setText(LocalDate.now().toString());
        addRow(form, "Invoice No.", invoiceNumberField);
        addRow(form, "Total Amount", totalAmountField);
        addRow(form, "Issue Date", issueDateField);

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

    private JTextField createField() {
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
        if (editingInvoice == null) {
            return;
        }
        invoiceNumberField.setText(editingInvoice.getInvoiceNumber());
        totalAmountField.setText(editingInvoice.getTotalTaxAmount() == null ? "" : editingInvoice.getTotalTaxAmount().toPlainString());
        issueDateField.setText(editingInvoice.getIssueDate() == null ? "" : editingInvoice.getIssueDate().toString());
    }

    private void save() {
        try {
            Invoice invoice = new Invoice();
            if (editingInvoice != null) {
                invoice.setInvoiceId(editingInvoice.getInvoiceId());
            }
            invoice.setInvoiceNumber(required(invoiceNumberField.getText(), "Invoice number"));
            invoice.setTotalTaxAmount(parseAmount(totalAmountField.getText()));
            invoice.setIssueDate(parseDate(issueDateField.getText()));
            saveAction.save(this, invoice);
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

    private BigDecimal parseAmount(String value) {
        try {
            BigDecimal amount = new BigDecimal(required(value, "Total amount"));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Total amount must be greater than 0");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Total amount must be numeric");
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(required(value, "Issue date"));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Issue date must use yyyy-MM-dd format");
        }
    }

    @FunctionalInterface
    public interface SaveAction {
        void save(InvoiceDialog dialog, Invoice invoice);
    }
}
