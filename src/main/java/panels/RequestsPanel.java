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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import models.Supply;
import models.SupplyRequest;
import models.User;

public class RequestsPanel extends JPanel {
    private static final int CONTENT_X = 28;
    private static final int CONTENT_WIDTH = 1564;
    private static final int CARD_WIDTH = 380;
    private static final int CARD_GAP = 14;
    private static final int ACTION_WIDTH = 112;
    private static final int ACTION_GAP = 12;
    private final User user;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Request ID", "Requester", "Department", "Supply", "Qty", "Date", "Status"}, 0
    );
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected"});
    private final JTextField searchField = new JTextField("Search by requester, department, or supply...");
    private TablePanel tablePanel;
    private PanelCard totalCard;
    private PanelCard pendingCard;
    private PanelCard approvedCard;
    private PanelCard rejectedCard;
    private List<SupplyRequest> currentRequests = new ArrayList<>();
    private List<Supply> currentSupplies = new ArrayList<>();

    public RequestsPanel(User user) {
        this.user = user;
        setLayout(null);
        setBackground(AppColors.BACKGROUND);
        buildHeader();
        buildCards();
        buildSearchAndRequestForm();
        buildTable();
        refresh();
    }

    public void refresh() {
        try {
            currentSupplies = OfficeSyncDatabase.findAllSupplies();
            currentRequests = OfficeSyncDatabase.findVisibleRequests(user);
            loadRequests();
            refreshCards();
        } catch (SQLException ex) {
            AppDialog.error(this, "Unable to load requests:\n" + ex.getMessage());
        }
    }

    private void buildHeader() {
        JLabel title = new JLabel("Requests Management");
        title.setBounds(28, 20, 360, 34);
        title.setFont(AppFonts.HEADING);
        title.setForeground(AppColors.TEXT);
        add(title);

        JLabel subtitle = new JLabel("Submit, review, approve, reject, view, or delete supply requests.");
        subtitle.setBounds(28, 52, 560, 24);
        subtitle.setFont(AppFonts.BODY);
        subtitle.setForeground(AppColors.MUTED_TEXT);
        add(subtitle);

        int buttonCount = 3;
        int x = CONTENT_X + CONTENT_WIDTH - (buttonCount * ACTION_WIDTH) - ((buttonCount - 1) * ACTION_GAP);

        JButton submit = actionButton("+ Submit", AppColors.SUCCESS, x);
        submit.addActionListener(event -> submitRequest());
        add(submit);
        x += ACTION_WIDTH + ACTION_GAP;

        JButton view = actionButton("View Request", AppColors.INFO, x);
        view.addActionListener(event -> viewRequest());
        add(view);
        x += ACTION_WIDTH + ACTION_GAP;

        JButton delete = actionButton("Delete", AppColors.DANGER, x);
        delete.addActionListener(event -> deleteRequest());
        add(delete);
    }

    private JButton actionButton(String text, java.awt.Color color, int x) {
        JButton button = new JButton(text);
        button.setBounds(x, 28, 112, 40);
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
        totalCard = new PanelCard("Total Requests", "0", AppColors.INFO);
        totalCard.setBounds(CONTENT_X, 96, CARD_WIDTH, 88);
        add(totalCard);

        pendingCard = new PanelCard("Pending", "0", AppColors.WARNING);
        pendingCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP), 96, CARD_WIDTH, 88);
        add(pendingCard);

        approvedCard = new PanelCard("Approved", "0", AppColors.SUCCESS);
        approvedCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP) * 2, 96, CARD_WIDTH, 88);
        add(approvedCard);

        rejectedCard = new PanelCard("Rejected", "0", AppColors.DANGER);
        rejectedCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP) * 3, 96, CARD_WIDTH, 88);
        add(rejectedCard);
    }

    private void buildSearchAndRequestForm() {
        JPanel panel = new JPanel(null);
        panel.setBounds(CONTENT_X, 204, CONTENT_WIDTH, 118);
        panel.setBackground(AppColors.SURFACE);
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(AppColors.BORDER));
        add(panel);

        searchField.setBounds(20, 18, CONTENT_WIDTH - 444, 36);
        searchField.setFont(AppFonts.BODY);
        searchField.setForeground(AppColors.MUTED_TEXT);
        panel.add(searchField);

        statusFilter.setBounds(CONTENT_WIDTH - 404, 18, 130, 36);
        statusFilter.setFont(AppFonts.BODY);
        statusFilter.addActionListener(event -> loadRequests());
        panel.add(statusFilter);

        JButton search = ButtonStyles.primary("Search");
        search.setBounds(CONTENT_WIDTH - 254, 18, 100, 36);
        search.addActionListener(event -> loadRequests());
        panel.add(search);

        JButton refresh = ButtonStyles.secondary("Refresh");
        refresh.setBounds(CONTENT_WIDTH - 134, 18, 100, 36);
        refresh.addActionListener(event -> {
            searchField.setText("Search by requester, department, or supply...");
            statusFilter.setSelectedItem("All");
            refresh();
        });
        panel.add(refresh);

        JLabel note = new JLabel("Use Submit to open the request form. Select a row before View, Approve, Reject, or Delete.");
        note.setBounds(20, 76, 720, 24);
        note.setFont(AppFonts.BODY);
        note.setForeground(AppColors.MUTED_TEXT);
        panel.add(note);
    }

    private void buildTable() {
        tablePanel = new TablePanel("Request Records", tableModel, 250);
        tablePanel.setBounds(CONTENT_X, 342, CONTENT_WIDTH, 590);
        if (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.DEPARTMENT_HEAD) {
            JButton approve = tableActionButton("Approve", AppColors.WARNING, CONTENT_WIDTH - 244);
            approve.setForeground(AppColors.TEXT);
            approve.addActionListener(event -> updateSelectedStatus("Approved"));
            tablePanel.add(approve);

            JButton reject = tableActionButton("Reject", AppColors.DANGER, CONTENT_WIDTH - 122);
            reject.addActionListener(event -> updateSelectedStatus("Rejected"));
            tablePanel.add(reject);
        }
        add(tablePanel);
    }

    private JButton tableActionButton(String text, java.awt.Color color, int x) {
        JButton button = new JButton(text);
        button.setBounds(x, 12, 100, 36);
        button.setFont(AppFonts.LABEL);
        button.setBackground(color);
        button.setForeground(java.awt.Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        return button;
    }

    private void submitRequest() {
        if (currentSupplies.isEmpty()) {
            AppDialog.warning(this, "No supply is available to request.");
            return;
        }

        JComboBox<SupplyOption> supplyBox = new JComboBox<>();
        for (Supply supply : currentSupplies) {
            supplyBox.addItem(new SupplyOption(supply.getId(), supply.getName()));
        }
        LabeledField quantityField = new LabeledField("Quantity");
        JPanel panel = requestFormPanel(supplyBox, quantityField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Submit Request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        SupplyOption option = (SupplyOption) supplyBox.getSelectedItem();
        try {
            int quantity = ValidationUtil.parsePositiveInt(quantityField.getText(), "Quantity");
            OfficeSyncDatabase.submitRequest(user.getId(), option.id, quantity);
            refresh();
            AppDialog.info(this, "Request submitted.");
        } catch (IllegalArgumentException ex) {
            AppDialog.warning(this, ex.getMessage());
        } catch (SQLException ex) {
            AppDialog.error(this, "Unable to submit request:\n" + ex.getMessage());
        }
    }

    private void viewRequest() {
        int row = getSelectedModelRow();
        if (row < 0) {
            AppDialog.warning(this, "Select a request to view.");
            return;
        }

        AppDialog.info(this,
                "Request ID: " + tableModel.getValueAt(row, 0)
                + "\nRequester: " + tableModel.getValueAt(row, 1)
                + "\nDepartment: " + tableModel.getValueAt(row, 2)
                + "\nSupply: " + tableModel.getValueAt(row, 3)
                + "\nQuantity: " + tableModel.getValueAt(row, 4)
                + "\nDate: " + tableModel.getValueAt(row, 5)
                + "\nStatus: " + tableModel.getValueAt(row, 6));
    }

    private void updateSelectedStatus(String status) {
        int row = getSelectedModelRow();
        if (row < 0) {
            AppDialog.warning(this, "Select a request first.");
            return;
        }

        try {
            OfficeSyncDatabase.updateRequestStatus(parseRequestId(row), status);
            refresh();
            AppDialog.info(this, "Request marked as " + status + ".");
        } catch (SQLException ex) {
            AppDialog.error(this, "Unable to update request:\n" + ex.getMessage());
        }
    }

    private void deleteRequest() {
        int row = getSelectedModelRow();
        if (row < 0) {
            AppDialog.warning(this, "Select a request to delete.");
            return;
        }

        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Delete request " + tableModel.getValueAt(row, 0) + "?",
                "Delete Request",
                javax.swing.JOptionPane.YES_NO_OPTION
        );
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        try {
            OfficeSyncDatabase.deleteRequest(parseRequestId(row));
            refresh();
            AppDialog.info(this, "Request deleted.");
        } catch (SQLException ex) {
            AppDialog.error(this, "Unable to delete request:\n" + ex.getMessage());
        }
    }

    private int parseRequestId(int modelRow) {
        return Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString().substring(1));
    }

    private int getSelectedModelRow() {
        if (tablePanel == null || tablePanel.getTable().getSelectedRow() < 0) {
            return -1;
        }
        return tablePanel.getTable().convertRowIndexToModel(tablePanel.getTable().getSelectedRow());
    }

    private void loadRequests() {
        tableModel.setRowCount(0);
        String selectedStatus = statusFilter.getSelectedItem() == null ? "All" : statusFilter.getSelectedItem().toString();
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        boolean hasSearch = !query.isEmpty() && !query.equals("search by requester, department, or supply...");

        for (SupplyRequest request : currentRequests) {
            if (!selectedStatus.equals("All") && !request.getStatus().equalsIgnoreCase(selectedStatus)) {
                continue;
            }
            if (hasSearch
                    && !request.getRequester().toLowerCase().contains(query)
                    && !request.getDepartment().toLowerCase().contains(query)
                    && !request.getSupplyName().toLowerCase().contains(query)) {
                continue;
            }
            tableModel.addRow(new Object[]{
                "R" + String.format("%03d", request.getRequestId()),
                request.getRequester(),
                request.getDepartment(),
                request.getSupplyName(),
                request.getQuantity(),
                request.getRequestDate(),
                request.getStatus()
            });
        }
    }

    private void refreshCards() {
        totalCard.setValue(String.valueOf(currentRequests.size()));
        pendingCard.setValue(String.valueOf(countStatus("Pending")));
        approvedCard.setValue(String.valueOf(countStatus("Approved")));
        rejectedCard.setValue(String.valueOf(countStatus("Rejected")));
    }

    private long countStatus(String status) {
        return currentRequests.stream()
                .filter(request -> request.getStatus().equalsIgnoreCase(status))
                .count();
    }

    private JPanel requestFormPanel(JComboBox<SupplyOption> supplyBox, LabeledField quantityField) {
        JPanel panel = new JPanel(null);
        panel.setPreferredSize(new java.awt.Dimension(420, 170));
        panel.setBackground(AppColors.SURFACE);

        JLabel supplyLabel = new JLabel("Supply");
        supplyLabel.setBounds(18, 12, 160, 22);
        supplyLabel.setFont(AppFonts.LABEL);
        supplyLabel.setForeground(AppColors.TEXT);
        panel.add(supplyLabel);

        supplyBox.setBounds(18, 38, 380, 34);
        supplyBox.setFont(AppFonts.BODY);
        panel.add(supplyBox);

        quantityField.setBounds(18, 86, 180, 48);
        panel.add(quantityField);

        JLabel note = new JLabel("Request quantity must be greater than zero.");
        note.setBounds(210, 102, 200, 24);
        note.setFont(AppFonts.BODY);
        note.setForeground(AppColors.MUTED_TEXT);
        panel.add(note);
        return panel;
    }

    private static class SupplyOption {
        private final int id;
        private final String name;

        private SupplyOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof SupplyOption option)) {
                return false;
            }
            return id == option.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(id);
        }
    }
}
