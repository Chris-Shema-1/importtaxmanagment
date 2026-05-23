package com.importtax.client.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Shared JTable formatting: currency, dates, status badges, and row styling.
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

    public static Color statusForeground(String status) {
        if (status == null) {
            return UIConstants.TEXT_COLOR;
        }
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "PENDING" -> UIConstants.WARNING_COLOR;
            case "PAID", "COMPLETED" -> UIConstants.PRIMARY_LIGHT;
            case "CLEARED", "APPROVED" -> UIConstants.SUCCESS_COLOR;
            case "HOLD", "FAILED", "CANCELLED", "REFUNDED" -> UIConstants.ERROR_COLOR;
            default -> UIConstants.TEXT_SECONDARY;
        };
    }

    public static Color statusBadgeBackground(String status) {
        if (status == null) {
            return UIConstants.SIDEBAR_HOVER;
        }
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "PENDING" -> UIConstants.BADGE_PENDING_BG;
            case "PAID", "COMPLETED" -> UIConstants.BADGE_PAID_BG;
            case "CLEARED", "APPROVED" -> UIConstants.BADGE_CLEARED_BG;
            case "HOLD", "FAILED", "CANCELLED", "REFUNDED" -> UIConstants.BADGE_HOLD_BG;
            default -> UIConstants.SIDEBAR_HOVER;
        };
    }

    public static DefaultTableCellRenderer alternatingRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (selected) {
                    setBackground(UIConstants.PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? UIConstants.ROW_EVEN : UIConstants.ROW_ALT);
                    setForeground(UIConstants.TEXT_COLOR);
                }
                return this;
            }
        };
    }

    public static DefaultTableCellRenderer statusBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                String status = value == null ? "" : value.toString().trim().toUpperCase(Locale.ROOT);
                super.getTableCellRendererComponent(table, status, selected, focused, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(6, 14, 6, 14));
                setOpaque(true);
                if (selected) {
                    setBackground(UIConstants.PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(statusBadgeBackground(status));
                    setForeground(statusForeground(status));
                    setFont(UIConstants.FONT_LABEL);
                }
                return this;
            }
        };
    }

    /** @deprecated use {@link #statusBadgeRenderer()} */
    public static DefaultTableCellRenderer statusRenderer() {
        return statusBadgeRenderer();
    }

    public static Color statusColor(String status) {
        return statusForeground(status);
    }

    public static void applyCurrencyColumn(JTable table, int columnIndex) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean sel, boolean foc,
                                                           int row, int col) {
                super.getTableCellRendererComponent(tbl, formatCurrency(value), sel, foc, row, col);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                setHorizontalAlignment(SwingConstants.RIGHT);
                styleRow(this, sel, row);
                return this;
            }
        };
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(r);
    }

    public static void applyDateColumn(JTable table, int columnIndex) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean sel, boolean foc,
                                                           int row, int col) {
                String text = value instanceof LocalDate ld ? formatDate(ld) : (value == null ? "—" : value.toString());
                super.getTableCellRendererComponent(tbl, text, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                styleRow(this, sel, row);
                return this;
            }
        };
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(r);
    }

    private static void styleRow(DefaultTableCellRenderer r, boolean selected, int row) {
        if (selected) {
            r.setBackground(UIConstants.PRIMARY_COLOR);
            r.setForeground(Color.WHITE);
        } else {
            r.setBackground(row % 2 == 0 ? UIConstants.ROW_EVEN : UIConstants.ROW_ALT);
            r.setForeground(UIConstants.TEXT_COLOR);
        }
    }
}
