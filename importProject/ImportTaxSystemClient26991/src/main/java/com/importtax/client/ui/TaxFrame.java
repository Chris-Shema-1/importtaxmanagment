package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.CsvExporter;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.Tax;
import com.importtax.server.rmi.TaxService;
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

public class TaxFrame extends JFrame {

    private TaxService taxService;
    private final List<Tax> allTaxes = new ArrayList<>();
    private final List<Tax> displayedTaxes = new ArrayList<>();
    private JTextField searchField;
    private JLabel statusLabel;
    private JTable table;
    private DefaultTableModel model;
    private RoundedButton addButton;
    private RoundedButton editButton;
    private RoundedButton deleteButton;
    private RoundedButton exportButton;

    public TaxFrame() {
        initializeService();
        initializeFrame();
        setContentPane(createContent());
        loadTaxes();
    }

    private void initializeService() {
        try {
            RmiConnection.initialize();
            taxService = RmiConnection.lookup(UIConstants.RMI_SERVICE_TAX);
        } catch (RemoteException | NotBoundException ex) {
            taxService = null;
        }
    }

    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " - Tax Management");
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
        JLabel title = new JLabel("Tax Profiles");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UIConstants.TEXT_COLOR);
        header.add(title);
        RoundedButton dashboardButton = new RoundedButton("Back to Dashboard");
        dashboardButton.setStateColors(UIConstants.PRIMARY_DARK,
                UIConstants.PRIMARY_DARK.brighter(), UIConstants.PRIMARY_DARK.darker());
        dashboardButton.addActionListener(e -> backToDashboard());
        header.add(dashboardButton, "h 42!");
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

        model = new DefaultTableModel(new Object[]{"ID", "Tax Name", "Rate (%)", "Description"}, 0) {
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
        table.setSelectionForeground(java.awt.Color.WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.setDefaultRenderer(String.class, new AlternatingRowRenderer());
        javax.swing.table.JTableHeader taxHeader = table.getTableHeader();
        taxHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        taxHeader.setBackground(new java.awt.Color(35, 35, 45));
        taxHeader.setForeground(UIConstants.TEXT_COLOR);
        taxHeader.setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(UIConstants.PANEL_COLOR);
        panel.add(scrollPane, "grow, wrap");

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

    private void loadTaxes() {
        if (!ensureService()) {
            return;
        }
        setLoading(true, "Loading tax profiles...");
        new SwingWorker<List<Tax>, Void>() {
            @Override
            protected List<Tax> doInBackground() throws Exception {
                return taxService.findAll();
            }

            @Override
            protected void done() {
                try {
                    allTaxes.clear();
                    allTaxes.addAll(get());
                    filter();
                    setLoading(false, allTaxes.size() + " tax profile(s) loaded");
                } catch (Exception ex) {
                    showError(extract(ex, "Could not load taxes"));
                }
            }
        }.execute();
    }

    private void filter() {
        String term = searchField.getText().trim().toLowerCase(Locale.ROOT);
        displayedTaxes.clear();
        model.setRowCount(0);
        for (Tax tax : allTaxes) {
            if (!term.isEmpty()
                    && !safe(tax.getTaxName()).contains(term)
                    && !safe(tax.getDescription()).contains(term)) {
                continue;
            }
            displayedTaxes.add(tax);
            model.addRow(new Object[]{tax.getTaxId(), tax.getTaxName(), tax.getTaxRate(), tax.getDescription()});
        }
        if (!term.isEmpty()) {
            statusLabel.setText(displayedTaxes.size() + " matching tax profile(s)");
        }
    }

    private void openDialog(Tax tax) {
        TaxDialog dialog = new TaxDialog(this, tax);
        dialog.setSaveAction((source, payload) -> mutate(source,
                payload.getTaxId() == null ? "Saving tax..." : "Updating tax...",
                () -> payload.getTaxId() == null ? taxService.save(payload) : taxService.update(payload),
                payload.getTaxId() == null ? "Tax profile saved" : "Tax profile updated"));
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        Tax selected = getSelected();
        if (selected == null) {
            showError("Select a tax profile to delete");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete selected tax profile?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        mutate(null, "Deleting tax...", () -> {
            taxService.delete(selected);
            return null;
        }, "Tax profile deleted");
    }

    private void exportCsv() {
        File file = CsvExporter.chooseTargetFile(this, "tax-profiles.csv");
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

    private void mutate(TaxDialog dialog, String loadingMessage, RemoteWork work, String successMessage) {
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
                setLoading(false, successMessage);
                try {
                    get();
                    if (dialog != null) {
                        dialog.dispose();
                    }
                    loadTaxes();
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

    private Tax getSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= displayedTaxes.size()) {
            return null;
        }
        return displayedTaxes.get(table.convertRowIndexToModel(row));
    }

    private boolean ensureService() {
        if (taxService == null) {
            showError("Tax service is unavailable. Start the RMI server and try again.");
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
        JOptionPane.showMessageDialog(this, message, "Tax Management", JOptionPane.ERROR_MESSAGE);
    }

    private String extract(Exception ex, String fallback) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() == null ? fallback : cause.getMessage();
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private void backToDashboard() {
        dispose();
        new DashboardFrame(com.importtax.client.util.CurrentSession.getUsername()).setVisible(true);
    }

    @FunctionalInterface
    private interface RemoteWork {
        Object run() throws Exception;
    }

    private static class AlternatingRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private static final java.awt.Color EVEN = UIConstants.PANEL_COLOR;
        private static final java.awt.Color ODD  = new java.awt.Color(38, 50, 62);

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            if (isSelected) {
                setBackground(UIConstants.PRIMARY_COLOR);
                setForeground(java.awt.Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? EVEN : ODD);
                setForeground(UIConstants.TEXT_COLOR);
            }
            return this;
        }
    }
}
