package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.CsvExporter;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.ImportItem;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.Notification;
import com.importtax.server.model.Payment;
import com.importtax.server.model.Tax;
import com.importtax.server.rmi.ImportItemService;
import com.importtax.server.rmi.InvoiceService;
import com.importtax.server.rmi.NotificationService;
import com.importtax.server.rmi.PaymentService;
import com.importtax.server.rmi.TaxService;
import net.miginfocom.swing.MigLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.math.BigDecimal;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class ReportsFrame extends JFrame {

    private ImportItemService importItemService;
    private TaxService taxService;
    private InvoiceService invoiceService;
    private PaymentService paymentService;
    private NotificationService notificationService;
    private JLabel importsMetric;
    private JLabel revenueMetric;
    private JLabel paymentMetric;
    private JLabel alertMetric;
    private JLabel statusLabel;
    private JTable summaryTable;
    private DefaultTableModel summaryModel;

    public ReportsFrame() {
        initializeServices();
        initializeFrame();
        setContentPane(createContent());
        loadReportData();
    }

    private void initializeServices() {
        try {
            RmiConnection.initialize();
            importItemService = RmiConnection.lookup(UIConstants.RMI_SERVICE_IMPORT);
            taxService = RmiConnection.lookup(UIConstants.RMI_SERVICE_TAX);
            invoiceService = RmiConnection.lookup(UIConstants.RMI_SERVICE_INVOICE);
            paymentService = RmiConnection.lookup(UIConstants.RMI_SERVICE_PAYMENT);
            notificationService = RmiConnection.lookup(UIConstants.RMI_SERVICE_NOTIFICATION);
        } catch (RemoteException | NotBoundException ex) {
            importItemService = null;
        }
    }

    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " - Reports");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][120!][pref]", ""));
        header.setOpaque(false);
        JLabel title = new JLabel("Operational Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UIConstants.TEXT_COLOR);
        header.add(title);
        RoundedButton export = new RoundedButton("Export");
        export.setStateColors(UIConstants.ACCENT_COLOR,
                UIConstants.ACCENT_COLOR.brighter(), UIConstants.ACCENT_COLOR.darker());
        export.addActionListener(e -> exportCsv());
        RoundedButton back = new RoundedButton("Back to Dashboard");
        back.setStateColors(UIConstants.PRIMARY_DARK,
                UIConstants.PRIMARY_DARK.brighter(), UIConstants.PRIMARY_DARK.darker());
        back.addActionListener(e -> {
            dispose();
            new DashboardFrame(CurrentSession.getUsername()).setVisible(true);
        });
        header.add(export, "h 42!");
        header.add(back, "h 42!");
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new MigLayout("insets 0, fill", "[grow]", "[][grow][]"));
        center.setOpaque(false);

        JPanel cards = new JPanel(new java.awt.GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        importsMetric = new JLabel("--");
        revenueMetric = new JLabel("--");
        paymentMetric = new JLabel("--");
        alertMetric = new JLabel("--");
        cards.add(metricCard("Imports", importsMetric, UIConstants.PRIMARY_COLOR));
        cards.add(metricCard("Revenue", revenueMetric, UIConstants.SUCCESS_COLOR));
        cards.add(metricCard("Payments", paymentMetric, UIConstants.ACCENT_COLOR));
        cards.add(metricCard("Alerts", alertMetric, UIConstants.ERROR_COLOR));
        center.add(cards, "grow, wrap, gapbottom 18");

        RoundedPanel tablePanel = new RoundedPanel(8, 8, new Color(34, 44, 56));
        tablePanel.setLayout(new BorderLayout());
        summaryModel = new DefaultTableModel(new Object[]{"Section", "Metric", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        summaryTable = new JTable(summaryModel);
        summaryTable.setRowHeight(36);
        summaryTable.setBackground(UIConstants.PANEL_COLOR);
        summaryTable.setForeground(UIConstants.TEXT_COLOR);
        summaryTable.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        summaryTable.setSelectionForeground(Color.WHITE);
        tablePanel.add(new JScrollPane(summaryTable), BorderLayout.CENTER);
        center.add(tablePanel, "grow, wrap");

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        center.add(statusLabel, "growx");
        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private RoundedPanel metricCard(String title, JLabel metric, Color accent) {
        RoundedPanel panel = new RoundedPanel(12, 12, new Color(34, 44, 56), accent, 2);
        panel.setLayout(new MigLayout("insets 18", "[grow]", "[][]"));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        metric.setForeground(accent);
        metric.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(titleLabel, "wrap");
        panel.add(metric);
        return panel;
    }

    private void loadReportData() {
        if (importItemService == null) {
            showError("Reporting services are unavailable. Start the RMI server and try again.");
            return;
        }
        setLoading(true, "Compiling reports...");
        new SwingWorker<Void, Void>() {
            List<ImportItem> imports;
            List<Tax> taxes;
            List<Invoice> invoices;
            List<Payment> payments;
            List<Notification> notifications;

            @Override
            protected Void doInBackground() throws Exception {
                imports = importItemService.findAllItems();
                taxes = taxService.findAll();
                invoices = invoiceService.findAll();
                payments = paymentService.findAll();
                notifications = notificationService.findAll();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    render(imports, taxes, invoices, payments, notifications);
                    setLoading(false, "Reports refreshed");
                } catch (Exception ex) {
                    showError(extract(ex, "Could not compile reports"));
                }
            }
        }.execute();
    }

    private void render(List<ImportItem> imports, List<Tax> taxes, List<Invoice> invoices,
                        List<Payment> payments, List<Notification> notifications) {
        BigDecimal importTaxTotal = imports.stream()
                .map(ImportItem::getTotalTax)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidTotal = payments.stream()
                .map(Payment::getAmountPaid)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingPayments = payments.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getPaymentStatus())).count();
        long alerts = notifications.stream().filter(n -> !"SENT".equalsIgnoreCase(n.getStatus())).count();

        importsMetric.setText(String.valueOf(imports.size()));
        revenueMetric.setText(paidTotal.toPlainString());
        paymentMetric.setText(String.valueOf(payments.size()));
        alertMetric.setText(String.valueOf(alerts));

        summaryModel.setRowCount(0);
        addSummary("Imports", "Total import items", imports.size());
        addSummary("Imports", "Accumulated tax", importTaxTotal.toPlainString());
        addSummary("Taxes", "Tax profiles", taxes.size());
        addSummary("Invoices", "Invoices issued", invoices.size());
        addSummary("Payments", "Payments recorded", payments.size());
        addSummary("Payments", "Pending payments", pendingPayments);
        addSummary("Payments", "Revenue collected", paidTotal.toPlainString());
        addSummary("Notifications", "Notifications logged", notifications.size());
        addSummary("Notifications", "Unresolved alerts", alerts);
    }

    private void addSummary(String section, String metric, Object value) {
        summaryModel.addRow(new Object[]{section, metric, value});
    }

    private void exportCsv() {
        File file = CsvExporter.chooseTargetFile(this, "reports-summary.csv");
        if (file == null) {
            return;
        }
        try {
            CsvExporter.exportTable(summaryTable, file);
            statusLabel.setText("Exported to " + file.getName());
            statusLabel.setForeground(UIConstants.SUCCESS_COLOR);
        } catch (Exception ex) {
            showError("Could not export CSV: " + ex.getMessage());
        }
    }

    private void setLoading(boolean loading, String message) {
        setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        statusLabel.setText(message);
        statusLabel.setForeground(loading ? UIConstants.INFO_COLOR : UIConstants.TEXT_SECONDARY);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        JOptionPane.showMessageDialog(this, message, "Reports", JOptionPane.ERROR_MESSAGE);
    }

    private String extract(Exception ex, String fallback) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() == null ? fallback : cause.getMessage();
    }
}
