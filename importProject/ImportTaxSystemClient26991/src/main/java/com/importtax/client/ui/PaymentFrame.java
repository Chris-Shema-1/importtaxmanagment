package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.CsvExporter;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.Payment;
import com.importtax.server.rmi.InvoiceService;
import com.importtax.server.rmi.PaymentService;
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
import java.util.stream.Collectors;

public class PaymentFrame extends JFrame {

    private PaymentService paymentService;
    private InvoiceService invoiceService;
    private final List<Payment> allPayments = new ArrayList<>();
    private final List<Payment> displayedPayments = new ArrayList<>();
    private List<Invoice> selectableInvoices = new ArrayList<>();
    private JTextField searchField;
    private JLabel statusLabel;
    private JTable table;
    private DefaultTableModel model;
    private RoundedButton addButton;
    private RoundedButton editButton;
    private RoundedButton deleteButton;
    private RoundedButton exportButton;

    public PaymentFrame() {
        initializeServices();
        initializeFrame();
        setContentPane(createContent());
        loadData();
    }

    private void initializeServices() {
        try {
            RmiConnection.initialize();
            paymentService = RmiConnection.lookup(UIConstants.RMI_SERVICE_PAYMENT);
            invoiceService = RmiConnection.lookup(UIConstants.RMI_SERVICE_INVOICE);
        } catch (RemoteException | NotBoundException ex) {
            paymentService = null;
            invoiceService = null;
        }
    }

    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " - Payments");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new Dimension(1020, 640));
        setLocationRelativeTo(null);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][pref]", ""));
        header.setOpaque(false);
        JLabel title = new JLabel("Payments");
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
        addButton = action("Add", UIConstants.SUCCESS_COLOR);
        addButton.addActionListener(e -> openDialog(null));
        editButton = action("Edit", UIConstants.PRIMARY_COLOR);
        editButton.addActionListener(e -> openDialog(getSelected()));
        deleteButton = action("Delete", UIConstants.ERROR_COLOR);
        deleteButton.addActionListener(e -> deleteSelected());
        exportButton = action("Export", UIConstants.ACCENT_COLOR);
        exportButton.addActionListener(e -> exportCsv());
        toolbar.add(addButton, "h 40!");
        toolbar.add(editButton, "h 40!");
        toolbar.add(deleteButton, "h 40!");
        toolbar.add(exportButton, "h 40!, wrap");
        panel.add(toolbar, "grow, wrap, gapbottom 10");

        model = new DefaultTableModel(new Object[]{"ID", "Invoice No.", "Amount", "Date", "Method", "Status"}, 0) {
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
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.setDefaultRenderer(String.class, new AlternatingRowRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        javax.swing.table.JTableHeader payHeader = table.getTableHeader();
        payHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        payHeader.setBackground(new Color(35, 35, 45));
        payHeader.setForeground(UIConstants.TEXT_COLOR);
        payHeader.setReorderingAllowed(false);
        panel.add(new JScrollPane(table), "grow, wrap");

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(statusLabel, "growx");
        root.add(panel, BorderLayout.CENTER);
        return root;
    }

    private RoundedButton action(String text, Color color) {
        RoundedButton button = new RoundedButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setStateColors(color, color.brighter(), color.darker());
        return button;
    }

    private void loadData() {
        if (!ensureService()) {
            return;
        }
        setLoading(true, "Loading payments...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                allPayments.clear();
                allPayments.addAll(paymentService.findAll());
                List<Invoice> allInvoices = invoiceService.findAll();
                selectableInvoices = allInvoices.stream()
                        .filter(invoice -> invoice.getPayment() == null)
                        .collect(Collectors.toCollection(ArrayList::new));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    filter();
                    setLoading(false, allPayments.size() + " payment(s) loaded");
                } catch (Exception ex) {
                    showError(extract(ex, "Could not load payments"));
                }
            }
        }.execute();
    }

    private void filter() {
        String term = searchField.getText().trim().toLowerCase(Locale.ROOT);
        displayedPayments.clear();
        model.setRowCount(0);
        for (Payment payment : allPayments) {
            if (!term.isEmpty()
                    && !safe(payment.getInvoice() == null ? null : payment.getInvoice().getInvoiceNumber()).contains(term)
                    && !safe(payment.getPaymentMethod()).contains(term)
                    && !safe(payment.getPaymentStatus()).contains(term)) {
                continue;
            }
            displayedPayments.add(payment);
            model.addRow(new Object[]{
                    payment.getPaymentId(),
                    payment.getInvoice() == null ? "" : payment.getInvoice().getInvoiceNumber(),
                    payment.getAmountPaid(),
                    payment.getPaymentDate(),
                    payment.getPaymentMethod(),
                    payment.getPaymentStatus()
            });
        }
    }

    private void openDialog(Payment payment) {
        List<Invoice> dialogInvoices = new ArrayList<>(selectableInvoices);
        if (payment != null && payment.getInvoice() != null) {
            dialogInvoices.add(payment.getInvoice());
        }
        dialogInvoices = dialogInvoices.stream().distinct().toList();
        if (dialogInvoices.isEmpty() && payment == null) {
            showError("No unpaid invoices are available. Create an invoice first.");
            return;
        }
        PaymentDialog dialog = new PaymentDialog(this, payment, dialogInvoices);
        dialog.setSaveAction((source, payload) -> mutate(source,
                payload.getPaymentId() == null ? "Saving payment..." : "Updating payment...",
                () -> payload.getPaymentId() == null ? paymentService.save(payload) : paymentService.update(payload),
                payload.getPaymentId() == null ? "Payment saved" : "Payment updated"));
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        Payment selected = getSelected();
        if (selected == null) {
            showError("Select a payment to delete");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete selected payment?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        mutate(null, "Deleting payment...", () -> {
            paymentService.delete(selected);
            return null;
        }, "Payment deleted");
    }

    private void exportCsv() {
        File file = CsvExporter.chooseTargetFile(this, "payments.csv");
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

    private void mutate(PaymentDialog dialog, String loadingMessage, RemoteWork work, String successMessage) {
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
                    loadData();
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

    private Payment getSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= displayedPayments.size()) {
            return null;
        }
        return displayedPayments.get(table.convertRowIndexToModel(row));
    }

    private boolean ensureService() {
        if (paymentService == null || invoiceService == null) {
            showError("Payment services are unavailable. Start the RMI server and try again.");
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
        JOptionPane.showMessageDialog(this, message, "Payment Management", JOptionPane.ERROR_MESSAGE);
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

    private static class AlternatingRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private static final Color EVEN = UIConstants.PANEL_COLOR;
        private static final Color ODD  = new Color(38, 50, 62);

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            if (isSelected) {
                setBackground(UIConstants.PRIMARY_COLOR);
                setForeground(Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? EVEN : ODD);
                setForeground(UIConstants.TEXT_COLOR);
            }
            return this;
        }
    }

    private static class StatusCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(CENTER);
            String status = value == null ? "" : value.toString().toUpperCase(Locale.ROOT);
            if (isSelected) {
                setBackground(UIConstants.PRIMARY_COLOR);
                setForeground(Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? UIConstants.PANEL_COLOR : new Color(38, 50, 62));
                setForeground(switch (status) {
                    case "PAID" -> UIConstants.SUCCESS_COLOR;
                    case "FAILED" -> UIConstants.ERROR_COLOR;
                    case "PENDING" -> UIConstants.WARNING_COLOR;
                    default -> UIConstants.TEXT_COLOR;
                });
            }
            return this;
        }
    }
}
