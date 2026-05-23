package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.UIConstants;
import com.importtax.client.util.UIStyleUtil;
import com.importtax.server.model.Tax;
import com.importtax.server.rmi.TaxService;
import java.awt.*;
import java.math.BigDecimal;
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

public class TaxPage extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(TaxPage.class);

    private final AppShell shell;
    private TaxService taxService;
    private List<Tax> allItems       = new ArrayList<>();
    private List<Tax> displayedItems = new ArrayList<>();

    private JTextField        searchField;
    private JLabel            statusLabel;
    private JTable            table;
    private DefaultTableModel tableModel;
    private RoundedButton     addBtn, editBtn, deleteBtn, refreshBtn;

    public TaxPage(AppShell shell) {
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
            taxService = RmiConnection.lookup(UIConstants.RMI_SERVICE_TAX);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("TaxService unavailable", e);
        }
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new MigLayout("insets 28 28 28 28, fill", "[grow]", "[][grow]"));
        root.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel title = new JLabel("Tax Rates");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.TEXT_COLOR);
        JLabel sub = new JLabel("Define and manage tax rate configurations");
        sub.setFont(UIConstants.FONT_REGULAR);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        JPanel hdr = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
        hdr.setOpaque(false);
        hdr.add(title, "wrap");
        hdr.add(sub);
        root.add(hdr, "growx, wrap, gapbottom 20");
        root.add(buildCard(), "grow");
        return root;
    }

    private JPanel buildCard() {
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

        JPanel toolbar = new JPanel(new MigLayout("insets 0, fillx", "[grow][100!][90!][90!][90!]", "[40!]"));
        toolbar.setOpaque(false);
        searchField = styledField("Search taxes...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filter(); }
            public void removeUpdate(DocumentEvent e)  { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        addBtn     = btn("Add Tax",  UIConstants.PRIMARY_COLOR);
        editBtn    = btn("Edit",     UIConstants.INFO_COLOR);
        deleteBtn  = btn("Delete",   UIConstants.ERROR_COLOR);
        refreshBtn = btn("Refresh",  UIConstants.BORDER_COLOR);
        addBtn.addActionListener(e     -> openForm(null));
        editBtn.addActionListener(e    -> openEdit());
        deleteBtn.addActionListener(e  -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());
        toolbar.add(searchField, "grow, h 40!");
        if (CurrentSession.isAdmin()) {
            toolbar.add(addBtn,    "h 40!");
            toolbar.add(editBtn,   "h 40!");
            toolbar.add(deleteBtn, "h 40!");
        }
        card.add(toolbar, "growx, wrap, gapbottom 12");

        tableModel = new DefaultTableModel(new Object[]{"ID", "Tax Name", "Rate (%)", "Description"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = styledTable(tableModel);
        if (CurrentSession.isAdmin()) {
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) openEdit();
                }
            });
        }
        int[] widths = {60, 220, 120, 400};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = styledScroll(table);
        card.add(scroll, "grow, wrap");

        JPanel footer = new JPanel(new MigLayout("insets 0, fillx", "[grow][]", "[]"));
        footer.setOpaque(false);
        statusLabel = statusLabel();
        footer.add(statusLabel, "grow");
        footer.add(refreshBtn, "h 36!");
        card.add(footer, "growx");
        return card;
    }

    private void openEdit() {
        Tax sel = selected();
        if (sel == null) { setStatus("Select a tax to edit", UIConstants.WARNING_COLOR); return; }
        openForm(sel);
    }

    private void openForm(Tax existing) {
        if (!svcOk()) return;
        JDialog dlg = TaxPage.styledDialog(shell, existing == null ? "Add Tax" : "Edit Tax", 480, 320);
        JPanel form = TaxPage.dialogForm();

        JTextField nameField = styledField("");
        JTextField rateField = styledField("");
        JTextField descField = styledField("");

        if (existing != null) {
            nameField.setText(existing.getTaxName());
            rateField.setText(existing.getTaxRate() != null ? existing.getTaxRate().toPlainString() : "");
            descField.setText(existing.getDescription() != null ? existing.getDescription() : "");
        }

        addRow(form, "Tax Name",    nameField);
        addRow(form, "Rate (%)",    rateField);
        addRow(form, "Description", descField);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(UIConstants.FONT_SMALL);
        errLabel.setForeground(UIConstants.ERROR_COLOR);
        form.add(errLabel, "span, growx, gaptop 4");

        JPanel btns = new JPanel(new MigLayout("insets 12 28 20 28, fillx", "[grow][110!][110!]", "[]"));
        btns.setBackground(UIConstants.BACKGROUND_COLOR);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        RoundedButton cancel = btn("Cancel", UIConstants.BORDER_COLOR);
        RoundedButton save   = btn("Save",   UIConstants.SUCCESS_COLOR);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String name = nameField.getText().trim();
            String rateStr = rateField.getText().trim();
            if (name.isEmpty() || rateStr.isEmpty()) { errLabel.setText("Name and Rate are required"); return; }
            BigDecimal rate;
            try { rate = new BigDecimal(rateStr); } catch (NumberFormatException ex) { errLabel.setText("Rate must be a number"); return; }
            Tax t = existing != null ? existing : new Tax();
            t.setTaxName(name);
            t.setTaxRate(rate);
            t.setDescription(descField.getText().trim());
            mutate(dlg, existing == null ? "Saving..." : "Updating...", () -> {
                if (existing == null) taxService.save(t); else taxService.update(t);
            }, existing == null ? "Tax saved" : "Tax updated");
        });
        btns.add(new JLabel(), "grow");
        btns.add(cancel, "h 42!");
        btns.add(save,   "h 42!");

        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void deleteSelected() {
        Tax sel = selected();
        if (sel == null) { setStatus("Select a tax to delete", UIConstants.WARNING_COLOR); return; }
        int ok = JOptionPane.showConfirmDialog(shell,
            "Delete tax \"" + sel.getTaxName() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        mutate(null, "Deleting...", () -> taxService.delete(sel), "Tax deleted");
    }

    private void loadData() {
        if (!svcOk()) return;
        setStatus("Loading...", UIConstants.INFO_COLOR);
        new SwingWorker<List<Tax>, Void>() {
            protected List<Tax> doInBackground() throws Exception { return taxService.findAll(); }
            protected void done() {
                try { allItems = new ArrayList<>(get()); filter(); }
                catch (Exception ex) { setStatus(rootMsg("Load failed", ex), UIConstants.ERROR_COLOR); }
            }
        }.execute();
    }

    private void mutate(JDialog dlg, String msg, Callable fn, String success) {
        if (!svcOk()) return;
        setStatus(msg, UIConstants.INFO_COLOR);
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception { fn.call(); return null; }
            protected void done() {
                try { get(); if (dlg != null) dlg.dispose(); setStatus(success, UIConstants.SUCCESS_COLOR); loadData(); }
                catch (Exception ex) { setStatus(rootMsg(success + " failed", ex), UIConstants.ERROR_COLOR); }
            }
        }.execute();
    }

    private void filter() {
        String term = searchField.getText().trim().toLowerCase(Locale.ROOT);
        List<Tax> src = term.isEmpty() ? allItems : allItems.stream()
            .filter(t -> contains(t.getTaxName(), term) || contains(t.getDescription(), term))
            .toList();
        displayedItems = new ArrayList<>(src);
        tableModel.setRowCount(0);
        for (Tax t : src)
            tableModel.addRow(new Object[]{t.getTaxId(), t.getTaxName(), t.getTaxRate(), t.getDescription()});
        setStatus(src.size() + " record(s)", UIConstants.TEXT_SECONDARY);
    }

    private Tax selected() {
        int row = table.getSelectedRow();
        return row < 0 ? null : displayedItems.get(table.convertRowIndexToModel(row));
    }

    private boolean svcOk() {
        if (taxService != null) return true;
        setStatus("Service unavailable", UIConstants.ERROR_COLOR); return false;
    }

    private void setStatus(String msg, Color c) { statusLabel.setText(msg); statusLabel.setForeground(c); }
    private boolean contains(String v, String t) { return v != null && v.toLowerCase(Locale.ROOT).contains(t); }
    private String rootMsg(String fb, Exception ex) { Throwable c = ex.getCause() != null ? ex.getCause() : ex; return c.getMessage() != null ? c.getMessage() : fb; }

    // ── Shared UI helpers (delegates to UIStyleUtil) ───────────────────────
    static JTextField styledField(String placeholder) {
        return UIStyleUtil.styledField(placeholder);
    }

    static RoundedButton btn(String text, Color color) {
        return UIStyleUtil.button(text, color);
    }

    static JTable styledTable(DefaultTableModel model) {
        return UIStyleUtil.styledTable(model);
    }

    static JScrollPane styledScroll(JTable t) {
        return UIStyleUtil.styledScroll(t);
    }

    static JLabel statusLabel() {
        return UIStyleUtil.statusLabel();
    }

    static void addRow(JPanel form, String label, JComponent field) {
        UIStyleUtil.addFormRow(form, label, field);
    }

    static JComboBox<String> styledCombo(String[] items) {
        return UIStyleUtil.styledCombo(items);
    }

    static JDialog styledDialog(JFrame owner, String title, int w, int h) {
        return UIStyleUtil.styledDialog(owner, title, w, h);
    }

    static JPanel dialogForm() {
        return UIStyleUtil.dialogForm();
    }

    static JPanel dialogButtons(JFrame owner, JDialog dlg, Runnable onSave, String saveLabel) {
        return UIStyleUtil.dialogButtons(dlg, onSave, saveLabel);
    }

    @FunctionalInterface
    interface Callable { void call() throws Exception; }
}
