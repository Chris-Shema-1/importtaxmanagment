package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.ImportItem;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.Payment;
import com.importtax.server.rmi.ImportItemService;
import com.importtax.server.rmi.InvoiceService;
import com.importtax.server.rmi.PaymentService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import net.miginfocom.swing.MigLayout;

public class ReportsPage extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ReportsPage.class);

    private final AppShell shell;
    private ImportItemService importService;
    private InvoiceService    invoiceService;
    private PaymentService    paymentService;

    // stat labels
    private JLabel totalImportsVal, pendingImportsVal, clearedImportsVal;
    private JLabel totalInvoicesVal, totalTaxVal;
    private JLabel totalPaymentsVal, completedPaymentsVal, pendingPaymentsVal, totalPaidVal;
    private JLabel statusLabel;

    // chart panels (replaced on each reload)
    private JPanel importChartHolder, paymentChartHolder, invoiceChartHolder;

    // raw data kept for export
    private List<ImportItem> lastImports;
    private List<Invoice>    lastInvoices;
    private List<Payment>    lastPayments;

    public ReportsPage(AppShell shell) {
        this.shell = shell;
        initRmi();
        setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        add(buildContent(), BorderLayout.CENTER);
    }

    public void reload() { loadData(); }

    private void initRmi() {
        try {
            RmiConnection.initialize();
            importService  = RmiConnection.lookup(UIConstants.RMI_SERVICE_IMPORT);
            invoiceService = RmiConnection.lookup(UIConstants.RMI_SERVICE_INVOICE);
            paymentService = RmiConnection.lookup(UIConstants.RMI_SERVICE_PAYMENT);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("Report services unavailable", e);
        }
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new MigLayout("insets 28 28 28 28, fillx", "[grow]", "[][][][][][][][]"));
        root.setBackground(UIConstants.BACKGROUND_COLOR);

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BACKGROUND_COLOR);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // ── Header ──
        JLabel title = new JLabel("Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.TEXT_COLOR);
        JLabel sub = new JLabel("Live summary with charts — exportable to CSV");
        sub.setFont(UIConstants.FONT_REGULAR);
        sub.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel hdr = new JPanel(new MigLayout("insets 0, fillx", "[grow][][][]", "[][]"));
        hdr.setOpaque(false);
        hdr.add(title, "span 4, wrap");
        hdr.add(sub, "grow");

        var refreshBtn = TaxPage.btn("Refresh",       UIConstants.PRIMARY_COLOR);
        var exportBtn  = TaxPage.btn("Export CSV",    UIConstants.SUCCESS_COLOR);
        refreshBtn.addActionListener(e -> loadData());
        exportBtn.addActionListener(e  -> exportCsv());
        hdr.add(refreshBtn, "h 38!, gapleft 8");
        hdr.add(exportBtn,  "h 38!, gapleft 8");
        root.add(hdr, "growx, wrap, gapbottom 24");

        // ── Import stat cards ──
        root.add(sectionLabel("Import Items"), "wrap, gapbottom 10");
        JPanel importCards = new JPanel(new GridLayout(1, 3, 16, 0));
        importCards.setOpaque(false);
        totalImportsVal   = statVal("--");
        pendingImportsVal = statVal("--");
        clearedImportsVal = statVal("--");
        importCards.add(statCard("Total Imports",  totalImportsVal,   UIConstants.PRIMARY_COLOR));
        importCards.add(statCard("Pending",        pendingImportsVal, UIConstants.WARNING_COLOR));
        importCards.add(statCard("Cleared",        clearedImportsVal, UIConstants.SUCCESS_COLOR));
        root.add(importCards, "growx, wrap, gapbottom 12");

        // ── Import chart holder ──
        importChartHolder = new JPanel(new BorderLayout());
        importChartHolder.setOpaque(false);
        importChartHolder.setPreferredSize(new Dimension(0, 260));
        root.add(importChartHolder, "growx, wrap, gapbottom 24");

        // ── Invoice stat cards ──
        root.add(sectionLabel("Invoices"), "wrap, gapbottom 10");
        JPanel invoiceCards = new JPanel(new GridLayout(1, 2, 16, 0));
        invoiceCards.setOpaque(false);
        totalInvoicesVal = statVal("--");
        totalTaxVal      = statVal("--");
        invoiceCards.add(statCard("Total Invoices",  totalInvoicesVal, UIConstants.INFO_COLOR));
        invoiceCards.add(statCard("Total Tax Value", totalTaxVal,      UIConstants.ACCENT_COLOR));
        root.add(invoiceCards, "growx, wrap, gapbottom 12");

        // ── Invoice chart holder ──
        invoiceChartHolder = new JPanel(new BorderLayout());
        invoiceChartHolder.setOpaque(false);
        invoiceChartHolder.setPreferredSize(new Dimension(0, 260));
        root.add(invoiceChartHolder, "growx, wrap, gapbottom 24");

        // ── Payment stat cards ──
        root.add(sectionLabel("Payments"), "wrap, gapbottom 10");
        JPanel payCards = new JPanel(new GridLayout(1, 4, 16, 0));
        payCards.setOpaque(false);
        totalPaymentsVal     = statVal("--");
        completedPaymentsVal = statVal("--");
        pendingPaymentsVal   = statVal("--");
        totalPaidVal         = statVal("--");
        payCards.add(statCard("Total Payments", totalPaymentsVal,     UIConstants.INFO_COLOR));
        payCards.add(statCard("Completed",      completedPaymentsVal, UIConstants.SUCCESS_COLOR));
        payCards.add(statCard("Pending",        pendingPaymentsVal,   UIConstants.WARNING_COLOR));
        payCards.add(statCard("Total Paid",     totalPaidVal,         UIConstants.PRIMARY_COLOR));
        root.add(payCards, "growx, wrap, gapbottom 12");

        // ── Payment chart holder ──
        paymentChartHolder = new JPanel(new BorderLayout());
        paymentChartHolder.setOpaque(false);
        paymentChartHolder.setPreferredSize(new Dimension(0, 260));
        root.add(paymentChartHolder, "growx, wrap, gapbottom 16");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        root.add(statusLabel, "growx");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.BACKGROUND_COLOR);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Data loading ───────────────────────────────────────────────────────
    private void loadData() {
        statusLabel.setText("Loading...");
        statusLabel.setForeground(UIConstants.INFO_COLOR);

        new SwingWorker<Void, Void>() {
            int totalImports, pending, cleared;
            int totalInvoices;
            BigDecimal totalTax = BigDecimal.ZERO;
            int totalPayments, completed, pendingPay;
            BigDecimal totalPaid = BigDecimal.ZERO;

            protected Void doInBackground() throws Exception {
                if (importService != null) {
                    lastImports  = importService.findAllItems();
                    totalImports = lastImports.size();
                    pending  = (int) lastImports.stream().filter(i -> "PENDING".equalsIgnoreCase(i.getStatus())).count();
                    cleared  = (int) lastImports.stream().filter(i -> "CLEARED".equalsIgnoreCase(i.getStatus())).count();
                }
                if (invoiceService != null) {
                    lastInvoices  = invoiceService.findAll();
                    totalInvoices = lastInvoices.size();
                    totalTax = lastInvoices.stream().map(Invoice::getTotalTaxAmount)
                        .filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add);
                }
                if (paymentService != null) {
                    lastPayments  = paymentService.findAll();
                    totalPayments = lastPayments.size();
                    completed  = (int) lastPayments.stream().filter(p -> "COMPLETED".equalsIgnoreCase(p.getPaymentStatus())).count();
                    pendingPay = (int) lastPayments.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getPaymentStatus())).count();
                    totalPaid  = lastPayments.stream()
                        .filter(p -> "COMPLETED".equalsIgnoreCase(p.getPaymentStatus()))
                        .map(Payment::getAmountPaid).filter(a -> a != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                }
                return null;
            }

            protected void done() {
                try {
                    get();
                    totalImportsVal.setText(String.valueOf(totalImports));
                    pendingImportsVal.setText(String.valueOf(pending));
                    clearedImportsVal.setText(String.valueOf(cleared));
                    totalInvoicesVal.setText(String.valueOf(totalInvoices));
                    totalTaxVal.setText("$" + totalTax.toPlainString());
                    totalPaymentsVal.setText(String.valueOf(totalPayments));
                    completedPaymentsVal.setText(String.valueOf(completed));
                    pendingPaymentsVal.setText(String.valueOf(pendingPay));
                    totalPaidVal.setText("$" + totalPaid.toPlainString());

                    // build charts
                    refreshChart(importChartHolder,  buildImportBarChart(pending, cleared, totalImports - pending - cleared));
                    refreshChart(invoiceChartHolder,  buildInvoicePieChart(totalInvoices, totalTax));
                    refreshChart(paymentChartHolder,  buildPaymentBarChart(completed, pendingPay, totalPayments - completed - pendingPay));

                    statusLabel.setText("Last updated just now");
                    statusLabel.setForeground(UIConstants.TEXT_MUTED);
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load report data");
                    statusLabel.setForeground(UIConstants.ERROR_COLOR);
                }
            }
        }.execute();
    }

    // ── Chart builders ─────────────────────────────────────────────────────
    private JFreeChart buildImportBarChart(int pending, int cleared, int other) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        ds.addValue(pending, "Count", "Pending");
        ds.addValue(cleared, "Count", "Cleared");
        ds.addValue(other,   "Count", "Other");

        JFreeChart chart = ChartFactory.createBarChart(
            "Import Items by Status", null, "Count", ds,
            PlotOrientation.VERTICAL, false, true, false);
        styleBarChart(chart, new Color[]{
            UIConstants.WARNING_COLOR, UIConstants.SUCCESS_COLOR, UIConstants.TEXT_MUTED});
        return chart;
    }

    private JFreeChart buildInvoicePieChart(int totalInvoices, BigDecimal totalTax) {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        ds.setValue("Invoices", totalInvoices);
        ds.setValue("Tax Value ($)", totalTax);

        JFreeChart chart = ChartFactory.createPieChart(
            "Invoice Overview", ds, true, true, false);
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(UIConstants.PANEL_COLOR);
        plot.setOutlineVisible(false);
        plot.setSectionPaint("Invoices",     UIConstants.INFO_COLOR);
        plot.setSectionPaint("Tax Value ($)", UIConstants.ACCENT_COLOR);
        chart.setBackgroundPaint(UIConstants.PANEL_COLOR);
        chart.getTitle().setPaint(UIConstants.TEXT_COLOR);
        return chart;
    }

    private JFreeChart buildPaymentBarChart(int completed, int pending, int other) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        ds.addValue(completed, "Count", "Completed");
        ds.addValue(pending,   "Count", "Pending");
        ds.addValue(other,     "Count", "Other");

        JFreeChart chart = ChartFactory.createBarChart(
            "Payments by Status", null, "Count", ds,
            PlotOrientation.VERTICAL, false, true, false);
        styleBarChart(chart, new Color[]{
            UIConstants.SUCCESS_COLOR, UIConstants.WARNING_COLOR, UIConstants.TEXT_MUTED});
        return chart;
    }

    private void styleBarChart(JFreeChart chart, Color[] colors) {
        chart.setBackgroundPaint(UIConstants.PANEL_COLOR);
        chart.getTitle().setPaint(UIConstants.TEXT_COLOR);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(UIConstants.PANEL_COLOR);
        plot.setRangeGridlinePaint(UIConstants.BORDER_COLOR);
        plot.setOutlineVisible(false);
        plot.getDomainAxis().setTickLabelPaint(UIConstants.TEXT_SECONDARY);
        plot.getRangeAxis().setTickLabelPaint(UIConstants.TEXT_SECONDARY);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setMaximumBarWidth(0.15);
        for (int i = 0; i < colors.length; i++)
            renderer.setSeriesPaint(i, colors[i]);
        renderer.setShadowVisible(false);
    }

    private void refreshChart(JPanel holder, JFreeChart chart) {
        holder.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(UIConstants.PANEL_COLOR);
        cp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        cp.setPreferredSize(new Dimension(0, 250));
        holder.add(cp, BorderLayout.CENTER);
        holder.revalidate();
        holder.repaint();
    }

    // ── CSV Export ─────────────────────────────────────────────────────────
    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Report as CSV");
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fc.setSelectedFile(new File("import_tax_report.csv"));
        if (fc.showSaveDialog(shell) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".csv")) file = new File(file.getAbsolutePath() + ".csv");

        try (FileWriter fw = new FileWriter(file)) {
            fw.write("=== IMPORT ITEMS ===\n");
            fw.write("ID,Item Name,Category,Qty,Unit Price,Tax Rate,Total Tax,Importer,Country,Status,Date\n");
            if (lastImports != null) {
                for (ImportItem i : lastImports)
                    fw.write(csv(i.getItemId(), i.getItemName(), i.getCategory(), i.getQuantity(),
                        i.getUnitPrice(), i.getTaxRate(), i.getTotalTax(),
                        i.getImporterName(), i.getCountryOfOrigin(), i.getStatus(), i.getImportDate()));
            }
            fw.write("\n=== INVOICES ===\n");
            fw.write("ID,Invoice Number,Total Tax Amount,Issue Date\n");
            if (lastInvoices != null) {
                for (Invoice i : lastInvoices)
                    fw.write(csv(i.getInvoiceId(), i.getInvoiceNumber(), i.getTotalTaxAmount(), i.getIssueDate()));
            }
            fw.write("\n=== PAYMENTS ===\n");
            fw.write("ID,Amount Paid,Date,Method,Status,Invoice ID\n");
            if (lastPayments != null) {
                for (Payment p : lastPayments) {
                    Long invId = p.getInvoice() != null ? p.getInvoice().getInvoiceId() : null;
                    fw.write(csv(p.getPaymentId(), p.getAmountPaid(), p.getPaymentDate(),
                        p.getPaymentMethod(), p.getPaymentStatus(), invId));
                }
            }
            JOptionPane.showMessageDialog(shell, "Report exported to:\n" + file.getAbsolutePath(),
                "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(shell, "Export failed: " + ex.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csv(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            String v = values[i] == null ? "" : values[i].toString().replace(",", ";");
            sb.append(v);
        }
        sb.append('\n');
        return sb.toString();
    }

    // ── UI helpers ─────────────────────────────────────────────────────────
    private JPanel statCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new MigLayout("insets 20 20 20 20", "[grow]", "[]8[]")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PANEL_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, getHeight() - 4, getWidth(), 4, 0, 0);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.add(valueLabel, "wrap");
        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        card.add(lbl);
        return card;
    }

    private JLabel statVal(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        l.setForeground(UIConstants.TEXT_COLOR);
        return l;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(UIConstants.TEXT_COLOR);
        return l;
    }
}
