package com.importtax.client.util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Shared JTable formatting: currency, dates, and import/payment status colors.
 */
public final class TableFormatUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final NumberFormat CURRENCY_FMT = NumberFormat.getCurrencyInstance(Locale.US);

    private TableFormatUtil() {
    }

    public static String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        return CURRENCY_FMT.format(value);
    }

    public static String formatCurrency(Object value) {
        if (value instanceof BigDecimal bd) {
            return formatCurrency(bd);
        }
        if (value == null) {
            return "—";
        }
        return value.toString();
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "—" : date.format(DATE_FMT);
    }

    public static Color statusColor(String status) {
        if (status == null) {
            return UIConstants.TEXT_COLOR;
        }
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "PENDING"   -> UIConstants.WARNING_COLOR;
            case "PAID", "COMPLETED" -> new Color(30, 120, 180);
            case "CLEARED"   -> UIConstants.SUCCESS_COLOR;
            case "HOLD", "FAILED", "CANCELLED" -> UIConstants.ERROR_COLOR;
            default          -> UIConstants.TEXT_COLOR;
        };
    }

    public static DefaultTableCellRenderer alternatingRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (selected) {
                    setBackground(UIConstants.PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? UIConstants.PANEL_COLOR : new Color(235, 238, 243));
                    setForeground(UIConstants.TEXT_COLOR);
                }
                return this;
            }
        };
    }

    public static DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(SwingConstants.CENTER);
                String status = value == null ? "" : value.toString();
                if (selected) {
                    setBackground(UIConstants.PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? UIConstants.PANEL_COLOR : new Color(235, 238, 243));
                    setForeground(statusColor(status));
                }
                return this;
            }
        };
    }

    public static void applyCurrencyColumn(JTable table, int columnIndex) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean sel, boolean foc,
                                                           int row, int col) {
                super.getTableCellRendererComponent(tbl, formatCurrency(value), sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (sel) {
                    setBackground(UIConstants.PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? UIConstants.PANEL_COLOR : new Color(235, 238, 243));
                    setForeground(UIConstants.TEXT_COLOR);
                }
                return this;
            }
        };
        TableColumn col = table.getColumnModel().getColumn(columnIndex);
        col.setCellRenderer(r);
    }

    public static void applyDateColumn(JTable table, int columnIndex) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean sel, boolean foc,
                                                           int row, int col) {
                String text = value instanceof LocalDate ld ? formatDate(ld) : (value == null ? "—" : value.toString());
                super.getTableCellRendererComponent(tbl, text, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (sel) {
                    setBackground(UIConstants.PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? UIConstants.PANEL_COLOR : new Color(235, 238, 243));
                    setForeground(UIConstants.TEXT_COLOR);
                }
                return this;
            }
        };
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(r);
    }
}
