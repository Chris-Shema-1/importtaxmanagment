package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.CsvExporter;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.Invoice;
import com.importtax.server.rmi.InvoiceService;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvoiceFrame extends JFrame {

    private InvoiceService invoiceService;
    private final List<Invoice> allInvoices = new ArrayList<>();
    private final List<Invoice> displayedInvoices = new ArrayList<>();
    private JTextField searchField;
    private JLabel statusLabel;
    private JTable table;
    private DefaultTableModel model;
    private RoundedButton addButton;
    private RoundedButton editButton;
    private RoundedButton deleteButton;
    private RoundedButton exportButton;

    public InvoiceFrame() {
        initializeService();
        initializeFrame();
        setContentPane(createContent());
        loadInvoices();
    }

    private void initializeService() {
        try {
            RmiConnection.initialize();
            invoiceService = RmiConnection.lookup(UIConstants.RMI_SERVICE_INVOICE);
        } catch (RemoteException | NotBoundException ex) {
            invoiceService = null;
        }
    }

    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " - Invoice Management");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][pref]", ""));
        header.setOpaque(false);
        JLabel title = new JLabel("Invoices");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UIConstants.TEXT_COLOR);
        header.add(title);
        RoundedButton back = new RoundedButton("Back to Dashboard");
        back.setStateColors(UIConstants.PRIMARY_DARK,
                UIConstants.PRIMARY_DARK.brighter(), UIConstants.PRIMARY_DARK.darker());
        back.addActionListener(e -> {
            dispose();
            new DashboardFrame(CurrentSession.getUsername()).setVisible(true);
        });
        header.add(back, "h 42!");
        root.add(header, BorderLayout.NORTH);

        RoundedPanel panel = new RoundedPanel(8, 8, new Color(34, 44, 56));
        panel.setLayout(new MigLayout("insets 18, fill", "[grow]", "[][grow][]"));
        JPanel toolbar = new JPanel(new MigLayout("insets 0, fillx", "[grow][110!][110!][110!][110!]", ""));
        toolbar.setOpaque(false);
        searchField = new JTextField();
        searchField.setFont(UIConstants.FONT_REGULAR);
        searchField.setBackground(UIConstants.PANEL_COLOR);
        searchField.setForeground(UIConstants.TEXT_COLOR);
        searchField.setCaretColor(UIConstants.TEXT_COLOR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        searchField.addActionListener(e -> filter());
        toolbar.add(searchField, "grow, h 40!");
        addButton = actionButton("Add", UIConstants.SUCCESS_COLOR);
        addButton.addActionListener(e -> openDialog(null));
        editButton = actionButton("Edit", UIConstants.PRIMARY_COLOR);
        editButton.addActionListener(e -> openDialog(getSelected()));
        deleteButton = actionButton("Delete", UIConstants.ERROR_COLOR);
        deleteButton.addActionListener(e -> deleteSelected());
        exportButton = actionButton("Export", UIConstants.ACCENT_COLOR);
        exportButton.addActionListener(e -> exportCsv());
        toolbar.add(addButton, "h 40!");
        toolbar.add(editButton, "h 40!");
        toolbar.add(deleteButton, "h 40!");
        toolbar.add(exportButton, "h 40!, wrap");
        panel.add(toolbar, "grow, wrap, gapbottom 10");

        model = new DefaultTableModel(new Object[]{"ID", "Invoice No.", "Amount", "Issue Date", "Payment Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(38);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(UIConstants.PANEL_COLOR);
        table.setForeground(UIConstants.TEXT_COLOR);
        table.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        panel.add(new JScrollPane(table), "grow, wrap");

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(statusLabel, "growx");
        root.add(panel, BorderLayout.CENTER);
        return root;
    }

    private RoundedButton actionButton(String text, Color color) {
        RoundedButton button = new RoundedButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setStateColors(color, color.brighter(), color.darker());
        return button;
    }

    private void loadInvoices() {
        if (!ensureService()) {
            return;
        }
        setLoading(true, "Loading invoices...");
        new SwingWorker<List<Invoice>, Void>() {
            @Override
            protected List<Invoice> doInBackground() throws Exception {
                return invoiceService.findAll();
            }

            @Override
            protected void done() {
                try {
                    allInvoices.clear();
                    allInvoices.addAll(get());
                    filter();
                    setLoading(false, allInvoices.size() + " invoice(s) loaded");
                } catch (Exception ex) {
                    showError(extract(ex, "Could not load invoices"));
                }
            }
        }.execute();
    }

    private void filter() {
        String term = searchField.getText().trim().toLowerCase(Locale.ROOT);
        displayedInvoices.clear();
        model.setRowCount(0);
        for (Invoice invoice : allInvoices) {
            if (!term.isEmpty()
                    && !safe(invoice.getInvoiceNumber()).contains(term)
                    && !safe(invoice.getIssueDate() == null ? null : invoice.getIssueDate().toString()).contains(term)) {
                continue;
            }
            displayedInvoices.add(invoice);
            model.addRow(new Object[]{
                    invoice.getInvoiceId(),
                    invoice.getInvoiceNumber(),
                    invoice.getTotalTaxAmount(),
                    invoice.getIssueDate(),
                    invoice.getPayment() == null ? "UNPAID" : "PAID"
            });
        }
    }

    private void openDialog(Invoice invoice) {
        InvoiceDialog dialog = new InvoiceDialog(this, invoice);
        dialog.setSaveAction((source, payload) -> mutate(source,
                payload.getInvoiceId() == null ? "Saving invoice..." : "Updating invoice...",
                () -> payload.getInvoiceId() == null ? invoiceService.save(payload) : invoiceService.update(payload),
                payload.getInvoiceId() == null ? "Invoice saved" : "Invoice updated"));
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        Invoice selected = getSelected();
        if (selected == null) {
            showError("Select an invoice to delete");
            return;
        }
        if (selected.getPayment() != null) {
            showError("Paid invoices cannot be deleted");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete selected invoice?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        mutate(null, "Deleting invoice...", () -> {
            invoiceService.delete(selected);
            return null;
        }, "Invoice deleted");
    }

    private void exportCsv() {
        File file = CsvExporter.chooseTargetFile(this, "invoices.csv");
        if (file == null) {
            return;
        }
        try {
            CsvExporter.exportTable(table, file);
            statusLabel.setText("Exported to " + file.getName());
            statusLabel.setForeground(UIConstants.SUCCESS_COLOR);
        } catch (Exception ex) {
            showError("Could not export CSV: " + ex.getMessage());
        }
    }

    private void mutate(InvoiceDialog dialog, String loadingMessage, RemoteWork work, String successMessage) {
        if (!ensureService()) {
            return;
        }
        setLoading(true, loadingMessage);
        if (dialog != null) {
            dialog.setLoading(true, loadingMessage);
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                work.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (dialog != null) {
                        dialog.dispose();
                    }
                    loadInvoices();
                } catch (Exception ex) {
                    String message = extract(ex, successMessage + " failed");
                    if (dialog != null) {
                        dialog.setLoading(false, " ");
                        dialog.showErrorDialog(message);
                    } else {
                        showError(message);
                    }
                }
            }
        }.execute();
    }

    private Invoice getSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= displayedInvoices.size()) {
            return null;
        }
        return displayedInvoices.get(table.convertRowIndexToModel(row));
    }

    private boolean ensureService() {
        if (invoiceService == null) {
            showError("Invoice service is unavailable. Start the RMI server and try again.");
            return false;
        }
        return true;
    }

    private void setLoading(boolean loading, String message) {
        setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        addButton.setEnabled(!loading);
        editButton.setEnabled(!loading);
        deleteButton.setEnabled(!loading);
        exportButton.setEnabled(!loading);
        statusLabel.setText(message);
        statusLabel.setForeground(loading ? UIConstants.INFO_COLOR : UIConstants.TEXT_SECONDARY);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        JOptionPane.showMessageDialog(this, message, "Invoice Management", JOptionPane.ERROR_MESSAGE);
    }

    private String extract(Exception ex, String fallback) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() == null ? fallback : cause.getMessage();
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    @FunctionalInterface
    private interface RemoteWork {
        Object run() throws Exception;
    }
}
