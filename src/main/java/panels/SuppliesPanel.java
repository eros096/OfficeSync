package panels;

import Database.OfficeSyncDatabase;
import constants.AppColors;
import constants.AppFonts;
import constants.ButtonStyles;
import constants.LabeledField;
import constants.PanelCard;
import constants.TablePanel;
import constants.ValidationUtil;
import dialogs.AppDialog;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import models.Supply;
import models.User;

public class SuppliesPanel extends JPanel {
    private static final int CONTENT_X = 28;
    private static final int CONTENT_WIDTH = 1564;
    private static final int CARD_WIDTH = 380;
    private static final int CARD_GAP = 14;
    private static final int ACTION_WIDTH = 122;
    private static final int ACTION_GAP = 12;
    private final User user;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Supply Name", "Category", "Stock", "Reorder Level", "Status"}, 0
    );
    private final JTextField searchField = new JTextField("Search by supply name or category...");
    private TablePanel tablePanel;
    private PanelCard totalCard;
    private PanelCard lowStockCard;
    private PanelCard availableCard;
    private PanelCard categoryCard;
    private List<Supply> currentSupplies = new ArrayList<>();

    public SuppliesPanel(User user) {
        this.user = user;
        setLayout(null);
        setBackground(AppColors.BACKGROUND);
        buildHeader();
        buildCards();
        buildSearchAndForm();
        buildTable();
        refresh();
    }

    public void refresh() {
        try {
            currentSupplies = OfficeSyncDatabase.findAllSupplies();
            loadSupplies(searchField.getText());
            refreshCards();
        } catch (SQLException ex) {
            AppDialog.error(this, "Unable to load supplies:\n" + ex.getMessage());
        }
    }

    private void buildHeader() {
        JLabel title = new JLabel("Supplies Management");
        title.setBounds(28, 20, 360, 34);
        title.setFont(AppFonts.HEADING);
        title.setForeground(AppColors.TEXT);
        add(title);

        JLabel subtitle = new JLabel("Manage office inventory, low-stock items, and reorder levels.");
        subtitle.setBounds(28, 52, 500, 24);
        subtitle.setFont(AppFonts.BODY);
        subtitle.setForeground(AppColors.MUTED_TEXT);
        add(subtitle);

        int buttonCount = user.getRole() == User.Role.ADMIN ? 4 : 1;
        int x = CONTENT_X + CONTENT_WIDTH - (buttonCount * ACTION_WIDTH) - ((buttonCount - 1) * ACTION_GAP);
        if (user.getRole() == User.Role.ADMIN) {
            JButton add = actionButton("+ Add Supply", AppColors.SUCCESS, x);
            add.addActionListener(event -> addSupply());
            add(add);
            x += ACTION_WIDTH + ACTION_GAP;
        }

        JButton view = actionButton("View Supply", AppColors.INFO, x);
        view.addActionListener(event -> viewSupply());
        add(view);
        x += ACTION_WIDTH + ACTION_GAP;

        if (user.getRole() == User.Role.ADMIN) {
            JButton edit = actionButton("Edit Supply", AppColors.WARNING, x);
            edit.setForeground(AppColors.TEXT);
            edit.addActionListener(event -> editSupply());
            add(edit);
            x += ACTION_WIDTH + ACTION_GAP;

            JButton delete = actionButton("Delete Supply", AppColors.DANGER, x);
            delete.addActionListener(event -> deleteSupply());
            add(delete);
        }
    }

    private JButton actionButton(String text, java.awt.Color color, int x) {
        JButton button = new JButton(text);
        button.setBounds(x, 28, 122, 40);
        button.setFont(AppFonts.LABEL);
        button.setBackground(color);
        button.setForeground(java.awt.Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        return button;
    }

    private void buildCards() {
        totalCard = new PanelCard("Total Supplies", "0", AppColors.INFO);
        totalCard.setBounds(CONTENT_X, 96, CARD_WIDTH, 88);
        add(totalCard);

        lowStockCard = new PanelCard("Low Stock", "0", AppColors.DANGER);
        lowStockCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP), 96, CARD_WIDTH, 88);
        add(lowStockCard);

        availableCard = new PanelCard("Available", "0", AppColors.SUCCESS);
        availableCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP) * 2, 96, CARD_WIDTH, 88);
        add(availableCard);

        categoryCard = new PanelCard("Categories", "0", AppColors.WARNING);
        categoryCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP) * 3, 96, CARD_WIDTH, 88);
        add(categoryCard);
    }

    private void buildSearchAndForm() {
        JPanel searchPanel = new JPanel(null);
        searchPanel.setBounds(CONTENT_X, 204, CONTENT_WIDTH, 118);
        searchPanel.setBackground(AppColors.SURFACE);
        searchPanel.setBorder(javax.swing.BorderFactory.createLineBorder(AppColors.BORDER));
        add(searchPanel);

        searchField.setBounds(20, 18, CONTENT_WIDTH - 294, 36);
        searchField.setFont(AppFonts.BODY);
        searchField.setForeground(AppColors.MUTED_TEXT);
        searchPanel.add(searchField);

        JButton search = ButtonStyles.primary("Search");
        search.setBounds(CONTENT_WIDTH - 254, 18, 100, 36);
        search.addActionListener(event -> loadSupplies(searchField.getText()));
        searchPanel.add(search);

        JButton refresh = ButtonStyles.secondary("Refresh");
        refresh.setBounds(CONTENT_WIDTH - 134, 18, 100, 36);
        refresh.addActionListener(event -> {
            searchField.setText("Search by supply name or category...");
            refresh();
        });
        searchPanel.add(refresh);

        if (user.getRole() == User.Role.ADMIN) {
            JLabel hint = new JLabel("Use Add, Edit, View, and Delete buttons above. Select a row before editing.");
            hint.setBounds(20, 72, 720, 24);
            hint.setFont(AppFonts.BODY);
            hint.setForeground(AppColors.MUTED_TEXT);
            searchPanel.add(hint);
        }
    }

    private void buildTable() {
        tablePanel = new TablePanel("Supply Records", tableModel, 250);
        tablePanel.setBounds(CONTENT_X, 342, CONTENT_WIDTH, 590);
        add(tablePanel);
    }

    private void addSupply() {
        LabeledField nameField = new LabeledField("Supply");
        LabeledField categoryField = new LabeledField("Category");
        LabeledField stockField = new LabeledField("Stock");
        LabeledField reorderField = new LabeledField("Reorder Level");
        JPanel panel = supplyFormPanel(nameField, categoryField, stockField, reorderField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Add Supply", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            if (ValidationUtil.isBlank(nameField.getText()) || ValidationUtil.isBlank(categoryField.getText())) {
                AppDialog.warning(this, "Enter supply name and category.");
                return;
            }
            int stock = ValidationUtil.parsePositiveInt(stockField.getText(), "Stock");
            int reorder = ValidationUtil.parsePositiveInt(reorderField.getText(), "Reorder level");
            OfficeSyncDatabase.addSupply(nameField.getText(), categoryField.getText(), stock, reorder);
            refresh();
            AppDialog.info(this, "Supply added.");
        } catch (IllegalArgumentException ex) {
            AppDialog.warning(this, ex.getMessage());
        } catch (SQLException ex) {
            AppDialog.error(this, "Unable to add supply:\n" + ex.getMessage());
        }
    }

    private void viewSupply() {
        int selectedRow = getSelectedModelRow();
        if (selectedRow < 0) {
            AppDialog.warning(this, "Select a supply to view.");
            return;
        }
        AppDialog.info(this,
                "Supply ID: " + tableModel.getValueAt(selectedRow, 0)
                + "\nName: " + tableModel.getValueAt(selectedRow, 1)
                + "\nCategory: " + tableModel.getValueAt(selectedRow, 2)
                + "\nStock: " + tableModel.getValueAt(selectedRow, 3)
                + "\nReorder Level: " + tableModel.getValueAt(selectedRow, 4)
                + "\nStatus: " + tableModel.getValueAt(selectedRow, 5));
    }

    private void editSupply() {
        int selectedRow = getSelectedModelRow();
        if (selectedRow < 0) {
            AppDialog.warning(this, "Select a supply to edit.");
            return;
        }

        LabeledField nameField = new LabeledField("Supply");
        LabeledField categoryField = new LabeledField("Category");
        LabeledField stockField = new LabeledField("Stock");
        LabeledField reorderField = new LabeledField("Reorder Level");
        nameField.getField().setText(tableModel.getValueAt(selectedRow, 1).toString());
        categoryField.getField().setText(tableModel.getValueAt(selectedRow, 2).toString());
        stockField.getField().setText(tableModel.getValueAt(selectedRow, 3).toString());
        reorderField.getField().setText(tableModel.getValueAt(selectedRow, 4).toString());

        JPanel panel = supplyFormPanel(nameField, categoryField, stockField, reorderField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Supply Stock", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int id = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            int stock = ValidationUtil.parsePositiveInt(stockField.getText(), "Stock");
            int reorder = ValidationUtil.parsePositiveInt(reorderField.getText(), "Reorder level");
            OfficeSyncDatabase.updateSupply(id, nameField.getText(), categoryField.getText(), stock, reorder);
            refresh();
            AppDialog.info(this, "Supply updated.");
        } catch (IllegalArgumentException ex) {
            AppDialog.warning(this, ex.getMessage());
        } catch (SQLException ex) {
            AppDialog.error(this, "Unable to update supply:\n" + ex.getMessage());
        }
    }

    private void deleteSupply() {
        int selectedRow = getSelectedModelRow();
        if (selectedRow < 0) {
            AppDialog.warning(this, "Select a supply to delete.");
            return;
        }

        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Delete " + tableModel.getValueAt(selectedRow, 1) + "?",
                "Delete Supply",
                javax.swing.JOptionPane.YES_NO_OPTION
        );
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int id = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            OfficeSyncDatabase.deleteSupply(id);
            refresh();
            AppDialog.info(this, "Supply deleted.");
        } catch (SQLException ex) {
            AppDialog.error(this, "Unable to delete supply. It may already be used in a request.\n" + ex.getMessage());
        }
    }

    private int getSelectedModelRow() {
        if (tablePanel == null || tablePanel.getTable().getSelectedRow() < 0) {
            return -1;
        }
        return tablePanel.getTable().convertRowIndexToModel(tablePanel.getTable().getSelectedRow());
    }

    private void loadSupplies(String query) {
        String normalized = query == null ? "" : query.trim();
        boolean hasFilter = !normalized.isEmpty() && !normalized.equals("Search by supply name or category...");
        tableModel.setRowCount(0);
        for (Supply supply : currentSupplies) {
            if (hasFilter
                    && !supply.getName().toLowerCase().contains(normalized.toLowerCase())
                    && !supply.getCategory().toLowerCase().contains(normalized.toLowerCase())) {
                continue;
            }
            tableModel.addRow(new Object[]{
                supply.getId(),
                supply.getName(),
                supply.getCategory(),
                supply.getStock(),
                supply.getReorderLevel(),
                supply.getStatus()
            });
        }
    }

    private void refreshCards() {
        long lowStock = currentSupplies.stream().filter(supply -> "Low Stock".equals(supply.getStatus())).count();
        long available = currentSupplies.size() - lowStock;
        long categories = currentSupplies.stream().map(Supply::getCategory).distinct().count();
        totalCard.setValue(String.valueOf(currentSupplies.size()));
        lowStockCard.setValue(String.valueOf(lowStock));
        availableCard.setValue(String.valueOf(available));
        categoryCard.setValue(String.valueOf(categories));
    }

    private JPanel supplyFormPanel(LabeledField nameField, LabeledField categoryField, LabeledField stockField, LabeledField reorderField) {
        JPanel panel = new JPanel(null);
        panel.setPreferredSize(new java.awt.Dimension(430, 230));
        panel.setBackground(AppColors.SURFACE);

        nameField.setBounds(18, 12, 390, 46);
        categoryField.setBounds(18, 64, 390, 46);
        stockField.setBounds(18, 116, 185, 46);
        reorderField.setBounds(223, 116, 185, 46);

        JLabel note = new JLabel("Stock and reorder level update the selected inventory record.");
        note.setBounds(18, 178, 390, 28);
        note.setFont(AppFonts.BODY);
        note.setForeground(AppColors.MUTED_TEXT);

        panel.add(nameField);
        panel.add(categoryField);
        panel.add(stockField);
        panel.add(reorderField);
        panel.add(note);
        return panel;
    }
}
