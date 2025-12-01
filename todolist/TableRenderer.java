package todolist;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TableRenderer extends DefaultTableCellRenderer {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DefaultTableModel model;

    public TableRenderer(DefaultTableModel model) {
        this.model = model;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int col) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        int mr = table.convertRowIndexToModel(row);
        boolean done = Boolean.TRUE.equals(model.getValueAt(mr, 0));
        int daysLeft = Integer.MAX_VALUE;
        boolean overdue = false;

        try {
            Object dl = model.getValueAt(mr, 3);
            if (dl != null && !dl.toString().trim().isEmpty()) {
                LocalDate d = LocalDate.parse(dl.toString(), DATE_FMT);
                daysLeft = (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), d);
                overdue = daysLeft < 0;
            }
        } catch (Exception ex) {}

        if (isSelected) {
            c.setForeground(new Color(33, 33, 33));
        } else if (done) {
            c.setForeground(new Color(140, 140, 150));
        } else if (overdue) {
            c.setForeground(new Color(198, 40, 40));
        } else if (daysLeft <= 2) {
            c.setForeground(new Color(230, 126, 34));
        } else {
            c.setForeground(new Color(50, 50, 60));
        }

        if (col == 1) {
            String text = value == null ? "" : escapeHtml(value.toString());
            if (done) {
                setText("<html><span style='color:#8c8c96'><strike>" + text + "</strike></span></html>");
            } else {
                String dot = daysLeft < 0 ? "<span style='color:#d32f2f'>&#9679; </span>"
                        : daysLeft <= 2 ? "<span style='color:#f57c00'>&#9679; </span>"
                        : daysLeft <= 7 ? "<span style='color:#388e3c'>&#9679; </span>"
                        : "<span style='color:#1976d2'>&#9679; </span>";
                setText("<html>" + dot + text + "</html>");
            }
        } else if (col == 2 || col == 3) {
            String text = value == null ? "" : escapeHtml(value.toString());
            if (done) {
                setText("<html><span style='color:#8c8c96'><strike>" + text + "</strike></span></html>");
            } else {
                setText(text);
            }
        } else {
            if (!(value != null && value.toString().startsWith("<html>"))) {
                setText(value == null ? "" : value.toString());
            }
        }
        return c;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}