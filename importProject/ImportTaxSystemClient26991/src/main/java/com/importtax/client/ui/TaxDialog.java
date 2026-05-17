package com.importtax.client.ui;

import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.Tax;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
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
import java.math.BigDecimal;

public class TaxDialog extends JDialog {

    private final Tax editingTax;
    private JTextField taxNameField;
    private JTextField taxRateField;
    private JTextArea descriptionArea;
    private JLabel statusLabel;
    private RoundedButton saveButton;
    private RoundedButton cancelButton;
    private SaveAction saveAction;

    public TaxDialog(JFrame owner, Tax tax) {
        super(owner, tax == null ? "Add Tax" : "Edit Tax", true);
        this.editingTax = tax;
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
        JOptionPane.showMessageDialog(this, message, "Tax Error", JOptionPane.ERROR_MESSAGE);
    }

    private void initialize(JFrame owner) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 460);
        setMinimumSize(new Dimension(480, 420));
        setLocationRelativeTo(owner);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        RoundedPanel form = new RoundedPanel(8, 8, new Color(34, 44, 56));
        form.setLayout(new MigLayout("insets 24, fillx", "[120!][grow,fill]", ""));

        JLabel title = new JLabel(editingTax == null ? "Create Tax Profile" : "Update Tax Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UIConstants.TEXT_COLOR);
        form.add(title, "span, wrap, gapbottom 16");

        taxNameField = createTextField();
        taxRateField = createTextField();
        descriptionArea = new JTextArea();
        descriptionArea.setFont(UIConstants.FONT_REGULAR);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(UIConstants.PANEL_COLOR);
        descriptionArea.setForeground(UIConstants.TEXT_COLOR);
        descriptionArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        addRow(form, "Tax Name", taxNameField, "h 40!");
        addRow(form, "Tax Rate (%)", taxRateField, "h 40!");
        addRow(form, "Description", new JScrollPane(descriptionArea), "h 120!");

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(statusLabel, "span, wrap, gapy 10 0");
        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new MigLayout("insets 16 0 0 0, fillx", "[grow][110!][110!]", ""));
        buttons.setOpaque(false);
        cancelButton = actionButton("Cancel", UIConstants.PRIMARY_DARK);
        cancelButton.addActionListener(e -> dispose());
        saveButton = actionButton("Save", UIConstants.SUCCESS_COLOR);
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

    private JTextField createTextField() {
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

    private RoundedButton actionButton(String text, Color color) {
        RoundedButton button = new RoundedButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setStateColors(color, color.brighter(), color.darker());
        return button;
    }

    private void populate() {
        if (editingTax == null) {
            taxNameField.setText("");
            taxRateField.setText("");
            descriptionArea.setText("");
            return;
        }
        taxNameField.setText(editingTax.getTaxName());
        taxRateField.setText(editingTax.getTaxRate() == null ? "" : editingTax.getTaxRate().toPlainString());
        descriptionArea.setText(editingTax.getDescription() == null ? "" : editingTax.getDescription());
    }

    private void save() {
        try {
            Tax tax = new Tax();
            if (editingTax != null) {
                tax.setTaxId(editingTax.getTaxId());
            }
            tax.setTaxName(required(taxNameField.getText(), "Tax name"));
            tax.setTaxRate(parseRate(taxRateField.getText()));
            tax.setDescription(descriptionArea.getText().trim());
            saveAction.save(this, tax);
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

    private BigDecimal parseRate(String value) {
        try {
            BigDecimal rate = new BigDecimal(required(value, "Tax rate"));
            if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Tax rate must be greater than 0");
            }
            return rate;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Tax rate must be a valid number");
        }
    }

    @FunctionalInterface
    public interface SaveAction {
        void save(TaxDialog dialog, Tax tax);
    }
}
