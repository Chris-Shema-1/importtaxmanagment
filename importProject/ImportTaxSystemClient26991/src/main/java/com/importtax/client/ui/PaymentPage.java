package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.TableFormatUtil;
import com.importtax.client.util.UserMessageUtil;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.Payment;
import com.importtax.server.rmi.InvoiceService;
import com.importtax.server.rmi.PaymentService;
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

public class PaymentPage extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(PaymentPage.class);

    private static final String[] METHODS  = {"BANK_TRANSFER", "CREDIT_CARD", "CASH", "CHEQUE"};
    private static final String[] STATUSES = {"PENDING", "COMPLETED", "FAILED", "REFUNDED"};

    private final AppShell shell;
    private PaymentService  paymentService;
    private InvoiceService  invoiceService;
    private List<Payment> allItems       = new ArrayList<>();
    private List<Payment> displayedItems = new ArrayList<>();

    private static final String[] PAYMENT_STATUS_FILTER = {"ALL", "PENDING", "COMPLETED", "FAILED", "REFUNDED"};

    private JTextField        searchField;
    private JComboBox<String> statusFilterCombo;
    private JLabel            statusLabel;
    private JTable            table;
    private DefaultTableModel tableModel;

    public PaymentPage(AppShell shell) {
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
            paymentService  = RmiConnection.lookup(UIConstants.RMI_SERVICE_PAYMENT);
            invoiceService  = RmiConnection.lookup(UIConstants.RMI_SERVICE_INVOICE);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("PaymentService unavailable", e);
        }
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new MigLayout("insets 28 28 28 28, fill", "[grow]", "[][grow]"));
        root.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel title = new JLabel("Payments");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.TEXT_COLOR);
        JLabel sub = new JLabel("Track and manage payment transactions");
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

        JPanel toolbar = new JPanel(new MigLayout("insets 0, fillx",
                "[grow][130!][110!][90!][90!][90!]", "[40!]"));
        toolbar.setOpaque(false);
        searchField = TaxPage.styledField("Search invoice #, method, status...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filter(); }
            public void removeUpdate(DocumentEvent e)  { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        statusFilterCombo = styledCombo(PAYMENT_STATUS_FILTER);
        statusFilterCombo.addActionListener(e -> filter());
        var addBtn     = TaxPage.btn("New Payment", UIConstants.PRIMARY_COLOR);
        var editBtn    = TaxPage.btn("Edit",        UIConstants.INFO_COLOR);
        var deleteBtn  = TaxPage.btn("Delete",      UIConstants.ERROR_COLOR);
        var refreshBtn = TaxPage.btn("Refresh",     UIConstants.BORDER_COLOR);
        addBtn.addActionListener(e     -> openForm(null));
        editBtn.addActionListener(e    -> openEdit());
        deleteBtn.addActionListener(e  -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());
        toolbar.add(searchField, "grow, h 40!");
        toolbar.add(statusFilterCombo, "h 40!");
        toolbar.add(addBtn,    "h 40!");
        toolbar.add(editBtn,   "h 40!");
        toolbar.add(deleteBtn, "h 40!");
        card.add(toolbar, "growx, wrap, gapbottom 12");

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Amount Paid", "Date", "Method", "Status", "Invoice ID"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = TaxPage.styledTable(tableModel);
        TableFormatUtil.applyCurrencyColumn(table, 1);
        TableFormatUtil.applyDateColumn(table, 2);
        table.getColumnModel().getColumn(4).setCellRenderer(TableFormatUtil.statusRenderer());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openEdit();
            }
        });
        int[] widths = {60, 150, 130, 160, 130, 100};
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
        Payment sel = selected();
        if (sel == null) { setStatus("Select a payment to edit", UIConstants.WARNING_COLOR); return; }
        openForm(sel);
    }

    private void openForm(Payment existing) {
        if (!svcOk()) return;

        // load invoices for dropdown first
        List<Invoice> invoices;
        try {
            invoices = invoiceService.findAll();
        } catch (Exception ex) {
            setStatus("Failed to load invoices", UIConstants.ERROR_COLOR);
            return;
        }
        if (invoices == null || invoices.isEmpty()) {
            setStatus("No invoices available — create an invoice first", UIConstants.WARNING_COLOR);
            return;
        }

        JDialog dlg = TaxPage.styledDialog(shell, existing == null ? "New Payment" : "Edit Payment", 500, 400);
        JPanel form = TaxPage.dialogForm();

        JTextField amountField       = TaxPage.styledField("");
        JTextField dateField         = TaxPage.styledField("yyyy-MM-dd");
        JComboBox<String> methodBox  = TaxPage.styledCombo(METHODS);
        JComboBox<String> statusBox  = TaxPage.styledCombo(STATUSES);

        // invoice dropdown — shows "#INV-001 ($500.00)" style labels
        JComboBox<Invoice> invoiceBox = new JComboBox<>();
        invoiceBox.setFont(UIConstants.FONT_REGULAR);
        invoiceBox.setBackground(UIConstants.PANEL_COLOR);
        invoiceBox.setForeground(UIConstants.TEXT_COLOR);
        invoiceBox.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Invoice inv)
                    setText("#" + inv.getInvoiceNumber() + "  ($" +
                        (inv.getTotalTaxAmount() != null ? inv.getTotalTaxAmount().toPlainString() : "0") + ")");
                return this;
            }
        });
        for (Invoice inv : invoices) invoiceBox.addItem(inv);

        if (existing != null) {
            amountField.setText(existing.getAmountPaid() != null ? existing.getAmountPaid().toPlainString() : "");
            dateField.setText(existing.getPaymentDate() != null ? existing.getPaymentDate().toString() : "");
            methodBox.setSelectedItem(existing.getPaymentMethod());
            statusBox.setSelectedItem(existing.getPaymentStatus());
            if (existing.getInvoice() != null) {
                invoices.stream()
                    .filter(inv -> inv.getInvoiceId().equals(existing.getInvoice().getInvoiceId()))
                    .findFirst().ifPresent(invoiceBox::setSelectedItem);
            }
        } else {
            dateField.setText(LocalDate.now().toString());
        }

        TaxPage.addRow(form, "Amount Paid",  amountField);
        TaxPage.addRow(form, "Payment Date", dateField);
        TaxPage.addRow(form, "Method",       methodBox);
        TaxPage.addRow(form, "Status",       statusBox);
        TaxPage.addRow(form, "Invoice",      invoiceBox);

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
            String amtStr  = amountField.getText().trim();
            String dateStr = dateField.getText().trim();
            String method  = (String) methodBox.getSelectedItem();
            String status  = (String) statusBox.getSelectedItem();
            Invoice selInv = (Invoice) invoiceBox.getSelectedItem();
            if (amtStr.isEmpty() || dateStr.isEmpty() || selInv == null) { errLabel.setText("All fields required"); return; }
            BigDecimal amt;
            LocalDate date;
            try { amt = new BigDecimal(amtStr); } catch (NumberFormatException ex) { errLabel.setText("Amount must be a number"); return; }
            try { date = LocalDate.parse(dateStr); } catch (DateTimeParseException ex) { errLabel.setText("Date must be yyyy-MM-dd"); return; }
            boolean completed = "COMPLETED".equalsIgnoreCase(status);
            mutate(dlg, save, cancel, existing == null ? "Saving..." : "Updating...", () -> {
                Payment p = existing != null ? existing : new Payment();
                p.setAmountPaid(amt);
                p.setPaymentDate(date);
                p.setPaymentMethod(method);
                p.setPaymentStatus(status);
                p.setInvoice(selInv);
                Long callerId = CurrentSession.getLoggedInUserId();
                if (existing == null) {
                    paymentService.savePaymentSecure(p, callerId);
                } else {
                    paymentService.updatePaymentSecure(p, callerId);
                }
            }, existing == null ? "Payment saved" : "Payment updated", completed);
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
        Payment sel = selected();
        if (sel == null) { setStatus("Select a payment to delete", UIConstants.WARNING_COLOR); return; }
        int ok = JOptionPane.showConfirmDialog(shell,
            "Delete payment #" + sel.getPaymentId() + "? This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        mutate(null, null, null, "Deleting...", () ->
            paymentService.deletePaymentSecure(sel.getPaymentId(), CurrentSession.getLoggedInUserId()),
            "Payment deleted");
    }

    private void loadData() {
        if (!svcOk()) return;
        setStatus("Loading...", UIConstants.INFO_COLOR);
        new SwingWorker<List<Payment>, Void>() {
            protected List<Payment> doInBackground() throws Exception { return paymentService.findAll(); }
            protected void done() {
                try { allItems = new ArrayList<>(get()); filter(); }
                catch (Exception ex) {
                    setStatus(UserMessageUtil.friendly(ex, "Unable to load payments."), UIConstants.ERROR_COLOR);
                }
            }
        }.execute();
    }

    private void mutate(JDialog dlg, JButton saveBtn, JButton cancelBtn, String msg, TaxPage.Callable fn, String success) {
        mutate(dlg, saveBtn, cancelBtn, msg, fn, success, false);
    }

    private void mutate(JDialog dlg, JButton saveBtn, JButton cancelBtn, String msg, TaxPage.Callable fn,
                        String success, boolean showPaymentSuccess) {
        if (!svcOk()) return;
        if (saveBtn != null) saveBtn.setEnabled(false);
        if (cancelBtn != null) cancelBtn.setEnabled(false);
        shell.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (dlg != null) dlg.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus(msg, UIConstants.INFO_COLOR);
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                fn.call();
                return null;
            }
            protected void done() {
                shell.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                if (dlg != null) dlg.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                if (saveBtn != null) saveBtn.setEnabled(true);
                if (cancelBtn != null) cancelBtn.setEnabled(true);
                try {
                    get();
                    if (dlg != null) dlg.dispose();
                    setStatus(success, UIConstants.SUCCESS_COLOR);
                    loadData();
                    if (showPaymentSuccess) {
                        JOptionPane.showMessageDialog(dlg != null ? dlg : shell,
                                "Payment completed successfully.",
                                "Payments",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String detail = UserMessageUtil.friendly(ex, "Unable to save payment. Please try again.");
                    setStatus(detail, UIConstants.ERROR_COLOR);
                    JOptionPane.showMessageDialog(dlg != null ? dlg : shell, detail,
                            UIConstants.APP_NAME, JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void filter() {
        String term = searchField.getText().trim().toLowerCase(Locale.ROOT);
        String statusFilter = statusFilterCombo != null
                ? (String) statusFilterCombo.getSelectedItem() : "ALL";
        List<Payment> src = allItems.stream()
            .filter(p -> "ALL".equalsIgnoreCase(statusFilter)
                    || statusFilter.equalsIgnoreCase(p.getPaymentStatus()))
            .filter(p -> term.isEmpty() || contains(p.getPaymentMethod(), term)
                    || contains(p.getPaymentStatus(), term)
                    || contains(invoiceSearchText(p), term))
            .toList();
        displayedItems = new ArrayList<>(src);
        tableModel.setRowCount(0);
        for (Payment p : src) {
            Long invId = p.getInvoice() != null ? p.getInvoice().getInvoiceId() : null;
            tableModel.addRow(new Object[]{p.getPaymentId(), p.getAmountPaid(), p.getPaymentDate(),
                p.getPaymentMethod(), p.getPaymentStatus(), invId});
        }
        setStatus(src.size() + " record(s)", UIConstants.TEXT_SECONDARY);
    }

    private Payment selected() {
        int row = table.getSelectedRow();
        return row < 0 ? null : displayedItems.get(table.convertRowIndexToModel(row));
    }

    private boolean svcOk() {
        if (paymentService != null) return true;
        setStatus("Service unavailable", UIConstants.ERROR_COLOR); return false;
    }

    private void setStatus(String msg, Color c) { statusLabel.setText(msg); statusLabel.setForeground(c); }
    private boolean contains(String v, String t) { return v != null && v.toLowerCase(Locale.ROOT).contains(t); }
    private String invoiceSearchText(Payment payment) {
        if (payment == null || payment.getInvoice() == null) {
            return "";
        }
        String number = payment.getInvoice().getInvoiceNumber();
        Long id = payment.getInvoice().getInvoiceId();
        return (number != null ? number : "") + " " + (id != null ? id.toString() : "");
    }

    private static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(UIConstants.FONT_REGULAR);
        cb.setBackground(UIConstants.PANEL_COLOR);
        cb.setForeground(UIConstants.TEXT_COLOR);
        return cb;
    }

    private static void addComboRow(JPanel form, String label, JComboBox<?> cb) {
        JLabel l = new JLabel(label);
        l.setFont(UIConstants.FONT_LABEL);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(l, "aligny center, gapy 8 0");
        form.add(cb, "h 40!, growx, wrap, gapbottom 4");
    }
}
