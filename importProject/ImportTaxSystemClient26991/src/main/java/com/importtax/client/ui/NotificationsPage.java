package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.Notification;
import com.importtax.server.rmi.NotificationService;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
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

public class NotificationsPage extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(NotificationsPage.class);

    private static final String[] TYPES    = {"INFO", "WARNING", "ALERT", "SYSTEM"};
    private static final String[] STATUSES = {"UNREAD", "READ", "ARCHIVED"};

    private final AppShell shell;
    private NotificationService notifService;
    private List<Notification> allItems       = new ArrayList<>();
    private List<Notification> displayedItems = new ArrayList<>();

    private JTextField        searchField;
    private JLabel            statusLabel;
    private JTable            table;
    private DefaultTableModel tableModel;

    public NotificationsPage(AppShell shell) {
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
            notifService = RmiConnection.lookup(UIConstants.RMI_SERVICE_NOTIFICATION);
        } catch (RemoteException | NotBoundException e) {
            logger.warn("NotificationService unavailable", e);
        }
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new MigLayout("insets 28 28 28 28, fill", "[grow]", "[][grow]"));
        root.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel title = new JLabel("Notifications");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.TEXT_COLOR);
        JLabel sub = new JLabel("System alerts and messages");
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

        JPanel toolbar = new JPanel(new MigLayout("insets 0, fillx", "[grow][120!][90!][90!][90!]", "[40!]"));
        toolbar.setOpaque(false);
        searchField = TaxPage.styledField("Search notifications...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filter(); }
            public void removeUpdate(DocumentEvent e)  { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        var addBtn     = TaxPage.btn("New Notification", UIConstants.PRIMARY_COLOR);
        var deleteBtn  = TaxPage.btn("Delete",           UIConstants.ERROR_COLOR);
        var refreshBtn = TaxPage.btn("Refresh",          UIConstants.BORDER_COLOR);
        addBtn.addActionListener(e     -> openForm());
        deleteBtn.addActionListener(e  -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());
        toolbar.add(searchField, "grow, h 40!");
        toolbar.add(addBtn,    "h 40!");
        toolbar.add(deleteBtn, "h 40!");
        card.add(toolbar, "growx, wrap, gapbottom 12");

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Type", "Recipient", "Message", "Sent At", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = TaxPage.styledTable(tableModel);

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(CENTER);
                String s = v == null ? "" : v.toString().toUpperCase(Locale.ROOT);
                if (sel) { setBackground(UIConstants.PRIMARY_COLOR); setForeground(Color.WHITE); return this; }
                setBackground(row % 2 == 0 ? UIConstants.PANEL_COLOR : new Color(235, 238, 243));
                setForeground(switch (s) {
                    case "READ"     -> UIConstants.SUCCESS_COLOR;
                    case "ARCHIVED" -> UIConstants.TEXT_MUTED;
                    case "UNREAD"   -> UIConstants.WARNING_COLOR;
                    default         -> UIConstants.TEXT_COLOR;
                });
                return this;
            }
        };
        table.getColumnModel().getColumn(5).setCellRenderer(statusRenderer);
        int[] widths = {60, 100, 160, 320, 120, 100};
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

    private void openForm() {
        if (!svcOk()) return;
        JDialog dlg = TaxPage.styledDialog(shell, "New Notification", 500, 360);
        JPanel form = TaxPage.dialogForm();

        JTextField recipientField = TaxPage.styledField("");
        JTextField messageField   = TaxPage.styledField("");
        JComboBox<String> typeBox   = TaxPage.styledCombo(TYPES);
        JComboBox<String> statusBox = TaxPage.styledCombo(STATUSES);

        TaxPage.addRow(form, "Recipient", recipientField);
        TaxPage.addRow(form, "Message",   messageField);
        TaxPage.addRow(form, "Type",      typeBox);
        TaxPage.addRow(form, "Status",    statusBox);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(UIConstants.FONT_SMALL);
        errLabel.setForeground(UIConstants.ERROR_COLOR);
        form.add(errLabel, "span, growx, gaptop 4");

        JPanel btns = new JPanel(new MigLayout("insets 12 28 20 28, fillx", "[grow][110!][110!]", "[]"));
        btns.setBackground(UIConstants.BACKGROUND_COLOR);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        var cancel = TaxPage.btn("Cancel", UIConstants.BORDER_COLOR);
        var save   = TaxPage.btn("Send",   UIConstants.SUCCESS_COLOR);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String recipient = recipientField.getText().trim();
            String message   = messageField.getText().trim();
            if (recipient.isEmpty() || message.isEmpty()) { errLabel.setText("Recipient and message required"); return; }
            Notification n = new Notification(message, (String) typeBox.getSelectedItem(),
                recipient, LocalDate.now(), (String) statusBox.getSelectedItem());
            mutate(dlg, "Sending...", () -> notifService.save(n), "Notification sent");
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
        Notification sel = selected();
        if (sel == null) { setStatus("Select a notification to delete", UIConstants.WARNING_COLOR); return; }
        int ok = JOptionPane.showConfirmDialog(shell,
            "Delete this notification?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        mutate(null, "Deleting...", () -> notifService.delete(sel), "Notification deleted");
    }

    private void loadData() {
        if (!svcOk()) return;
        setStatus("Loading...", UIConstants.INFO_COLOR);
        new SwingWorker<List<Notification>, Void>() {
            protected List<Notification> doInBackground() throws Exception { return notifService.findAll(); }
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
        List<Notification> src = term.isEmpty() ? allItems : allItems.stream()
            .filter(n -> contains(n.getRecipient(), term) || contains(n.getMessage(), term)
                      || contains(n.getNotificationType(), term))
            .toList();
        displayedItems = new ArrayList<>(src);
        tableModel.setRowCount(0);
        for (Notification n : src)
            tableModel.addRow(new Object[]{n.getNotificationId(), n.getNotificationType(),
                n.getRecipient(), n.getMessage(), n.getSentAt(), n.getStatus()});
        setStatus(src.size() + " record(s)", UIConstants.TEXT_SECONDARY);
    }

    private Notification selected() {
        int row = table.getSelectedRow();
        return row < 0 ? null : displayedItems.get(table.convertRowIndexToModel(row));
    }

    private boolean svcOk() {
        if (notifService != null) return true;
        setStatus("Service unavailable", UIConstants.ERROR_COLOR); return false;
    }

    private void setStatus(String msg, Color c) { statusLabel.setText(msg); statusLabel.setForeground(c); }
    private boolean contains(String v, String t) { return v != null && v.toLowerCase(Locale.ROOT).contains(t); }
    private String rootMsg(String fb, Exception ex) { Throwable c = ex.getCause() != null ? ex.getCause() : ex; return c.getMessage() != null ? c.getMessage() : fb; }

    private static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(UIConstants.FONT_REGULAR);
        cb.setBackground(UIConstants.PANEL_COLOR);
        cb.setForeground(UIConstants.TEXT_COLOR);
        return cb;
    }

    private static void addComboRow(JPanel form, String label, JComboBox<String> cb) {
        JLabel l = new JLabel(label);
        l.setFont(UIConstants.FONT_LABEL);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(l, "aligny center, gapy 8 0");
        form.add(cb, "h 40!, growx, wrap, gapbottom 4");
    }
}
