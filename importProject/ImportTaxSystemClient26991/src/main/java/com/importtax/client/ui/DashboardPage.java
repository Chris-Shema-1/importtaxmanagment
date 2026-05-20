package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.TableFormatUtil;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.ImportStatusCounts;
import com.importtax.server.model.MonthlyTaxSummary;
import com.importtax.server.model.RecentActivityEntry;
import com.importtax.server.model.ReportSummary;
import com.importtax.server.rmi.ReportService;
import net.miginfocom.swing.MigLayout;
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
import java.awt.*;
import java.math.BigDecimal;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DashboardPage extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DashboardPage.class);

    private final AppShell shell;
    private ReportService reportService;

    private JLabel statusLabel;
    private JButton refreshBtn;
    private JPanel statsPanel;
    private JPanel activityPanel;
    private JPanel importChartHolder;
    private JPanel taxChartHolder;
    private JPanel paymentChartHolder;

    private final JLabel[] statValueLabels = new JLabel[8];

    public DashboardPage(AppShell shell) {
        this.shell = shell;
        initRmi();
        setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        add(buildScrollContent(), BorderLayout.CENTER);
        reload();
    }

    public void reload() {
        loadDashboard();
    }

    private void initRmi() {
        try {
            RmiConnection.initialize();
            reportService = RmiConnection.lookup(UIConstants.RMI_SERVICE_REPORT);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("ReportService unavailable", e);
        }
    }

    private JScrollPane buildScrollContent() {
        JPanel p = new JPanel(new MigLayout(
                "insets 32 32 32 32, fillx",
                "[grow]",
                "[]16[]16[]16[]16[]"));
        p.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel greeting = new JLabel("Good to see you back");
        greeting.setFont(new Font("Segoe UI", Font.BOLD, 26));
        greeting.setForeground(UIConstants.TEXT_COLOR);

        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][120!]", "[]"));
        header.setOpaque(false);
        header.add(greeting, "grow");
        refreshBtn = TaxPage.btn("Refresh", UIConstants.PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadDashboard());
        header.add(refreshBtn, "h 38!");
        p.add(header, "growx, wrap");

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        p.add(statusLabel, "growx, wrap, gapbottom 8");

        statsPanel = new JPanel(new GridLayout(2, 4, 16, 16));
        statsPanel.setOpaque(false);
        String[] titles = {
                "Total Users", "Total Import Items", "Pending Imports", "Paid Imports",
                "Cleared Imports", "Total Tax Collected", "Total Payments", "Total Invoices"
        };
        Color[] accents = {
                UIConstants.ACCENT_COLOR, UIConstants.PRIMARY_COLOR, UIConstants.WARNING_COLOR,
                new Color(30, 120, 180), UIConstants.SUCCESS_COLOR, UIConstants.INFO_COLOR,
                UIConstants.PRIMARY_COLOR, UIConstants.ACCENT_COLOR
        };
        for (int i = 0; i < titles.length; i++) {
            statValueLabels[i] = statVal("--");
            statsPanel.add(statCard(titles[i], statValueLabels[i], accents[i]));
        }
        p.add(statsPanel, "growx, wrap");

        p.add(sectionLabel("Visual Summary"), "wrap, gapbottom 8");
        JPanel charts = new JPanel(new GridLayout(1, 3, 16, 0));
        charts.setOpaque(false);
        importChartHolder = chartHolder();
        taxChartHolder = chartHolder();
        paymentChartHolder = chartHolder();
        charts.add(importChartHolder);
        charts.add(taxChartHolder);
        charts.add(paymentChartHolder);
        p.add(charts, "growx, wrap");

        p.add(sectionLabel("Quick Actions"), "wrap, gapbottom 12");
        p.add(buildQuickActions(), "growx, wrap");

        p.add(sectionLabel("Recent Activity"), "wrap, gapbottom 12");
        activityPanel = new JPanel(new MigLayout("insets 20 20 20 20, fillx", "[grow]", "")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PANEL_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        activityPanel.setOpaque(false);
        p.add(activityPanel, "growx, wrap, gapbottom 16");

        JLabel footer = new JLabel(UIConstants.FOOTER_TAGLINE);
        footer.setFont(UIConstants.FONT_SMALL);
        footer.setForeground(UIConstants.TEXT_MUTED);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(footer, "growx, wrap");

        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BACKGROUND_COLOR);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel chartHolder() {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.setPreferredSize(new Dimension(0, 220));
        return holder;
    }

    private void loadDashboard() {
        if (reportService == null) {
            statusLabel.setText("Report service unavailable — start the server");
            statusLabel.setForeground(UIConstants.ERROR_COLOR);
            return;
        }

        refreshBtn.setEnabled(false);
        shell.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        statusLabel.setText("Loading dashboard…");
        statusLabel.setForeground(UIConstants.INFO_COLOR);

        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardData data = new DashboardData();
                data.summary = reportService.getDashboardSummary();
                data.statusCounts = reportService.getImportStatusCounts();
                data.monthlyTax = reportService.getMonthlyTaxSummary();
                data.activities = reportService.getRecentActivities();
                return data;
            }

            @Override
            protected void done() {
                refreshBtn.setEnabled(true);
                shell.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                try {
                    DashboardData data = get();
                    applySummary(data.summary);
                    refreshChart(importChartHolder, buildStatusPieChart(data.statusCounts));
                    refreshChart(taxChartHolder, buildMonthlyTaxChart(data.monthlyTax));
                    refreshChart(paymentChartHolder, buildPaymentOverviewChart(data.summary));
                    refreshActivity(data.activities);
                    String at = data.summary != null && data.summary.getGeneratedAt() != null
                            ? data.summary.getGeneratedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                            : "now";
                    statusLabel.setText("Last updated at " + at);
                    statusLabel.setForeground(UIConstants.TEXT_MUTED);
                } catch (Exception ex) {
                    logger.warn("Dashboard load failed", ex);
                    statusLabel.setText("Failed to load dashboard data");
                    statusLabel.setForeground(UIConstants.ERROR_COLOR);
                    JOptionPane.showMessageDialog(shell,
                            "Could not load dashboard data. Check the server connection.",
                            "Dashboard", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void applySummary(ReportSummary s) {
        if (s == null) {
            return;
        }
        statValueLabels[0].setText(String.valueOf(s.getTotalUsers()));
        statValueLabels[1].setText(String.valueOf(s.getTotalImports()));
        statValueLabels[2].setText(String.valueOf(s.getPendingImports()));
        statValueLabels[3].setText(String.valueOf(s.getPaidImports()));
        statValueLabels[4].setText(String.valueOf(s.getClearedImports()));
        statValueLabels[5].setText(TableFormatUtil.formatCurrency(s.getTotalTaxCollected()));
        statValueLabels[6].setText(String.valueOf(s.getTotalPayments()));
        statValueLabels[7].setText(String.valueOf(s.getTotalInvoices()));
    }

    private void refreshActivity(List<RecentActivityEntry> activities) {
        activityPanel.removeAll();
        if (activities == null || activities.isEmpty()) {
            JLabel empty = new JLabel("No recent activity");
            empty.setFont(UIConstants.FONT_REGULAR);
            empty.setForeground(UIConstants.TEXT_MUTED);
            activityPanel.add(empty, "growx, wrap");
        } else {
            for (RecentActivityEntry entry : activities) {
                Color dot = activityDotColor(entry.getActivityType(), entry.getStatus());
                activityPanel.add(activityRow(entry.getDescription(), formatActivityTime(entry.getActivityDate()), dot),
                        "growx, wrap, gapbottom 4");
            }
        }
        activityPanel.revalidate();
        activityPanel.repaint();
    }

    private static Color activityDotColor(String type, String status) {
        if (status != null && !status.isBlank()) {
            return TableFormatUtil.statusColor(status);
        }
        return switch (type == null ? "" : type.toUpperCase()) {
            case "PAYMENT" -> UIConstants.SUCCESS_COLOR;
            case "INVOICE" -> UIConstants.INFO_COLOR;
            default        -> UIConstants.PRIMARY_COLOR;
        };
    }

    private static String formatActivityTime(LocalDate date) {
        if (date == null) {
            return "—";
        }
        long days = ChronoUnit.DAYS.between(date, LocalDate.now());
        if (days == 0) {
            return "Today";
        }
        if (days == 1) {
            return "Yesterday";
        }
        if (days < 7) {
            return days + " days ago";
        }
        return TableFormatUtil.formatDate(date);
    }

    private JFreeChart buildStatusPieChart(ImportStatusCounts counts) {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        if (counts != null) {
            if (counts.getPending() > 0) ds.setValue("Pending", counts.getPending());
            if (counts.getPaid() > 0) ds.setValue("Paid", counts.getPaid());
            if (counts.getCleared() > 0) ds.setValue("Cleared", counts.getCleared());
            if (counts.getHold() > 0) ds.setValue("Hold", counts.getHold());
        }
        if (ds.getItemCount() == 0) {
            ds.setValue("No data", 1);
        }
        JFreeChart chart = ChartFactory.createPieChart("Import Status", ds, true, false, false);
        stylePieChart(chart);
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setSectionPaint("Pending", UIConstants.WARNING_COLOR);
        plot.setSectionPaint("Paid", new Color(30, 120, 180));
        plot.setSectionPaint("Cleared", UIConstants.SUCCESS_COLOR);
        plot.setSectionPaint("Hold", UIConstants.ERROR_COLOR);
        return chart;
    }

    private JFreeChart buildMonthlyTaxChart(List<MonthlyTaxSummary> monthly) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        if (monthly != null) {
            for (MonthlyTaxSummary m : monthly) {
                BigDecimal tax = m.getTotalTax() != null ? m.getTotalTax() : BigDecimal.ZERO;
                ds.addValue(tax.doubleValue(), "Tax", m.getMonth());
            }
        }
        if (ds.getRowCount() == 0) {
            ds.addValue(0, "Tax", "—");
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Tax Collected by Month", null, "Amount", ds,
                PlotOrientation.VERTICAL, false, true, false);
        styleBarChart(chart, UIConstants.INFO_COLOR);
        return chart;
    }

    private JFreeChart buildPaymentOverviewChart(ReportSummary summary) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        BigDecimal revenue = summary != null && summary.getTotalRevenue() != null
                ? summary.getTotalRevenue() : BigDecimal.ZERO;
        long payments = summary != null ? summary.getTotalPayments() : 0;
        ds.addValue(revenue.doubleValue(), "Value", "Revenue");
        ds.addValue(payments, "Value", "Payment Count");
        JFreeChart chart = ChartFactory.createBarChart(
                "Payment Overview", null, "Value", ds,
                PlotOrientation.VERTICAL, false, true, false);
        styleBarChart(chart, UIConstants.SUCCESS_COLOR);
        return chart;
    }

    private void stylePieChart(JFreeChart chart) {
        chart.setBackgroundPaint(UIConstants.PANEL_COLOR);
        chart.getTitle().setPaint(UIConstants.TEXT_COLOR);
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(UIConstants.PANEL_COLOR);
        plot.setOutlineVisible(false);
    }

    private void styleBarChart(JFreeChart chart, Color color) {
        chart.setBackgroundPaint(UIConstants.PANEL_COLOR);
        chart.getTitle().setPaint(UIConstants.TEXT_COLOR);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(UIConstants.PANEL_COLOR);
        plot.setRangeGridlinePaint(UIConstants.BORDER_COLOR);
        plot.setOutlineVisible(false);
        plot.getDomainAxis().setTickLabelPaint(UIConstants.TEXT_SECONDARY);
        plot.getRangeAxis().setTickLabelPaint(UIConstants.TEXT_SECONDARY);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, color);
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.2);
    }

    private void refreshChart(JPanel holder, JFreeChart chart) {
        holder.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(UIConstants.PANEL_COLOR);
        cp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        holder.add(cp, BorderLayout.CENTER);
        holder.revalidate();
        holder.repaint();
    }

    private JPanel buildQuickActions() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        Object[][] actions = {
                {"New Import", UIConstants.PRIMARY_COLOR, AppShell.PAGE_IMPORTS},
                {"View Reports", UIConstants.INFO_COLOR, AppShell.PAGE_REPORTS},
                {"Process Payment", UIConstants.SUCCESS_COLOR, AppShell.PAGE_PAYMENTS},
                {"User Management", UIConstants.ACCENT_COLOR, AppShell.PAGE_USERS},
        };
        for (Object[] a : actions) {
            JButton btn = actionBtn((String) a[0], (Color) a[1]);
            btn.addActionListener(e -> shell.navigate((String) a[2]));
            row.add(btn);
        }
        return row;
    }

    private JPanel statCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new MigLayout("insets 18 16 18 16", "[grow]", "[]6[]")) {
            @Override
            protected void paintComponent(Graphics g) {
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
        valueLabel.setForeground(accent);
        card.add(valueLabel, "wrap");
        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        card.add(lbl);
        return card;
    }

    private JLabel statVal(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        return l;
    }

    private JButton actionBtn(String text, Color color) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? color.darker()
                        : getModel().isRollover() ? color.brighter() : color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(UIConstants.FONT_BUTTON);
        b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(0, 44));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel activityRow(String text, String time, Color dot) {
        JPanel row = new JPanel(new MigLayout("insets 8 0 8 0, fillx", "[8!][grow][]", "[]"));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                UIConstants.withAlpha(UIConstants.BORDER_COLOR, 80)));

        JPanel dotPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dot);
                g2.fillOval(0, (getHeight() - 8) / 2, 8, 8);
                g2.dispose();
            }
        };
        dotPanel.setOpaque(false);
        dotPanel.setPreferredSize(new Dimension(8, 8));
        row.add(dotPanel, "w 8!, h 8!, aligny center");

        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_REGULAR);
        label.setForeground(UIConstants.TEXT_COLOR);
        row.add(label, "grow, gapleft 12");

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(UIConstants.FONT_SMALL);
        timeLabel.setForeground(UIConstants.TEXT_MUTED);
        row.add(timeLabel);
        return row;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(UIConstants.TEXT_COLOR);
        return l;
    }

    private static final class DashboardData {
        ReportSummary summary;
        ImportStatusCounts statusCounts;
        List<MonthlyTaxSummary> monthlyTax;
        List<RecentActivityEntry> activities;
    }
}
