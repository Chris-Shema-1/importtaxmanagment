package com.importtax.client.util;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class CsvExporter {

    private CsvExporter() {
    }

    public static File chooseTargetFile(java.awt.Component parent, String suggestedName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export CSV");
        chooser.setSelectedFile(new File(suggestedName));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        int option = chooser.showSaveDialog(parent);
        if (option != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }
        return file;
    }

    public static void exportTable(JTable table, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int col = 0; col < table.getColumnCount(); col++) {
                writer.write(escape(table.getColumnName(col)));
                if (col < table.getColumnCount() - 1) {
                    writer.write(',');
                }
            }
            writer.newLine();

            for (int row = 0; row < table.getRowCount(); row++) {
                for (int col = 0; col < table.getColumnCount(); col++) {
                    Object value = table.getValueAt(row, col);
                    writer.write(escape(value == null ? "" : value.toString()));
                    if (col < table.getColumnCount() - 1) {
                        writer.write(',');
                    }
                }
                writer.newLine();
            }
        }
    }

    private static String escape(String value) {
        String escaped = value.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }
}
