package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.TableFormatUtil;
import com.importtax.client.util.UIConstants;
import com.importtax.client.util.UserMessageUtil;
import com.importtax.server.model.ReportSummary;
import com.importtax.server.model.ReportTableRow;
import com.importtax.server.rmi.ReportService;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import net.miginfocom.swing.MigLayout;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ReportsPage extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ReportsPage.class);

    private final AppShell shell;
    private ReportService reportService;

    // stat labels
    private JLabel totalImportsVal, pendingImportsVal, clearedImportsVal;
    private JLabel totalInvoicesVal, totalTaxVal;
    private JLabel totalPaymentsVal, completedPaymentsVal, pendingPaymentsVal, totalPaidVal;
    private JLabel totalsSummaryLabel;
    private JLabel statusLabel;

    // chart panels (replaced on each reload)
    private JPanel importChartHolder, paymentChartHolder, invoiceChartHolder;

    private JTable reportTable;
    private DefaultTableModel reportTableModel;

    private ReportSummary lastSummary;
    private List<ReportTableRow> lastReportRows;
    private boolean dataLoaded;
    private JButton refreshBtn;
    private JButton exportCsvBtn;
    private JButton exportPdfBtn;
    private JLabel lastRefreshedLabel;

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
            reportService = RmiConnection.lookup(UIConstants.RMI_SERVICE_REPORT);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("ReportService unavailable", e);
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
        JLabel sub = new JLabel("Live server aggregates, detail table, and exportable reports");
        sub.setFont(UIConstants.FONT_REGULAR);
        sub.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel hdr = new JPanel(new MigLayout("insets 0, fillx", "[grow][][][]", "[][]"));
        hdr.setOpaque(false);
        hdr.add(title, "span 4, wrap");
        hdr.add(sub, "grow");

        refreshBtn = TaxPage.btn("Refresh",       UIConstants.PRIMARY_COLOR);
        exportCsvBtn  = TaxPage.btn("Export CSV",    UIConstants.SUCCESS_COLOR);
        exportPdfBtn     = TaxPage.btn("Export PDF",    new Color(192, 57, 43));
        refreshBtn.addActionListener(e -> loadData());
        exportCsvBtn.addActionListener(e  -> exportCsv());
        exportPdfBtn.addActionListener(e     -> exportPdf());
        exportCsvBtn.setEnabled(false);
        exportPdfBtn.setEnabled(false);
        hdr.add(refreshBtn, "h 38!, gapleft 8");
        hdr.add(exportCsvBtn,  "h 38!, gapleft 8");
        hdr.add(exportPdfBtn,     "h 38!, gapleft 8");
        root.add(hdr, "growx, wrap");

        lastRefreshedLabel = new JLabel("Not refreshed yet");
        lastRefreshedLabel.setFont(UIConstants.FONT_SMALL);
        lastRefreshedLabel.setForeground(UIConstants.TEXT_MUTED);
        root.add(lastRefreshedLabel, "growx, wrap, gapbottom 24");

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
        root.add(paymentChartHolder, "growx, wrap, gapbottom 24");

        root.add(sectionLabel("Report Detail"), "wrap, gapbottom 10");
        reportTableModel = new DefaultTableModel(
                new Object[]{"Invoice #", "Import Item", "User", "Amount", "Status", "Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        reportTable = TaxPage.styledTable(reportTableModel);
        TableFormatUtil.applyCurrencyColumn(reportTable, 3);
        TableFormatUtil.applyDateColumn(reportTable, 5);
        reportTable.getColumnModel().getColumn(4).setCellRenderer(TableFormatUtil.statusRenderer());
        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        int[] reportWidths = {120, 220, 140, 120, 110, 110};
        for (int i = 0; i < reportWidths.length; i++)
            reportTable.getColumnModel().getColumn(i).setPreferredWidth(reportWidths[i]);
        root.add(TaxPage.styledScroll(reportTable), "growx, h 220!, wrap, gapbottom 8");

        totalsSummaryLabel = new JLabel(" ");
        totalsSummaryLabel.setFont(UIConstants.FONT_SMALL);
        totalsSummaryLabel.setForeground(UIConstants.TEXT_SECONDARY);
        root.add(totalsSummaryLabel, "growx, wrap, gapbottom 8");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        root.add(statusLabel, "growx");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.BACKGROUND_COLOR);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Data loading (server-side aggregation via ReportService) ───────────
    private void loadData() {
        if (reportService == null) {
            statusLabel.setText("Report service unavailable — start the server");
            statusLabel.setForeground(UIConstants.ERROR_COLOR);
            return;
        }

        statusLabel.setText("Loading...");
        statusLabel.setForeground(UIConstants.INFO_COLOR);
        shell.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        refreshBtn.setEnabled(false);

        new SwingWorker<Void, Void>() {
            ReportSummary summary;
            long pending, paid, cleared, hold;
            List<ReportTableRow> rows;

            protected Void doInBackground() throws Exception {
                summary = reportService.getDashboardSummary();
                var counts = reportService.getImportStatusCounts();
                pending = counts.getPending();
                paid = counts.getPaid();
                cleared = counts.getCleared();
                hold = counts.getHold();
                rows = reportService.getReportTableRows();
                return null;
            }

            protected void done() {
                shell.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                refreshBtn.setEnabled(true);
                try {
                    get();
                    lastSummary = summary;
                    lastReportRows = rows;
                    dataLoaded = summary != null;
                    exportCsvBtn.setEnabled(dataLoaded);
                    exportPdfBtn.setEnabled(dataLoaded);

                    if (summary != null) {
                        totalImportsVal.setText(String.valueOf(summary.getTotalImports()));
                        pendingImportsVal.setText(String.valueOf(summary.getPendingImports()));
                        clearedImportsVal.setText(String.valueOf(summary.getClearedImports()));
                        totalInvoicesVal.setText(String.valueOf(summary.getTotalInvoices()));
                        totalTaxVal.setText(TableFormatUtil.formatCurrency(summary.getTotalTaxCollected()));
                        totalPaymentsVal.setText(String.valueOf(summary.getTotalPayments()));
                        completedPaymentsVal.setText(String.valueOf(summary.getCompletedPayments()));
                        pendingPaymentsVal.setText(String.valueOf(summary.getPendingPayments()));
                        totalPaidVal.setText(TableFormatUtil.formatCurrency(summary.getTotalRevenue()));
                    }

                    refreshChart(importChartHolder,
                            buildImportBarChart((int) pending, (int) paid, (int) cleared, (int) hold, 0));
                    refreshChart(invoiceChartHolder,
                            buildInvoicePieChart(
                                    summary != null ? (int) summary.getTotalInvoices() : 0,
                                    summary != null ? summary.getTotalTaxCollected() : BigDecimal.ZERO));
                    refreshChart(paymentChartHolder,
                            buildPaymentBarChart(
                                    summary != null ? (int) summary.getCompletedPayments() : 0,
                                    summary != null ? (int) summary.getPendingPayments() : 0,
                                    summary != null ? (int) Math.max(0,
                                            summary.getTotalPayments()
                                                    - summary.getCompletedPayments()
                                                    - summary.getPendingPayments()) : 0));

                    fillReportTable(rows);
                    updateTotalsSummary(summary);

                    String at = summary != null && summary.getGeneratedAt() != null
                            ? summary.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    statusLabel.setText("Report data loaded");
                    statusLabel.setForeground(UIConstants.TEXT_MUTED);
                    lastRefreshedLabel.setText("Last refreshed: " + at);
                } catch (Exception ex) {
                    logger.warn("Report load failed", ex);
                    dataLoaded = false;
                    exportCsvBtn.setEnabled(false);
                    exportPdfBtn.setEnabled(false);
                    statusLabel.setText("Failed to load report data");
                    statusLabel.setForeground(UIConstants.ERROR_COLOR);
                    lastRefreshedLabel.setText("Last refresh failed");
                    JOptionPane.showMessageDialog(shell,
                            UserMessageUtil.friendly(ex, "Could not load reports. Check the server connection."),
                            UIConstants.APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void fillReportTable(List<ReportTableRow> rows) {
        reportTableModel.setRowCount(0);
        if (rows == null || rows.isEmpty()) {
            reportTableModel.addRow(new Object[]{
                    "—", "No report data available", "—", null, "—", null
            });
            return;
        }
        for (ReportTableRow r : rows) {
            reportTableModel.addRow(new Object[]{
                    r.getInvoiceNumber(),
                    r.getImportItemName(),
                    r.getUserDisplay(),
                    r.getAmount(),
                    r.getStatus(),
                    r.getRecordDate()
            });
        }
    }

    private void updateTotalsSummary(ReportSummary summary) {
        if (summary == null) {
            totalsSummaryLabel.setText(" ");
            return;
        }
        totalsSummaryLabel.setText(String.format(
                "Totals — Users: %d | Imports: %d | Invoices: %d | Tax: %s | Revenue: %s",
                summary.getTotalUsers(),
                summary.getTotalImports(),
                summary.getTotalInvoices(),
                TableFormatUtil.formatCurrency(summary.getTotalTaxCollected()),
                TableFormatUtil.formatCurrency(summary.getTotalRevenue())));
    }

    // ── Chart builders ─────────────────────────────────────────────────────
    private JFreeChart buildImportBarChart(int pending, int paid, int cleared, int hold, int other) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        ds.addValue(pending, "Count", "Pending");
        ds.addValue(paid,    "Count", "Paid");
        ds.addValue(cleared, "Count", "Cleared");
        ds.addValue(hold,    "Count", "Hold");
        ds.addValue(other,  "Count", "Other");

        JFreeChart chart = ChartFactory.createBarChart(
            "Import Items by Status", null, "Count", ds,
            PlotOrientation.VERTICAL, false, true, false);
        styleBarChart(chart, new Color[]{
            UIConstants.WARNING_COLOR, UIConstants.INFO_COLOR, UIConstants.SUCCESS_COLOR,
            UIConstants.ERROR_COLOR, UIConstants.TEXT_MUTED});
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

    // ── PDF Export ─────────────────────────────────────────────────────────
    private void exportPdf() {
        if (!dataLoaded) {
            JOptionPane.showMessageDialog(shell,
                    "Load report data before exporting.",
                    UIConstants.APP_NAME, JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Report as PDF");
        fc.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        fc.setSelectedFile(new File("import_tax_report.pdf"));
        if (fc.showSaveDialog(shell) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".pdf")) file = new File(file.getAbsolutePath() + ".pdf");
        final File finalFile = file;

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                Document doc = new Document();
                PdfWriter.getInstance(doc, new java.io.FileOutputStream(finalFile));
                doc.open();

                com.itextpdf.text.Font titleFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                com.itextpdf.text.Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                com.itextpdf.text.Font bodyFont    = FontFactory.getFont(FontFactory.HELVETICA, 9);
                com.itextpdf.text.Font headerFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9,
                    new BaseColor(255, 255, 255));

                Paragraph mainTitle = new Paragraph(UIConstants.APP_NAME + " — Financial Report", titleFont);
                mainTitle.setAlignment(Element.ALIGN_CENTER);
                mainTitle.setSpacingAfter(4);
                doc.add(mainTitle);
                String generated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                Paragraph date = new Paragraph("Generated: " + generated, bodyFont);
                date.setAlignment(Element.ALIGN_CENTER);
                date.setSpacingAfter(12);
                doc.add(date);

                if (lastSummary != null) {
                    doc.add(new Paragraph("Summary Totals", sectionFont));
                    doc.add(new Paragraph(
                            "Users: " + lastSummary.getTotalUsers()
                                    + " | Imports: " + lastSummary.getTotalImports()
                                    + " | Invoices: " + lastSummary.getTotalInvoices()
                                    + " | Tax: " + TableFormatUtil.formatCurrency(lastSummary.getTotalTaxCollected())
                                    + " | Revenue: " + TableFormatUtil.formatCurrency(lastSummary.getTotalRevenue()),
                            bodyFont));
                    doc.add(new Paragraph(" "));
                }

                doc.add(new Paragraph("Invoice Report Detail", sectionFont));
                doc.add(new Paragraph(" "));
                if (lastReportRows != null && !lastReportRows.isEmpty()) {
                    PdfPTable t = new PdfPTable(new float[]{2, 3, 2, 2, 2, 2});
                    t.setWidthPercentage(100);
                    BaseColor hdrColor = new BaseColor(0, 100, 180);
                    for (String h : new String[]{"Invoice #", "Import Item", "User", "Amount", "Status", "Date"}) {
                        PdfPCell c = new PdfPCell(new Phrase(h, headerFont));
                        c.setBackgroundColor(hdrColor);
                        c.setPadding(5);
                        t.addCell(c);
                    }
                    for (ReportTableRow r : lastReportRows) {
                        t.addCell(cell(str(r.getInvoiceNumber()), bodyFont));
                        t.addCell(cell(str(r.getImportItemName()), bodyFont));
                        t.addCell(cell(str(r.getUserDisplay()), bodyFont));
                        t.addCell(cell(TableFormatUtil.formatCurrency(r.getAmount()), bodyFont));
                        t.addCell(cell(str(r.getStatus()), bodyFont));
                        t.addCell(cell(TableFormatUtil.formatDate(r.getRecordDate()), bodyFont));
                    }
                    doc.add(t);
                } else {
                    doc.add(new Paragraph("No report rows available. Refresh reports first.", bodyFont));
                }

                doc.close();
                return null;
            }
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(shell,
                        "PDF exported to:\n" + finalFile.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    logger.warn("PDF export failed", ex);
                    JOptionPane.showMessageDialog(shell,
                        "PDF export failed. Ensure report data is loaded and try again.",
                        "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private PdfPCell cell(String text, com.itextpdf.text.Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
        c.setPadding(4);
        c.setBorderColor(new BaseColor(220, 224, 230));
        return c;
    }

    private String str(Object v) { return v == null ? "" : v.toString(); }

    // ── CSV Export ─────────────────────────────────────────────────────────
    private void exportCsv() {
        if (!dataLoaded) {
            JOptionPane.showMessageDialog(shell,
                    "Load report data before exporting.",
                    UIConstants.APP_NAME, JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Report as CSV");
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fc.setSelectedFile(new File("import_tax_report.csv"));
        if (fc.showSaveDialog(shell) != JFileChooser.APPROVE_OPTION) return;

        File selected = fc.getSelectedFile();
        if (!selected.getName().endsWith(".csv")) {
            selected = new File(selected.getAbsolutePath() + ".csv");
        }
        final File exportFile = selected;

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                try (FileWriter fw = new FileWriter(exportFile)) {
                    String generated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    fw.write(UIConstants.APP_NAME + " — Financial Report\n");
                    fw.write("Generated," + generated + "\n\n");
                    if (lastSummary != null) {
                        fw.write("=== SUMMARY TOTALS ===\n");
                        fw.write(csv("Users", lastSummary.getTotalUsers()));
                        fw.write(csv("Imports", lastSummary.getTotalImports()));
                        fw.write(csv("Invoices", lastSummary.getTotalInvoices()));
                        fw.write(csv("Tax Collected", lastSummary.getTotalTaxCollected()));
                        fw.write(csv("Revenue", lastSummary.getTotalRevenue()));
                        fw.write("\n");
                    }
                    fw.write("=== REPORT DETAIL ===\n");
                    fw.write("Invoice Number,Import Item,User,Amount,Status,Date\n");
                    if (lastReportRows != null) {
                        for (ReportTableRow r : lastReportRows) {
                            fw.write(csv(r.getInvoiceNumber(), r.getImportItemName(), r.getUserDisplay(),
                                    r.getAmount(), r.getStatus(), r.getRecordDate()));
                        }
                    }
                }
                return null;
            }

            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(shell, "Report exported to:\n" + exportFile.getAbsolutePath(),
                            "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    logger.warn("CSV export failed", ex);
                    JOptionPane.showMessageDialog(shell,
                            "Export failed. Load report data and try again.",
                            "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
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
