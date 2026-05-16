package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.Invoice;
import com.importtax.server.rmi.InvoiceService;
import java.awt.*;
import java.math.BigDecimal;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoicePage extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(InvoicePage.class);

    private final AppShell shell;
    private InvoiceService invoiceService;
    private List<Invoice> allItems       = new ArrayList<>();
    private List<Invoice> displayedItems = new ArrayList<>();

    private JTextField        searchField;
    private JLabel            statusLabel;
    private JTable            table;
    private DefaultTableModel tableModel;

    public InvoicePage(AppShell shell) {
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
            invoiceService = RmiConnection.lookup(UIConstants.RMI_SERVICE_INVOICE);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("InvoiceService unavailable", e);
        }
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new MigLayout("insets 28 28 28 28, fill", "[grow]", "[][grow]"));
        root.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel title = new JLabel("Invoices");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.TEXT_COLOR);
        JLabel sub = new JLabel("View and manage tax invoices");
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
        searchField = TaxPage.styledField("Search invoices...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filter(); }
            public void removeUpdate(DocumentEvent e)  { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        var addBtn     = TaxPage.btn("New Invoice", UIConstants.PRIMARY_COLOR);
        var editBtn    = TaxPage.btn("Edit",        UIConstants.INFO_COLOR);
        var deleteBtn  = TaxPage.btn("Delete",      UIConstants.ERROR_COLOR);
        var refreshBtn = TaxPage.btn("Refresh",     UIConstants.BORDER_COLOR);
        addBtn.addActionListener(e     -> openForm(null));
        editBtn.addActionListener(e    -> openEdit());
        deleteBtn.addActionListener(e  -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());
        toolbar.add(searchField, "grow, h 40!");
        toolbar.add(addBtn,    "h 40!");
        toolbar.add(editBtn,   "h 40!");
        toolbar.add(deleteBtn, "h 40!");
        card.add(toolbar, "growx, wrap, gapbottom 12");

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Invoice #", "Total Tax Amount", "Issue Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = TaxPage.styledTable(tableModel);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openEdit();
            }
        });
        int[] widths = {60, 220, 180, 160};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        card.add(TaxPage.styledScroll(table), "grow, wrap");

        JPanel footer = new JPanel(new MigLayout("insets 0, fillx", "[grow][]", "[]"));
        footer.setOpaque(false);
        statusLabel = TaxPage.statusLabel();
        footer.add(statusLabel, "grow");
        footer.add(refreshBtn, "h 36!");
        card.add(footer, "growx");
        return card;
    }

    private void openEdit() {
        Invoice sel = selected();
        if (sel == null) { setStatus("Select an invoice to edit", UIConstants.WARNING_COLOR); return; }
        openForm(sel);
    }

    private void openForm(Invoice existing) {
        if (!svcOk()) return;
        JDialog dlg = TaxPage.styledDialog(shell, existing == null ? "New Invoice" : "Edit Invoice", 480, 310);
        JPanel form = TaxPage.dialogForm();

        JTextField numField    = TaxPage.styledField("");
        JTextField amountField = TaxPage.styledField("");
        JTextField dateField   = TaxPage.styledField("yyyy-MM-dd");

        if (existing != null) {
            numField.setText(existing.getInvoiceNumber());
            amountField.setText(existing.getTotalTaxAmount() != null ? existing.getTotalTaxAmount().toPlainString() : "");
            dateField.setText(existing.getIssueDate() != null ? existing.getIssueDate().toString() : "");
        } else {
            dateField.setText(LocalDate.now().toString());
        }

        TaxPage.addRow(form, "Invoice #",  numField);
        TaxPage.addRow(form, "Tax Amount", amountField);
        TaxPage.addRow(form, "Issue Date", dateField);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(UIConstants.FONT_SMALL);
        errLabel.setForeground(UIConstants.ERROR_COLOR);
        form.add(errLabel, "span, growx, gaptop 4");

        JPanel btns = new JPanel(new MigLayout("insets 12 28 20 28, fillx", "[grow][110!][110!]", "[]"));
        btns.setBackground(UIConstants.BACKGROUND_COLOR);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        var cancel = TaxPage.btn("Cancel", UIConstants.BORDER_COLOR);
        var save   = TaxPage.btn("Save",   UIConstants.SUCCESS_COLOR);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String num = numField.getText().trim();
            String amtStr = amountField.getText().trim();
            String dateStr = dateField.getText().trim();
            if (num.isEmpty() || amtStr.isEmpty() || dateStr.isEmpty()) { errLabel.setText("All fields required"); return; }
            BigDecimal amt;
            LocalDate date;
            try { amt = new BigDecimal(amtStr); } catch (NumberFormatException ex) { errLabel.setText("Amount must be a number"); return; }
            try { date = LocalDate.parse(dateStr); } catch (DateTimeParseException ex) { errLabel.setText("Date must be yyyy-MM-dd"); return; }
            Invoice inv = existing != null ? existing : new Invoice();
            inv.setInvoiceNumber(num);
            inv.setTotalTaxAmount(amt);
            inv.setIssueDate(date);
            mutate(dlg, existing == null ? "Saving..." : "Updating...", () -> {
                if (existing == null) invoiceService.save(inv); else invoiceService.update(inv);
            }, existing == null ? "Invoice saved" : "Invoice updated");
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
        Invoice sel = selected();
        if (sel == null) { setStatus("Select an invoice to delete", UIConstants.WARNING_COLOR); return; }
        int ok = JOptionPane.showConfirmDialog(shell,
            "Delete invoice \"" + sel.getInvoiceNumber() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        mutate(null, "Deleting...", () -> invoiceService.delete(sel), "Invoice deleted");
    }

    private void loadData() {
        if (!svcOk()) return;
        setStatus("Loading...", UIConstants.INFO_COLOR);
        new SwingWorker<List<Invoice>, Void>() {
            protected List<Invoice> doInBackground() throws Exception { return invoiceService.findAll(); }
            protected void done() {
                try { allItems = new ArrayList<>(get()); filter(); }
                catch (Exception ex) { setStatus(rootMsg("Load failed", ex), UIConstants.ERROR_COLOR); }
            }
        }.execute();
    }

    private void mutate(JDialog dlg, String msg, TaxPage.Callable fn, String success) {
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
        List<Invoice> src = term.isEmpty() ? allItems : allItems.stream()
            .filter(i -> contains(i.getInvoiceNumber(), term))
            .toList();
        displayedItems = new ArrayList<>(src);
        tableModel.setRowCount(0);
        for (Invoice i : src)
            tableModel.addRow(new Object[]{i.getInvoiceId(), i.getInvoiceNumber(), i.getTotalTaxAmount(), i.getIssueDate()});
        setStatus(src.size() + " record(s)", UIConstants.TEXT_SECONDARY);
    }

    private Invoice selected() {
        int row = table.getSelectedRow();
        return row < 0 ? null : displayedItems.get(table.convertRowIndexToModel(row));
    }

    private boolean svcOk() {
        if (invoiceService != null) return true;
        setStatus("Service unavailable", UIConstants.ERROR_COLOR); return false;
    }

    private void setStatus(String msg, Color c) { statusLabel.setText(msg); statusLabel.setForeground(c); }
    private boolean contains(String v, String t) { return v != null && v.toLowerCase(Locale.ROOT).contains(t); }
    private String rootMsg(String fb, Exception ex) { Throwable c = ex.getCause() != null ? ex.getCause() : ex; return c.getMessage() != null ? c.getMessage() : fb; }
}
