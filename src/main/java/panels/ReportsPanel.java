package panels;

import Database.OfficeSyncDatabase;
import constants.AppColors;
import constants.AppFonts;
import constants.PanelCard;
import constants.TablePanel;
import dialogs.AppDialog;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import models.Supply;
import models.SupplyRequest;
import models.User;

public class ReportsPanel extends JPanel {
    private static final int CONTENT_X = 28;
    private static final int CONTENT_WIDTH = 1564;
    private static final int CARD_WIDTH = 380;
    private static final int CARD_GAP = 14;
    private final User user;
    private final DefaultTableModel notificationModel = new DefaultTableModel(
            new Object[]{"Supply", "Category", "Stock", "Reorder Level", "Notification"}, 0
    );
    private final DefaultTableModel requestModel = new DefaultTableModel(
            new Object[]{"Request ID", "Requester", "Department", "Supply", "Qty", "Date", "Status"}, 0
    );
    private final PanelCard lowStockCard = new PanelCard("Low Stock", "0", AppColors.DANGER);
    private final PanelCard pendingCard = new PanelCard("Pending", "0", AppColors.WARNING);
    private final PanelCard outStockCard = new PanelCard("Out of Stock", "0", AppColors.DANGER);
    private final PanelCard statusCard = new PanelCard("Notifications", "Ready", AppColors.SUCCESS);
    private final JComboBox<String> notificationFilter = new JComboBox<>(
            new String[]{"All", "Low Stock", "Out of Stock", "Pending Requests", "Approved Requests", "Rejected Requests"}
    );
    private List<Supply> currentSupplies = new ArrayList<>();
    private List<SupplyRequest> currentRequests = new ArrayList<>();

    public ReportsPanel(User user) {
        this.user = user;
        setLayout(null);
        setBackground(AppColors.BACKGROUND);
        buildHeader();
        buildCards();
        buildTable();
        refresh();
    }

    public void refresh() {
        try {
            currentSupplies = OfficeSyncDatabase.findAllSupplies();
            currentRequests = OfficeSyncDatabase.findVisibleRequests(user);
            int lowStock = countLowStock();
            int outStock = countOutOfStock();
            int pending = OfficeSyncDatabase.countPendingRequestsFor(user);
            lowStockCard.setValue(String.valueOf(lowStock));
            outStockCard.setValue(String.valueOf(outStock));
            pendingCard.setValue(String.valueOf(pending));
            statusCard.setValue("Ready");
            reloadTables();
        } catch (SQLException ex) {
            statusCard.setValue("Error");
            AppDialog.error(this, "Unable to load notifications:\n" + ex.getMessage());
        }
    }

    private void buildHeader() {
        JLabel title = new JLabel("Inventory Notifications");
        title.setBounds(28, 20, 360, 34);
        title.setFont(AppFonts.HEADING);
        title.setForeground(AppColors.TEXT);
        add(title);

        JLabel subtitle = new JLabel("Review low-stock alerts, out-of-stock items, and pending request details.");
        subtitle.setBounds(28, 52, 520, 24);
        subtitle.setFont(AppFonts.BODY);
        subtitle.setForeground(AppColors.MUTED_TEXT);
        add(subtitle);
    }

    private void buildCards() {
        lowStockCard.setBounds(CONTENT_X, 96, CARD_WIDTH, 88);
        pendingCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP), 96, CARD_WIDTH, 88);
        outStockCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP) * 2, 96, CARD_WIDTH, 88);
        statusCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP) * 3, 96, CARD_WIDTH, 88);
        add(lowStockCard);
        add(pendingCard);
        add(outStockCard);
        add(statusCard);
    }

    private void buildTable() {
        notificationFilter.setBounds(CONTENT_X, 210, 230, 34);
        notificationFilter.setFont(AppFonts.BODY);
        notificationFilter.addActionListener(event -> reloadTables());
        add(notificationFilter);

        TablePanel notificationTable = new TablePanel("Stock Notification Records", notificationModel, 250);
        notificationTable.setBounds(CONTENT_X, 254, CONTENT_WIDTH, 286);
        add(notificationTable);

        TablePanel requestTable = new TablePanel("Request Detail Records", requestModel, 250);
        requestTable.setBounds(CONTENT_X, 618, CONTENT_WIDTH, 314);
        add(requestTable);
    }

    private void reloadTables() {
        loadNotifications();
        loadRequests();
        revalidate();
        repaint();
    }

    private void loadNotifications() {
        notificationModel.setRowCount(0);
        String selectedFilter = currentFilter();
        for (Supply supply : currentSupplies) {
            String notification = stockNotificationFor(supply);
            if (notification.isEmpty()) {
                continue;
            }
            if (!"All".equals(selectedFilter) && !notification.equals(selectedFilter)) {
                continue;
            }
            notificationModel.addRow(new Object[]{
                supply.getName(),
                supply.getCategory(),
                supply.getStock(),
                supply.getReorderLevel(),
                notification
            });
        }
    }

    private void loadRequests() {
        requestModel.setRowCount(0);
        String selectedFilter = currentFilter();
        for (SupplyRequest request : currentRequests) {
            if (!shouldShowRequest(request, selectedFilter)) {
                continue;
            }
            requestModel.addRow(new Object[]{
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

    private boolean shouldShowRequest(SupplyRequest request, String selectedFilter) {
        if ("All".equals(selectedFilter)) {
            return true;
        }
        if (selectedFilter.endsWith(" Requests")) {
            String status = selectedFilter.replace(" Requests", "");
            return request.getStatus().equalsIgnoreCase(status);
        }
        return false;
    }

    private String currentFilter() {
        return notificationFilter.getSelectedItem() == null ? "All" : notificationFilter.getSelectedItem().toString();
    }

    private int countLowStock() {
        return (int) currentSupplies.stream()
                .filter(supply -> "Low Stock".equals(stockNotificationFor(supply)))
                .count();
    }

    private int countOutOfStock() {
        return (int) currentSupplies.stream()
                .filter(supply -> "Out of Stock".equals(stockNotificationFor(supply)))
                .count();
    }

    private String stockNotificationFor(Supply supply) {
        if (!supply.isAvailable() || supply.getStock() <= 0) {
            return "Out of Stock";
        }
        if (supply.getStock() <= supply.getReorderLevel()) {
            return "Low Stock";
        }
        return "";
    }
}
