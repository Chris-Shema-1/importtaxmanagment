package com.importtax.client.ui;

import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.ImportItem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.miginfocom.swing.MigLayout;

public class ImportItemDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final String[] STATUSES = {"PENDING", "CLEARED", "HOLD"};

    private final ImportItem editingItem;

    private JTextField itemNameField;
    private JTextField categoryField;
    private JTextArea descriptionArea;
    private JTextField quantityField;
    private JTextField unitPriceField;
    private JTextField countryField;
    private JTextField importerNameField;
    private JTextField taxRateField;
    private JTextField totalTaxField;
    private JTextField importDateField;
    private JComboBox<String> statusComboBox;
    private JLabel statusLabel;
    private RoundedButton saveButton;
    private RoundedButton cancelButton;
    private RoundedButton resetButton;
    private SaveAction saveAction;

    public ImportItemDialog(JFrame owner) {
        this(owner, null);
    }

    public ImportItemDialog(JFrame owner, ImportItem item) {
        super(owner, item == null ? "Add New Import" : "Edit Import Item", true);
        this.editingItem = item;
        initializeDialog(owner);
        setContentPane(createContentPanel());
        populateForm(item);
    }

    public void setSaveAction(SaveAction saveAction) {
        this.saveAction = saveAction;
    }

    public void setLoading(boolean loading, String message) {
        setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        saveButton.setEnabled(!loading);
        cancelButton.setEnabled(!loading);
        resetButton.setEnabled(!loading);
        statusLabel.setText(message);
        statusLabel.setForeground(loading ? UIConstants.INFO_COLOR : UIConstants.TEXT_SECONDARY);
    }

    public void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
    }

    public void showErrorDialog(String message) {
        showError(message);
        JOptionPane.showMessageDialog(this, message, "Import Item Error", JOptionPane.ERROR_MESSAGE);
    }

    private void initializeDialog(JFrame owner) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(520, 560));
        setSize(new Dimension(620, 720));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
    }

    private JPanel createContentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new MigLayout("insets 20 24 12 24", "[grow]", "[][]"));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));
        JLabel title = new JLabel(editingItem == null ? "Add New Import" : "Edit Import Item");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UIConstants.TEXT_COLOR);
        header.add(title, "wrap");
        JLabel subtitle = new JLabel("Enter item details and save when ready");
        subtitle.setFont(UIConstants.FONT_REGULAR);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        header.add(subtitle);
        root.add(header, BorderLayout.NORTH);

        RoundedPanel form = new RoundedPanel(8, 8, new Color(40, 40, 46));
        form.setLayout(new MigLayout("insets 24 24 28 24, fillx, hidemode 3", "[140!][grow,fill]", ""));

        itemNameField = createTextField();
        categoryField = createTextField();
        descriptionArea = createTextArea();
        quantityField = createTextField();
        unitPriceField = createTextField();
        countryField = createTextField();
        importerNameField = createTextField();
        taxRateField = createTextField();
        totalTaxField = createTextField();
        totalTaxField.setEditable(false);
        totalTaxField.setFocusable(false);
        importDateField = createTextField();
        importDateField.setText(LocalDate.now().toString());
        statusComboBox = new JComboBox<>(STATUSES);
        statusComboBox.setSelectedItem("PENDING");
        statusComboBox.setFont(UIConstants.FONT_REGULAR);
        statusComboBox.setBackground(UIConstants.PANEL_COLOR);
        statusComboBox.setForeground(UIConstants.TEXT_COLOR);
        installTaxPreviewListeners();

        addField(form, "Item Name", itemNameField);
        addField(form, "Category", categoryField);
        addField(form, "Description", new JScrollPane(descriptionArea), "h 90!");
        addField(form, "Quantity", quantityField);
        addField(form, "Unit Price", unitPriceField);
        addField(form, "Country of Origin", countryField);
        addField(form, "Importer Name", importerNameField);
        addField(form, "Tax Rate (%)", taxRateField);
        addField(form, "Total Tax", totalTaxField);
        addField(form, "Import Date", importDateField);
        addField(form, "Status", statusComboBox);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(statusLabel, "span, growx, gapy 8 0");

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScroll.getVerticalScrollBar().setUnitIncrement(18);
        root.add(formScroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new MigLayout("insets 12 24 20 24, fillx", "[grow][110!][110!][110!]", "[]"));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        resetButton = createActionButton("Reset", UIConstants.PRIMARY_DARK);
        resetButton.addActionListener(e -> populateForm(editingItem));
        cancelButton = createActionButton("Cancel", UIConstants.BORDER_COLOR);
        cancelButton.addActionListener(e -> dispose());
        saveButton = createActionButton("Save", UIConstants.SUCCESS_COLOR);
        saveButton.addActionListener(e -> save());
        buttons.add(new JLabel(), "grow");
        buttons.add(resetButton, "h 42!");
        buttons.add(cancelButton, "h 42!");
        buttons.add(saveButton, "h 42!");
        root.add(buttons, BorderLayout.SOUTH);

        return root;
    }

    private void addField(JPanel panel, String labelText, java.awt.Component component) {
        addField(panel, labelText, component, "h 38!");
    }

    private void addField(JPanel panel, String labelText, java.awt.Component component, String constraints) {
        JLabel label = new JLabel(labelText);
        label.setFont(UIConstants.FONT_LABEL);
        label.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(label, "aligny top, gapy 8 0");
        panel.add(component, constraints + ", wrap, gapbottom 4");
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setBackground(UIConstants.PANEL_COLOR);
        field.setForeground(UIConstants.TEXT_COLOR);
        field.setCaretColor(UIConstants.PRIMARY_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIConstants.PRIMARY_COLOR, 2, true),
                    new EmptyBorder(7, 11, 7, 11)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIConstants.BORDER_COLOR, 1, true),
                    new EmptyBorder(8, 12, 8, 12)));
            }
        });
        return field;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UIConstants.FONT_REGULAR);
        area.setBackground(UIConstants.PANEL_COLOR);
        area.setForeground(UIConstants.TEXT_COLOR);
        area.setCaretColor(UIConstants.PRIMARY_COLOR);
        area.setBorder(new EmptyBorder(8, 12, 8, 12));
        return area;
    }

    private RoundedButton createActionButton(String text, Color color) {
        RoundedButton button = new RoundedButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setStateColors(color, color.brighter(), color.darker());
        return button;
    }

    private void populateForm(ImportItem item) {
        if (item == null) {
            itemNameField.setText("");
            categoryField.setText("");
            descriptionArea.setText("");
            quantityField.setText("");
            unitPriceField.setText("");
            countryField.setText("");
            importerNameField.setText("");
            taxRateField.setText("");
            totalTaxField.setText("");
            importDateField.setText(LocalDate.now().toString());
            statusComboBox.setSelectedItem("PENDING");
        } else {
            itemNameField.setText(nullToEmpty(item.getItemName()));
            categoryField.setText(nullToEmpty(item.getCategory()));
            descriptionArea.setText(nullToEmpty(item.getDescription()));
            quantityField.setText(item.getQuantity() == null ? "" : item.getQuantity().toString());
            unitPriceField.setText(item.getUnitPrice() == null ? "" : item.getUnitPrice().toString());
            countryField.setText(nullToEmpty(item.getCountryOfOrigin()));
            importerNameField.setText(nullToEmpty(item.getImporterName()));
            taxRateField.setText(item.getTaxRate() == null ? "" : item.getTaxRate().toString());
            totalTaxField.setText(item.getTotalTax() == null ? "" : item.getTotalTax().toString());
            importDateField.setText(item.getImportDate() == null ? "" : item.getImportDate().toString());
            statusComboBox.setSelectedItem(nullToEmpty(item.getStatus()));
        }
        statusLabel.setText(" ");
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        updateTotalTaxPreview();
    }

    private void save() {
        ImportItem item;
        try {
            item = buildItemFromForm();
        } catch (IllegalArgumentException ex) {
            showValidationError(ex.getMessage());
            return;
        }
        if (saveAction != null) {
            saveAction.save(this, item);
        }
    }

    private ImportItem buildItemFromForm() {
        if (CurrentSession.getLoggedInUserId() == null) {
            throw new IllegalArgumentException("No logged-in user found. Please sign in again.");
        }

        ImportItem item = new ImportItem();
        if (editingItem != null) {
            item.setItemId(editingItem.getItemId());
        }
        item.setItemName(required(itemNameField, "Item Name"));
        item.setCategory(required(categoryField, "Category"));
        item.setDescription(descriptionArea.getText().trim());
        item.setQuantity(parseInteger(quantityField.getText(), "Quantity"));
        item.setUnitPrice(parseDecimal(unitPriceField.getText(), "Unit Price"));
        item.setCountryOfOrigin(countryField.getText().trim());
        item.setImporterName(required(importerNameField, "Importer Name"));
        item.setTaxRate(parseDecimal(taxRateField.getText(), "Tax Rate"));
        item.setImportDate(parseDate(importDateField.getText()));
        item.setStatus((String) statusComboBox.getSelectedItem());
        item.setUser(CurrentSession.getLoggedInUser());
        return item;
    }

    private String required(JTextField field, String name) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    private Integer parseInteger(String value, String name) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(name + " must be greater than 0");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " must be a valid whole number");
        }
    }

    private BigDecimal parseDecimal(String value, String name) {
        try {
            BigDecimal parsed = new BigDecimal(value.trim());
            if (parsed.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(name + " cannot be negative");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " must be a valid number");
        }
    }

    private void installTaxPreviewListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTotalTaxPreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTotalTaxPreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTotalTaxPreview();
            }
        };
        quantityField.getDocument().addDocumentListener(listener);
        unitPriceField.getDocument().addDocumentListener(listener);
        taxRateField.getDocument().addDocumentListener(listener);
    }

    private void updateTotalTaxPreview() {
        try {
            String quantityText = quantityField.getText().trim();
            String unitPriceText = unitPriceField.getText().trim();
            String taxRateText = taxRateField.getText().trim();
            if (quantityText.isEmpty() || unitPriceText.isEmpty() || taxRateText.isEmpty()) {
                totalTaxField.setText("");
                return;
            }

            int quantity = Integer.parseInt(quantityText);
            BigDecimal unitPrice = new BigDecimal(unitPriceText);
            BigDecimal taxRate = new BigDecimal(taxRateText);
            if (quantity <= 0
                    || unitPrice.compareTo(BigDecimal.ZERO) <= 0
                    || taxRate.compareTo(BigDecimal.ZERO) < 0) {
                totalTaxField.setText("");
                return;
            }

            BigDecimal preview = BigDecimal.valueOf(quantity)
                    .multiply(unitPrice)
                    .multiply(taxRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalTaxField.setText(preview.toPlainString());
        } catch (NumberFormatException ex) {
            totalTaxField.setText("");
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Import Date must use yyyy-MM-dd format");
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void showValidationError(String message) {
        showErrorDialog(message);
    }

    @FunctionalInterface
    public interface SaveAction {
        void save(ImportItemDialog dialog, ImportItem item);
    }
}
