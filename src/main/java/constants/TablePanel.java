package constants;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TablePanel extends JPanel {
    private final JLabel titleLabel;
    private final JScrollPane scrollPane;
    private final JTable table;

    public TablePanel(String title, DefaultTableModel model, int scrollHeight) {
        setLayout(null);
        setBackground(AppColors.SURFACE);
        setBorder(javax.swing.BorderFactory.createLineBorder(AppColors.BORDER));

        titleLabel = new JLabel(title);
        titleLabel.setBounds(24, 16, 600, 30);
        titleLabel.setFont(AppFonts.HEADING);
        titleLabel.setForeground(AppColors.TEXT);
        add(titleLabel);

        table = new JTable(model);
        styleTable(table);
        table.setDefaultEditor(Object.class, null);
        table.getTableHeader().setReorderingAllowed(false);

        scrollPane = new JScrollPane(table);
        scrollPane.setBounds(0, 58, 882, scrollHeight);
        add(scrollPane);
    }

    public JTable getTable() {
        return table;
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());
        titleLabel.setBounds(24, 16, width - 48, 30);
        scrollPane.setBounds(0, 58, width, height - 58);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(42);
        table.setFont(AppFonts.BODY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(AppColors.BORDER);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.getTableHeader().setFont(AppFonts.LABEL.deriveFont(java.awt.Font.BOLD, 14f));
        table.getTableHeader().setBackground(AppColors.HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.setDefaultRenderer(Object.class, new StatusRenderer());
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable source, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(source, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setBackground(AppColors.HEADER);
            label.setForeground(Color.WHITE);
            label.setFont(AppFonts.LABEL.deriveFont(java.awt.Font.BOLD, 14f));
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
            return label;
        }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));

            if (isSelected) {
                label.setBackground(AppColors.HEADER);
                label.setForeground(Color.WHITE);
                return label;
            }

            String text = value == null ? "" : value.toString();
            Color rowColor = row % 2 == 0 ? Color.WHITE : new Color(245, 248, 252);
            label.setBackground(rowColor);
            label.setForeground(AppColors.TEXT);

            if (text.equalsIgnoreCase("Paper")) {
                label.setBackground(new Color(226, 240, 255));
                label.setForeground(new Color(35, 88, 150));
            } else if (text.equalsIgnoreCase("Writing")) {
                label.setBackground(new Color(236, 232, 255));
                label.setForeground(new Color(90, 62, 160));
            } else if (text.equalsIgnoreCase("Filing")) {
                label.setBackground(new Color(232, 247, 242));
                label.setForeground(new Color(35, 120, 95));
            } else if (text.equalsIgnoreCase("Desk Tool")) {
                label.setBackground(new Color(255, 238, 219));
                label.setForeground(new Color(150, 85, 20));
            } else if (text.equalsIgnoreCase("Low Stock") || text.equalsIgnoreCase("Rejected")) {
                label.setBackground(new Color(255, 231, 231));
                label.setForeground(new Color(150, 35, 35));
            } else if (text.equalsIgnoreCase("Available") || text.equalsIgnoreCase("Approved")) {
                label.setBackground(new Color(229, 246, 235));
                label.setForeground(new Color(35, 120, 65));
            } else if (text.equalsIgnoreCase("Pending")) {
                label.setBackground(new Color(255, 244, 214));
                label.setForeground(new Color(145, 95, 20));
            }

            return label;
        }
    }
}
