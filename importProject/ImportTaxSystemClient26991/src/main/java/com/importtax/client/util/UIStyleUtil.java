package com.importtax.client.util;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 * Centralized Swing styling helpers used across all client pages.
 */
public final class UIStyleUtil {

    private UIStyleUtil() {
    }

    // ── Cards & panels ─────────────────────────────────────────────────────

    public static JPanel contentCard() {
        return new JPanel(new MigLayout("insets 20, fill", "[grow]", "[][grow][]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PANEL_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        UIConstants.BORDER_RADIUS_LARGE, UIConstants.BORDER_RADIUS_LARGE);
                g2.setColor(UIConstants.withAlpha(Color.WHITE, 12));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        UIConstants.BORDER_RADIUS_LARGE, UIConstants.BORDER_RADIUS_LARGE);
                g2.dispose();
            }
        };
    }

    public static JPanel pageHeader(String title, String subtitle) {
        JPanel hdr = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
        hdr.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(UIConstants.FONT_TITLE);
        t.setForeground(UIConstants.TEXT_COLOR);
        hdr.add(t, "wrap");
        if (subtitle != null && !subtitle.isBlank()) {
            JLabel s = new JLabel(subtitle);
            s.setFont(UIConstants.FONT_REGULAR);
            s.setForeground(UIConstants.TEXT_SECONDARY);
            hdr.add(s);
        }
        return hdr;
    }

    public static JPanel authCard(JPanel inner) {
        JPanel wrap = new JPanel(new MigLayout("fill, insets 0", "[center, grow]", "[center, grow]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PANEL_COLOR);
                int pad = 24;
                g2.fillRoundRect(pad, pad, getWidth() - pad * 2, getHeight() - pad * 2,
                        UIConstants.BORDER_RADIUS_LARGE, UIConstants.BORDER_RADIUS_LARGE);
                g2.setColor(UIConstants.withAlpha(UIConstants.PRIMARY_COLOR, 30));
                g2.drawRoundRect(pad, pad, getWidth() - pad * 2 - 1, getHeight() - pad * 2 - 1,
                        UIConstants.BORDER_RADIUS_LARGE, UIConstants.BORDER_RADIUS_LARGE);
                g2.dispose();
            }
        };
        wrap.setOpaque(false);
        inner.setOpaque(false);
        wrap.add(inner, "grow, push");
        return wrap;
    }

    // ── Inputs ─────────────────────────────────────────────────────────────

    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(UIConstants.FONT_REGULAR);
        f.setBackground(UIConstants.PANEL_COLOR);
        f.setForeground(UIConstants.TEXT_COLOR);
        f.setCaretColor(UIConstants.PRIMARY_LIGHT);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setBorder(compoundInputBorder(false));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(compoundInputBorder(true));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(compoundInputBorder(false));
            }
        });
        return f;
    }

    public static JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(UIConstants.FONT_REGULAR);
        f.setBackground(UIConstants.PANEL_COLOR);
        f.setForeground(UIConstants.TEXT_COLOR);
        f.setCaretColor(UIConstants.PRIMARY_LIGHT);
        f.setEchoChar('●');
        f.setBorder(compoundInputBorder(false));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(compoundInputBorder(true));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(compoundInputBorder(false));
            }
        });
        return f;
    }

    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(UIConstants.FONT_REGULAR);
        cb.setBackground(UIConstants.PANEL_COLOR);
        cb.setForeground(UIConstants.TEXT_COLOR);
        cb.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true));
        return cb;
    }

    private static javax.swing.border.Border compoundInputBorder(boolean focused) {
        Color border = focused ? UIConstants.PRIMARY_COLOR : UIConstants.BORDER_COLOR;
        return BorderFactory.createCompoundBorder(
                new LineBorder(border, focused ? 2 : 1, true),
                new EmptyBorder(10, 14, 10, 14));
    }

    // ── Buttons ────────────────────────────────────────────────────────────

    public static RoundedButton primaryButton(String text) {
        return button(text, UIConstants.PRIMARY_COLOR);
    }

    public static RoundedButton successButton(String text) {
        return button(text, UIConstants.SUCCESS_COLOR);
    }

    public static RoundedButton dangerButton(String text) {
        return button(text, UIConstants.ERROR_COLOR);
    }

    public static RoundedButton secondaryButton(String text) {
        return button(text, UIConstants.SIDEBAR_HOVER);
    }

    public static RoundedButton button(String text, Color color) {
        RoundedButton b = new RoundedButton(text, UIConstants.BORDER_RADIUS,
                color, UIConstants.blend(color, Color.WHITE, 0.15f), Color.WHITE);
        b.setStateColors(color, UIConstants.blend(color, Color.WHITE, 0.12f), color.darker());
        b.setFont(UIConstants.FONT_BUTTON);
        return b;
    }

    // ── Tables ─────────────────────────────────────────────────────────────

    public static JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row) && c instanceof JComponent jc) {
                    jc.setOpaque(true);
                }
                return c;
            }
        };
        t.setFont(UIConstants.FONT_REGULAR);
        t.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        t.setGridColor(UIConstants.withAlpha(UIConstants.BORDER_COLOR, 60));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        t.setRowSorter(new TableRowSorter<>(model));
        t.setFillsViewportHeight(true);
        t.setBackground(UIConstants.PANEL_COLOR);
        t.setForeground(UIConstants.TEXT_COLOR);
        t.setSelectionBackground(UIConstants.PRIMARY_COLOR);
        t.setSelectionForeground(Color.WHITE);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setShowHorizontalLines(true);

        DefaultTableCellRenderer base = TableFormatUtil.alternatingRenderer();
        for (int i = 0; i < model.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(base);
        }

        JTableHeader th = t.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(UIConstants.SIDEBAR_BG);
        th.setForeground(UIConstants.TEXT_SECONDARY);
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(0, 42));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));
        return t;
    }

    public static JScrollPane styledScroll(JTable table) {
        JScrollPane s = new JScrollPane(table);
        s.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UIConstants.BORDER_COLOR));
        s.getViewport().setBackground(UIConstants.PANEL_COLOR);
        s.setOpaque(false);
        s.getViewport().setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(18);
        s.getHorizontalScrollBar().setUnitIncrement(18);
        return s;
    }

    public static JLabel statusLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(UIConstants.FONT_SMALL);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        return l;
    }

    // ── Dialogs ────────────────────────────────────────────────────────────

    public static void addFormRow(JPanel form, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setFont(UIConstants.FONT_LABEL);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        form.add(l, "aligny center, gapy 8 0");
        form.add(field, "h " + UIConstants.INPUT_HEIGHT + "!, growx, wrap, gapbottom 4");
    }

    public static JDialog styledDialog(JFrame owner, String title, int w, int h) {
        JDialog dlg = new JDialog(owner, title, true);
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(owner);
        dlg.getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        return dlg;
    }

    public static JPanel dialogForm() {
        JPanel form = new JPanel(new MigLayout("insets 24 28 8 28, fillx", "[150!][grow]", ""));
        form.setBackground(UIConstants.BACKGROUND_COLOR);
        return form;
    }

    public static JPanel dialogButtons(JDialog dlg, Runnable onSave, String saveLabel) {
        JPanel btns = new JPanel(new MigLayout("insets 12 28 20 28, fillx", "[grow][110!][110!]", "[]"));
        btns.setBackground(UIConstants.BACKGROUND_COLOR);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        RoundedButton cancel = secondaryButton("Cancel");
        RoundedButton save = successButton(saveLabel);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> onSave.run());
        btns.add(new JLabel(), "grow");
        btns.add(cancel, "h 42!");
        btns.add(save, "h 42!");
        return btns;
    }
}
