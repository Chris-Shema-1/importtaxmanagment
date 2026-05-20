package com.importtax.client.ui;

import com.importtax.client.rmi.RmiConnection;
import com.importtax.client.util.CsvExporter;
import com.importtax.client.util.CurrentSession;
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;
import com.importtax.server.model.User;
import com.importtax.server.rmi.UserService;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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

public class UserManagementFrame extends JFrame {

    private UserService userService;
    private final List<User> allUsers = new ArrayList<>();
    private JTextField searchField;
    private JLabel statusLabel;
    private JTable table;
    private DefaultTableModel model;

    public UserManagementFrame() {
        initializeService();
        initializeFrame();
        setContentPane(createContent());
        loadUsers();
    }

    private void initializeService() {
        try {
            RmiConnection.initialize();
            userService = RmiConnection.lookup(UIConstants.RMI_SERVICE_USER);
        } catch (RemoteException | NotBoundException ex) {
            userService = null;
        }
    }

    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " - User Directory");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new Dimension(1020, 640));
        setLocationRelativeTo(null);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][pref]", ""));
        header.setOpaque(false);
        JLabel title = new JLabel("Users");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UIConstants.TEXT_COLOR);
        header.add(title);
        RoundedButton back = new RoundedButton("Back to Dashboard");
        back.setStateColors(UIConstants.PRIMARY_DARK,
                UIConstants.PRIMARY_DARK.brighter(), UIConstants.PRIMARY_DARK.darker());
        back.addActionListener(e -> {
            dispose();
            new DashboardFrame(CurrentSession.getUsername()).setVisible(true);
        });
        header.add(back, "h 42!");
        root.add(header, BorderLayout.NORTH);

        RoundedPanel panel = new RoundedPanel(8, 8, new Color(34, 44, 56));
        panel.setLayout(new MigLayout("insets 18, fill", "[grow]", "[][grow][]"));

        JPanel toolbar = new JPanel(new MigLayout("insets 0, fillx", "[grow][120!][130!]", ""));
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
        RoundedButton createUser = new RoundedButton("Create User");
        createUser.setStateColors(UIConstants.SUCCESS_COLOR,
                UIConstants.SUCCESS_COLOR.brighter(), UIConstants.SUCCESS_COLOR.darker());
        createUser.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
        RoundedButton export = new RoundedButton("Export");
        export.setStateColors(UIConstants.ACCENT_COLOR,
                UIConstants.ACCENT_COLOR.brighter(), UIConstants.ACCENT_COLOR.darker());
        export.addActionListener(e -> exportCsv());
        toolbar.add(createUser, "h 40!");
        toolbar.add(export, "h 40!, wrap");
        panel.add(toolbar, "grow, wrap, gapbottom 10");

        model = new DefaultTableModel(new Object[]{"ID", "Full Name", "Email", "Username", "Role", "Created"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(38);
        table.setBackground(UIConstants.PANEL_COLOR);
        table.setForeground(UIConstants.TEXT_COLOR);
        table.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        table.setSelectionForeground(java.awt.Color.WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.setDefaultRenderer(String.class, new AlternatingRowRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new RoleCellRenderer());
        javax.swing.table.JTableHeader userHeader = table.getTableHeader();
        userHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userHeader.setBackground(new java.awt.Color(35, 35, 45));
        userHeader.setForeground(UIConstants.TEXT_COLOR);
        userHeader.setReorderingAllowed(false);
        panel.add(new JScrollPane(table), "grow, wrap");

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(UIConstants.TEXT_SECONDARY);
        panel.add(statusLabel, "growx");
        root.add(panel, BorderLayout.CENTER);
        return root;
    }

    private void loadUsers() {
        if (userService == null) {
            showError("User service is unavailable. Start the RMI server and try again.");
            return;
        }
        setLoading(true, "Loading users...");
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userService.findAll();
            }

            @Override
            protected void done() {
                try {
                    allUsers.clear();
                    allUsers.addAll(get());
                    filter();
                    setLoading(false, allUsers.size() + " user(s) loaded");
                } catch (Exception ex) {
                    showError(extract(ex, "Could not load users"));
                }
            }
        }.execute();
    }

    private void filter() {
        String term = searchField.getText().trim().toLowerCase(Locale.ROOT);
        model.setRowCount(0);
        for (User user : allUsers) {
            if (!term.isEmpty()
                    && !safe(user.getFullName()).contains(term)
                    && !safe(user.getEmail()).contains(term)
                    && !safe(user.getUsername()).contains(term)
                    && !safe(user.getRole()).contains(term)) {
                continue;
            }
            model.addRow(new Object[]{
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getRole(),
                    user.getCreatedAt()
            });
        }
    }

    private void exportCsv() {
        File file = CsvExporter.chooseTargetFile(this, "users.csv");
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

    private void setLoading(boolean loading, String message) {
        setCursor(Cursor.getPredefinedCursor(loading ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        statusLabel.setText(message);
        statusLabel.setForeground(loading ? UIConstants.INFO_COLOR : UIConstants.TEXT_SECONDARY);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(UIConstants.ERROR_COLOR);
        JOptionPane.showMessageDialog(this, message, "User Management", JOptionPane.ERROR_MESSAGE);
    }

    private String extract(Exception ex, String fallback) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() == null ? fallback : cause.getMessage();
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static class AlternatingRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private static final java.awt.Color EVEN = UIConstants.PANEL_COLOR;
        private static final java.awt.Color ODD  = new java.awt.Color(38, 50, 62);

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new javax.swing.border.EmptyBorder(0, 10, 0, 10));
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

    private static class RoleCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new javax.swing.border.EmptyBorder(0, 10, 0, 10));
            String role = value == null ? "" : value.toString().toUpperCase(Locale.ROOT);
            if (isSelected) {
                setBackground(UIConstants.PRIMARY_COLOR);
                setForeground(java.awt.Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? UIConstants.PANEL_COLOR : new java.awt.Color(38, 50, 62));
                setForeground(switch (role) {
                    case "ADMIN" -> UIConstants.ERROR_COLOR;
                    case "FINANCE_OFFICER" -> UIConstants.ACCENT_COLOR;
                    case "CUSTOMS_OFFICER" -> UIConstants.INFO_COLOR;
                    default -> UIConstants.TEXT_COLOR;
                });
            }
            return this;
        }
    }
}
