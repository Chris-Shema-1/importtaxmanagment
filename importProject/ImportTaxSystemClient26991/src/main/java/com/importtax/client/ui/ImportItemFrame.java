package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.ImportItem;
import com.importtax.server.rmi.ImportItemService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportItemFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ImportItemFrame.class);

    private ImportItemService importItemService;
    private List<ImportItem> allItems = new ArrayList<>();
    private List<ImportItem> displayedItems = new ArrayList<>();

    private JTextField searchField;
    private JLabel statusLabel;
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private RoundedButton addButton;
    private RoundedButton editButton;
    private RoundedButton deleteButton;
    private RoundedButton refreshButton;
    private RoundedButton searchButton;

    public ImportItemFrame() {
        logger.info("Initializing ImportItemFrame");
        initializeRmiService();
        initializeFrame();
        setupLayout();
        setupTableSelection();
        loadItems();
    }

    private void initializeRmiService() {
        try {
            RmiConnection.initialize();
            importItemService = RmiConnection.lookup(UIConstants.RMI_SERVICE_IMPORT);
            logger.info("ImportItemService RMI connection established");
        } catch (RemoteException | NotBoundException e) {
            logger.warn("ImportItemService is unavailable", e);
            importItemService = null;
        }
    }

    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " - Import Items");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1400, 850);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        setBackground(UIConstants.BACKGROUND_COLOR);
    }

    private void setupLayout() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        root.add(createHeaderPanel(), BorderLayout.NORTH);
        root.add(createTablePanel(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][pref]", "[]"));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Import Item Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UIConstants.TEXT_COLOR);
        titlePanel.add(title, "wrap");

        JLabel subtitle = new JLabel("Search, review, and manage imported goods");
        subtitle.setFont(UIConstants.FONT_REGULAR);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        titlePanel.add(subtitle);
        header.add(titlePanel, "grow");

        RoundedButton dashboardButton = new RoundedButton("Back to Dashboard");
        dashboardButton.setPreferredSize(new Dimension(170, 42));
        dashboardButton.setStateColors(UIConstants.PRIMARY_DARK,
                UIConstants.PRIMARY_DARK.brighter(), UIConstants.PRIMARY_DARK.darker());
        dashboardButton.addActionListener(e -> openDashboard());
        header.add(dashboardButton, "h 42!");
        return header;
    }

    private JPanel createTablePanel() {
        RoundedPanel panel = new RoundedPanel(8, 8, new Color(40, 40, 46));
        panel.setLayout(new MigLayout("insets 18, fill", "[grow]", "[][grow][]"));

        JPanel toolbar = new JPanel(new MigLayout("insets 0, fillx", "[grow][120!][150!][110!][110!]", "[]"));
        toolbar.setOpaque(false);
        searchField = createTextField();
        searchField.addActionListener(e -> applySearchFilter());
        installSearchFilter();
        searchButton = createActionButton("Search", UIConstants.ACCENT_COLOR);
        searchButton.addActionListener(e -> applySearchFilter());
        addButton = createActionButton("Add New Import", UIConstants.SUCCESS_COLOR);
        addButton.addActionListener(e -> openAddDialog());
        editButton = createActionButton("Edit", UIConstants.PRIMARY_COLOR);
        editButton.addActionListener(e -> openEditDialog());
        deleteButton = createActionButton("Delete", UIConstants.ERROR_COLOR);
        deleteButton.addActionListener(e -> deleteItem());
        refreshButton = createActionButton("Refresh", UIConstants.INFO_COLOR);
        refreshButton.addActionListener(e -> loadItems());

        toolbar.add(searchField, "grow, h 40!");
        toolbar.add(searchButton, "h 40!");
        toolbar.add(addButton, "h 40!");
        toolbar.add(editButton, "h 40!");
        toolbar.add(deleteButton, "h 40!, wrap");
        panel.add(toolbar, "grow, wrap, gapbottom 12");

        tableModel = new DefaultTableModel(new Object[]{
            "ID", "Item Name", "Category", "Quantity", "Unit Price", "Tax Rate",
            "Total Tax", "Importer Name", "Country", "Status", "Import Date"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Long.class;
                    case 3 -> Integer.class;
                    case 4, 5, 6 -> java.math.BigDecimal.class;
                    case 10 -> java.time.LocalDate.class;
                    default -> String.class;
                };
            }
        };

        itemTable = new ImportItemTable(tableModel);
        itemTable.setFont(UIConstants.FONT_REGULAR);
        itemTable.setRowHeight(40);
        itemTable.setGridColor(UIConstants.BORDER_COLOR);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        itemTable.setAutoCreateRowSorter(false);
        itemTable.setRowSorter(new TableRowSorter<>(tableModel));
        itemTable.setFillsViewportHeight(true);
        itemTable.setBackground(UIConstants.PANEL_COLOR);
        itemTable.setForeground(UIConstants.TEXT_COLOR);
        itemTable.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        itemTable.setSelectionForeground(Color.WHITE);
        itemTable.setShowVerticalLines(false);
        itemTable.setIntercellSpacing(new Dimension(0, 1));
        itemTable.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        itemTable.setDefaultRenderer(String.class, new AlternatingRowRenderer());
        itemTable.setDefaultRenderer(Number.class, new AlternatingRowRenderer());
        itemTable.getColumnModel().getColumn(9).setCellRenderer(new StatusCellRenderer());

        JTableHeader header = itemTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(35, 35, 45));
        header.setForeground(UIConstants.TEXT_COLOR);
        header.setReorderingAllowed(false);
        configureTableColumns();

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(UIConstants.PANEL_COLOR);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);
        panel.add(scrollPane, "grow, wrap");

        JPanel footer = new JPanel(new MigLayout("insets 0, fillx", "[grow][110!]", "[]"));
        footer.setOpaque(false);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        footer.add(statusLabel, "grow");
        footer.add(refreshButton, "h 38!");
        panel.add(footer, "growx");
        return panel;
    }

    private void configureTableColumns() {
        int[] widths = {70, 210, 150, 90, 125, 110, 125, 180, 150, 115, 130};
        for (int i = 0; i < widths.length; i++) {
            itemTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
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

    private RoundedButton createActionButton(String text, Color color) {
        RoundedButton button = new RoundedButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setStateColors(color, color.brighter(), color.darker());
        return button;
    }

    private void installSearchFilter() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter();
            }
        });
    }

    private void setupTableSelection() {
        itemTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditDialog();
                }
            }
        });
    }

    private void openAddDialog() {
        if (!ensureServiceAvailable()) {
            return;
        }
        ImportItemDialog dialog = new ImportItemDialog(this);
        dialog.setSaveAction((source, item) -> executeMutation(source, "Saving item...",
                () -> importItemService.saveItem(item, currentUserId()), "Item saved successfully"));
        dialog.setVisible(true);
    }

    private void openEditDialog() {
        ImportItem selected = getSelectedItem();
        if (selected == null) {
            showError("Select an item to update");
            return;
        }
        if (!ensureServiceAvailable()) {
            return;
        }
        ImportItemDialog dialog = new ImportItemDialog(this, selected);
        dialog.setSaveAction((source, item) -> executeMutation(source, "Updating item...",
                () -> importItemService.updateItem(item, currentUserId()), "Item updated successfully"));
        dialog.setVisible(true);
    }

    private void deleteItem() {
        ImportItem selected = getSelectedItem();
        if (selected == null) {
            showError("Select an item to delete");
            return;
        }
        int confirmed = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this import item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirmed != JOptionPane.YES_OPTION) {
            return;
        }
        executeMutation(null, "Deleting item...", () -> {
            importItemService.deleteItem(selected.getItemId());
            return null;
        }, "Item deleted successfully");
    }

    private ImportItem getSelectedItem() {
        int row = itemTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return displayedItems.get(itemTable.convertRowIndexToModel(row));
    }

    private void loadItems() {
        if (!ensureServiceAvailable()) {
            return;
        }
        setLoading(true, "Loading items...");
        new SwingWorker<List<ImportItem>, Void>() {
            @Override
            protected List<ImportItem> doInBackground() throws Exception {
                Long userId = currentUserId();
                return userId == null ? importItemService.findAllItems() : importItemService.findItemsByUser(userId);
            }

            @Override
            protected void done() {
                setLoading(false, " ");
                try {
                    allItems = new ArrayList<>(get());
                    applySearchFilter();
                    logger.info("Import items loaded");
                } catch (Exception ex) {
                    handleWorkerError("Could not load import items", ex);
                }
            }
        }.execute();
    }

    private void applySearchFilter() {
        String term = searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (term.isEmpty()) {
            populateTable(allItems);
            return;
        }

        List<ImportItem> filtered = allItems.stream()
                .filter(item -> containsIgnoreCase(item.getItemName(), term)
                || containsIgnoreCase(item.getCategory(), term)
                || containsIgnoreCase(item.getImporterName(), term)
                || containsIgnoreCase(item.getStatus(), term))
                .toList();
        populateTable(filtered);
    }

    private void executeMutation(ImportItemDialog dialog, String loadingMessage,
            RemoteMutation mutation, String successMessage) {
        if (!ensureServiceAvailable()) {
            return;
        }
        setLoading(true, loadingMessage);
        if (dialog != null) {
            dialog.setLoading(true, loadingMessage);
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                mutation.run();
                return null;
            }

            @Override
            protected void done() {
                setLoading(false, " ");
                if (dialog != null) {
                    dialog.setLoading(false, " ");
                }
                try {
                    get();
                    if (dialog != null) {
                        dialog.dispose();
                    }
                    JOptionPane.showMessageDialog(ImportItemFrame.this,
                            successMessage, "Import Items", JOptionPane.INFORMATION_MESSAGE);
                    loadItems();
                } catch (Exception ex) {
                    String detail = extractErrorMessage(successMessage + " failed", ex);
                    statusLabel.setText(detail);
                    statusLabel.setForeground(UIConstants.ERROR_COLOR);
                    logger.error(successMessage + " failed", ex.getCause() != null ? ex.getCause() : ex);
                    if (dialog != null) {
                        dialog.showErrorDialog(detail);
                    } else {
                        JOptionPane.showMessageDialog(ImportItemFrame.this,
                                detail, "Import Item Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }.execute();
    }

    private void populateTable(List<ImportItem> items) {
        displayedItems = new ArrayList<>(items);
        tableModel.setRowCount(0);
        for (ImportItem item : items) {
            tableModel.addRow(new Object[]{
                item.getItemId(),
                item.getItemName(),
                item.getCategory(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTaxRate(),
                item.getTotalTax(),
                item.getImporterName(),
                item.getCountryOfOrigin(),
                item.getStatus(),
                item.getImportDate()
            });
        }
        if (items.isEmpty()) {
            statusLabel.setText("No import records found");
        } else {
            String suffix = searchField.getText().trim().isEmpty() ? "" : " matching search";
            statusLabel.setText(items.size() + " import record(s)" + suffix);
        }
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
    }

    private boolean containsIgnoreCase(String value, String lowercaseTerm) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowercaseTerm);
    }

    private Long currentUserId() {
        return CurrentSession.getLoggedInUserId();
    }

    private boolean ensureServiceAvailable() {
        if (importItemService == null) {
            showError("Import item service is unavailable. Start the RMI server and try again.");
            return false;
        }
        return true;
    }

    private void setLoading(boolean loading, String message) {
        setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        addButton.setEnabled(!loading);
        editButton.setEnabled(!loading);
        deleteButton.setEnabled(!loading);
        refreshButton.setEnabled(!loading);
        searchButton.setEnabled(!loading);
        statusLabel.setText(message);
        statusLabel.setForeground(loading ? UIConstants.INFO_COLOR : UIConstants.TEXT_SECONDARY);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        JOptionPane.showMessageDialog(this, message, "Import Item Error", JOptionPane.ERROR_MESSAGE);
        logger.warn("Import item validation failed: {}", message);
    }

    private void handleWorkerError(String message, Exception ex) {
        String detail = extractErrorMessage(message, ex);
        statusLabel.setText(detail);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        JOptionPane.showMessageDialog(this, detail, "Import Item Error", JOptionPane.ERROR_MESSAGE);
        logger.error(message, ex.getCause() != null ? ex.getCause() : ex);
    }

    private String extractErrorMessage(String fallback, Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() == null ? fallback : cause.getMessage();
    }

    private void openDashboard() {
        dispose();
        String username = CurrentSession.getUsername();
        DashboardFrame dashboardFrame = new DashboardFrame(username);
        dashboardFrame.setVisible(true);
    }

    @FunctionalInterface
    private interface RemoteMutation {
        Object run() throws Exception;
    }

    private static class ImportItemTable extends JTable {

        private static final long serialVersionUID = 1L;

        ImportItemTable(DefaultTableModel model) {
            super(model);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() > 0) {
                return;
            }

            g.setColor(UIConstants.TEXT_SECONDARY);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            String message = "No import records found";
            int x = Math.max(16, (getWidth() - g.getFontMetrics().stringWidth(message)) / 2);
            int y = Math.max(40, getHeight() / 2);
            g.drawString(message, x, y);
        }
    }

    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        private static final Color EVEN_ROW = UIConstants.PANEL_COLOR;
        private static final Color ODD_ROW = new Color(50, 50, 56);

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            if (isSelected) {
                setBackground(UIConstants.PRIMARY_COLOR);
                setForeground(Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? EVEN_ROW : ODD_ROW);
                setForeground(UIConstants.TEXT_COLOR);
            }
            return this;
        }
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value == null ? "" : value.toString().trim().toUpperCase(Locale.ROOT);
            setHorizontalAlignment(CENTER);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            if (isSelected) {
                setBackground(UIConstants.PRIMARY_COLOR);
                setForeground(Color.WHITE);
                return this;
            }

            setBackground(row % 2 == 0 ? UIConstants.PANEL_COLOR : new Color(50, 50, 56));
            setForeground(statusColor(status));
            setText(status);
            return this;
        }

        private Color statusColor(String status) {
            return switch (status) {
                case "APPROVED", "PAID", "CLEARED" -> UIConstants.SUCCESS_COLOR;
                case "REJECTED", "HOLD" -> UIConstants.ERROR_COLOR;
                case "PENDING" -> UIConstants.WARNING_COLOR;
                default -> UIConstants.TEXT_COLOR;
            };
        }
    }
}
