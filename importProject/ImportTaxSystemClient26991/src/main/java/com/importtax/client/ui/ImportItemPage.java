package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.TableFormatUtil;
import com.importtax.client.util.UserMessageUtil;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.ImportItem;
import com.importtax.server.rmi.ImportItemService;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportItemPage extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ImportItemPage.class);

    private final AppShell shell;
    private ImportItemService importItemService;
    private List<ImportItem> allItems       = new ArrayList<>();
    private List<ImportItem> displayedItems = new ArrayList<>();

    private static final String[] STATUS_FILTER_OPTIONS = {"ALL", "PENDING", "PAID", "CLEARED", "HOLD"};

    private JTextField        searchField;
    private JComboBox<String> statusFilterCombo;
    private JLabel            statusLabel;
    private JTable            itemTable;
    private DefaultTableModel tableModel;
    private RoundedButton     addButton, editButton, deleteButton, refreshButton;

    public ImportItemPage(AppShell shell) {
        this.shell = shell;
        initRmi();
        setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        add(buildContent(), BorderLayout.CENTER);
        setupDoubleClick();
    }

    public void reload() { loadItems(); }

    private void initRmi() {
        try {
            RmiConnection.initialize();
            importItemService = RmiConnection.lookup(UIConstants.RMI_SERVICE_IMPORT);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("ImportItemService unavailable", e);
        }
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new MigLayout("insets 28 28 28 28, fill", "[grow]", "[][grow]"));
        root.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]", "[][]"));
        header.setOpaque(false);
        JLabel title = new JLabel("Import Items");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.TEXT_COLOR);
        header.add(title, "wrap");
        JLabel sub = new JLabel("Search, review, and manage imported goods");
        sub.setFont(UIConstants.FONT_REGULAR);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        header.add(sub, "grow");
        root.add(header, "growx, wrap, gapbottom 20");
        root.add(buildTableCard(), "grow");
        return root;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new MigLayout("insets 20, fill", "[grow]", "[][grow][]")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PANEL_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        JPanel toolbar = new JPanel(new MigLayout("insets 0, fillx",
            "[grow][130!][100!][130!][90!][90!]", "[40!]"));
        toolbar.setOpaque(false);

        searchField = styledField();
        searchField.putClientProperty("JTextField.placeholderText", "Search name, importer, category, status...");
        searchField.getDocument().addDocumentListener(filterListener());

        statusFilterCombo = styledStatusCombo();
        statusFilterCombo.addActionListener(e -> applyFilter());

        toolbar.add(searchField, "grow, h 40!");
        toolbar.add(statusFilterCombo, "h 40!");

        addButton     = actionBtn("Add Item", UIConstants.PRIMARY_COLOR);
        editButton    = actionBtn("Edit",     UIConstants.INFO_COLOR);
        deleteButton  = actionBtn("Delete",   UIConstants.ERROR_COLOR);
        refreshButton = actionBtn("Refresh",  UIConstants.BORDER_COLOR);

        addButton.addActionListener(e     -> openAdd());
        editButton.addActionListener(e    -> openEdit());
        deleteButton.addActionListener(e  -> deleteSelected());
        refreshButton.addActionListener(e -> loadItems());

        toolbar.add(addButton,   "h 40!");
        toolbar.add(editButton,  "h 40!");
        toolbar.add(deleteButton,"h 40!");
        card.add(toolbar, "growx, wrap, gapbottom 12");

        tableModel = new DefaultTableModel(new Object[]{
            "ID", "Item Name", "Category", "Qty", "Unit Price",
            "Tax Rate", "Total Tax", "Importer", "Country", "Status", "Date"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0  -> Long.class;
                    case 3  -> Integer.class;
                    case 4, 5, 6 -> java.math.BigDecimal.class;
                    case 10 -> java.time.LocalDate.class;
                    default -> String.class;
                };
            }
        };

        itemTable = new JTable(tableModel) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getRowCount() == 0) {
                    g.setColor(UIConstants.TEXT_MUTED);
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    String msg = "No import records found";
                    int x = (getWidth() - g.getFontMetrics().stringWidth(msg)) / 2;
                    int y = Math.max(60, getHeight() / 2);
                    g.drawString(msg, x, y);
                }
            }
        };
        itemTable.setFont(UIConstants.FONT_REGULAR);
        itemTable.setRowHeight(42);
        itemTable.setGridColor(UIConstants.withAlpha(UIConstants.BORDER_COLOR, 80));
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        itemTable.setRowSorter(new TableRowSorter<>(tableModel));
        itemTable.setFillsViewportHeight(true);
        itemTable.setBackground(UIConstants.PANEL_COLOR);
        itemTable.setForeground(UIConstants.TEXT_COLOR);
        itemTable.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        itemTable.setSelectionForeground(Color.WHITE);
        itemTable.setShowVerticalLines(false);
        itemTable.setIntercellSpacing(new Dimension(0, 0));

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if (i != 9) {
                itemTable.getColumnModel().getColumn(i).setCellRenderer(TableFormatUtil.alternatingRenderer());
            }
        }
        itemTable.getColumnModel().getColumn(9).setCellRenderer(TableFormatUtil.statusRenderer());
        TableFormatUtil.applyCurrencyColumn(itemTable, 4);
        TableFormatUtil.applyCurrencyColumn(itemTable, 5);
        TableFormatUtil.applyCurrencyColumn(itemTable, 6);
        TableFormatUtil.applyDateColumn(itemTable, 10);

        JTableHeader th = itemTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(235, 238, 243));
        th.setForeground(UIConstants.TEXT_SECONDARY);
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(0, 38));

        int[] widths = {60, 200, 140, 70, 110, 100, 110, 170, 140, 110, 120};
        for (int i = 0; i < widths.length; i++)
            itemTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(itemTable);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UIConstants.BORDER_COLOR));
        scroll.getViewport().setBackground(UIConstants.PANEL_COLOR);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setUnitIncrement(18);
        card.add(scroll, "grow, wrap");

        JPanel footer = new JPanel(new MigLayout("insets 0, fillx", "[grow][]", "[]"));
        footer.setOpaque(false);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        footer.add(statusLabel, "grow");
        footer.add(refreshButton, "h 36!");
        card.add(footer, "growx");
        return card;
    }

    private void openAdd() {
        if (!serviceOk()) return;
        ImportItemDialog d = new ImportItemDialog(shell);
        d.setSaveAction((src, item) -> mutate(src, "Saving...",
            () -> importItemService.saveItem(item, CurrentSession.getLoggedInUserId()), "Item saved", false, true));
        d.setVisible(true);
    }

    private void openEdit() {
        ImportItem sel = selected();
        if (sel == null) { status("Select an item to edit", UIConstants.WARNING_COLOR); return; }
        if (!serviceOk()) return;
        ImportItemDialog d = new ImportItemDialog(shell, sel);
        d.setSaveAction((src, item) -> {
            boolean clearing = "CLEARED".equalsIgnoreCase(item.getStatus());
            mutate(src, "Updating...",
                () -> importItemService.updateItem(item, CurrentSession.getLoggedInUserId()),
                clearing ? "Import item cleared" : "Item updated",
                clearing);
        });
        d.setVisible(true);
    }

    private void deleteSelected() {
        ImportItem sel = selected();
        if (sel == null) { status("Select an item to delete", UIConstants.WARNING_COLOR); return; }
        int ok = JOptionPane.showConfirmDialog(shell,
            "Delete \"" + sel.getItemName() + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        mutate(null, "Deleting...", () -> {
            importItemService.deleteItem(sel.getItemId());
            return null;
        }, "Item deleted");
    }

    private void loadItems() {
        if (!serviceOk()) return;
        setLoading(true, "Loading...");
        new SwingWorker<List<ImportItem>, Void>() {
            protected List<ImportItem> doInBackground() throws Exception {
                Long uid = CurrentSession.getLoggedInUserId();
                return uid == null
                    ? importItemService.findAllItems()
                    : importItemService.findItemsByUser(uid);
            }
            protected void done() {
                setLoading(false, " ");
                try {
                    allItems = new ArrayList<>(get());
                    applyFilter();
                } catch (Exception ex) {
                    status(UserMessageUtil.friendly(ex, "Unable to load import items."), UIConstants.ERROR_COLOR);
                }
            }
        }.execute();
    }

    private void mutate(ImportItemDialog dialog, String msg, RemoteMutation fn, String success) {
        mutate(dialog, msg, fn, success, false, false);
    }

    private void mutate(ImportItemDialog dialog, String msg, RemoteMutation fn, String success,
                        boolean cleared) {
        mutate(dialog, msg, fn, success, cleared, false);
    }

    private void mutate(ImportItemDialog dialog, String msg, RemoteMutation fn, String success,
                        boolean cleared, boolean isNew) {
        if (!serviceOk()) return;
        setLoading(true, msg);
        if (dialog != null) dialog.setLoading(true, msg);
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception { fn.run(); return null; }
            protected void done() {
                setLoading(false, " ");
                if (dialog != null) dialog.setLoading(false, " ");
                try {
                    get();
                    if (dialog != null) dialog.dispose();
                    status(success, UIConstants.SUCCESS_COLOR);
                    loadItems();
                    if (cleared) {
                        JOptionPane.showMessageDialog(dialog != null ? dialog : shell,
                                "Import item cleared successfully.",
                                UIConstants.APP_NAME,
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        String msg = isNew ? "Import item saved successfully."
                                : "Import item updated successfully.";
                        JOptionPane.showMessageDialog(dialog != null ? dialog : shell,
                                msg, UIConstants.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    String err = UserMessageUtil.friendly(ex, "Unable to save import item. Please try again.");
                    status(err, UIConstants.ERROR_COLOR);
                    if (dialog != null) dialog.showErrorDialog(err);
                }
            }
        }.execute();
    }

    private void applyFilter() {
        String term = searchField.getText().trim().toLowerCase(Locale.ROOT);
        String statusFilter = statusFilterCombo != null
                ? (String) statusFilterCombo.getSelectedItem() : "ALL";
        List<ImportItem> src = allItems.stream()
            .filter(i -> "ALL".equalsIgnoreCase(statusFilter)
                    || statusFilter.equalsIgnoreCase(i.getStatus()))
            .filter(i -> term.isEmpty() || contains(i.getItemName(), term)
                    || contains(i.getCategory(), term)
                    || contains(i.getImporterName(), term)
                    || contains(i.getStatus(), term))
            .toList();
        displayedItems = new ArrayList<>(src);
        tableModel.setRowCount(0);
        for (ImportItem i : src) {
            tableModel.addRow(new Object[]{
                i.getItemId(), i.getItemName(), i.getCategory(), i.getQuantity(),
                i.getUnitPrice(), i.getTaxRate(), i.getTotalTax(),
                i.getImporterName(), i.getCountryOfOrigin(), i.getStatus(), i.getImportDate()
            });
        }
        String suffix = term.isEmpty() ? "" : " matching \"" + term + "\"";
        statusLabel.setText(src.isEmpty() ? "No records found" : src.size() + " record(s)" + suffix);
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
    }

    private void setupDoubleClick() {
        itemTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openEdit();
            }
        });
    }

    private ImportItem selected() {
        int row = itemTable.getSelectedRow();
        return row < 0 ? null : displayedItems.get(itemTable.convertRowIndexToModel(row));
    }

    private boolean serviceOk() {
        if (importItemService != null) return true;
        status("Service unavailable -- start the RMI server", UIConstants.ERROR_COLOR);
        return false;
    }

    private void setLoading(boolean on, String msg) {
        shell.setCursor(Cursor.getPredefinedCursor(on ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        addButton.setEnabled(!on);
        editButton.setEnabled(!on);
        deleteButton.setEnabled(!on);
        refreshButton.setEnabled(!on);
        status(msg, on ? UIConstants.INFO_COLOR : UIConstants.TEXT_SECONDARY);
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private boolean contains(String val, String term) {
        return val != null && val.toLowerCase(Locale.ROOT).contains(term);
    }

    private DocumentListener filterListener() {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        };
    }

    private JComboBox<String> styledStatusCombo() {
        JComboBox<String> cb = new JComboBox<>(STATUS_FILTER_OPTIONS);
        cb.setFont(UIConstants.FONT_REGULAR);
        cb.setBackground(UIConstants.PANEL_COLOR);
        cb.setForeground(UIConstants.TEXT_COLOR);
        return cb;
    }

    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(UIConstants.FONT_REGULAR);
        f.setBackground(UIConstants.PANEL_COLOR);
        f.setForeground(UIConstants.TEXT_COLOR);
        f.setCaretColor(UIConstants.PRIMARY_COLOR);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIConstants.BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        return f;
    }

    private RoundedButton actionBtn(String text, Color color) {
        RoundedButton b = new RoundedButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setStateColors(color, color.brighter(), color.darker());
        return b;
    }

    @FunctionalInterface
    private interface RemoteMutation { Object run() throws Exception; }

    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private static final Color EVEN = UIConstants.PANEL_COLOR;
        private static final Color ODD  = new Color(235, 238, 243);

        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setBackground(sel ? UIConstants.PRIMARY_COLOR : (row % 2 == 0 ? EVEN : ODD));
            setForeground(sel ? Color.WHITE : UIConstants.TEXT_COLOR);
            return this;
        }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            String s = v == null ? "" : v.toString().trim().toUpperCase(Locale.ROOT);
            setHorizontalAlignment(CENTER);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            if (sel) { setBackground(UIConstants.PRIMARY_COLOR); setForeground(Color.WHITE); return this; }
            setBackground(row % 2 == 0 ? UIConstants.PANEL_COLOR : new Color(235, 238, 243));
            setForeground(switch (s) {
                case "PENDING"                     -> UIConstants.WARNING_COLOR;
                case "PAID"                        -> UIConstants.INFO_COLOR;
                case "CLEARED", "APPROVED"        -> UIConstants.SUCCESS_COLOR;
                case "HOLD"                        -> UIConstants.ERROR_COLOR;
                default                            -> UIConstants.TEXT_COLOR;
            });
            setText(s);
            return this;
        }
    }
}
