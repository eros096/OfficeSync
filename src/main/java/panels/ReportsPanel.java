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

    private final DefaultTableModel reportModel = new DefaultTableModel();

    private final PanelCard lowStockCard =
            new PanelCard("Low Stock", "0", AppColors.DANGER);

    private final PanelCard pendingCard =
            new PanelCard("Pending", "0", AppColors.WARNING);

    private final PanelCard outStockCard =
            new PanelCard("Out of Stock", "0", AppColors.DANGER);

    private final PanelCard statusCard =
            new PanelCard("Notifications", "Ready", AppColors.SUCCESS);

    private final JComboBox<String> notificationFilter =
            new JComboBox<>(new String[]{
                "Low Stock",
                "Out of Stock",
                "Pending Requests",
                "Approved Requests",
                "Rejected Requests"
            });

    private List<Supply> currentSupplies = new ArrayList<>();
    private List<SupplyRequest> currentRequests = new ArrayList<>();

    private TablePanel reportTable;

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

            reloadTable();

        } catch (SQLException ex) {
            statusCard.setValue("Error");

            AppDialog.error(
                    this,
                    "Unable to load notifications:\n" + ex.getMessage()
            );
        }
    }

    private void buildHeader() {
        JLabel title = new JLabel("Inventory Notifications");
        title.setBounds(28, 20, 360, 34);
        title.setFont(AppFonts.HEADING);
        title.setForeground(AppColors.TEXT);
        add(title);

        JLabel subtitle = new JLabel(
                "Review low-stock alerts, out-of-stock items, and request details."
        );
        subtitle.setBounds(28, 52, 600, 24);
        subtitle.setFont(AppFonts.BODY);
        subtitle.setForeground(AppColors.MUTED_TEXT);
        add(subtitle);
    }

    private void buildCards() {

        lowStockCard.setBounds(
                CONTENT_X,
                96,
                CARD_WIDTH,
                88
        );

        pendingCard.setBounds(
                CONTENT_X + (CARD_WIDTH + CARD_GAP),
                96,
                CARD_WIDTH,
                88
        );

        outStockCard.setBounds(
                CONTENT_X + (CARD_WIDTH + CARD_GAP) * 2,
                96,
                CARD_WIDTH,
                88
        );

        statusCard.setBounds(
                CONTENT_X + (CARD_WIDTH + CARD_GAP) * 3,
                96,
                CARD_WIDTH,
                88
        );

        add(lowStockCard);
        add(pendingCard);
        add(outStockCard);
        add(statusCard);
    }

    private void buildTable() {

        notificationFilter.setBounds(
                CONTENT_X,
                210,
                230,
                34
        );

        notificationFilter.setFont(AppFonts.BODY);
        notificationFilter.setSelectedItem("Low Stock");
        notificationFilter.addActionListener(event -> reloadTable());

        add(notificationFilter);

        reportTable = new TablePanel(
                "Report Records",
                reportModel,
                250
        );

        reportTable.setBounds(
                CONTENT_X,
                254,
                CONTENT_WIDTH,
                678
        );

        add(reportTable);
    }

    private void reloadTable() {

        String filter = currentFilter();

        if ("Low Stock".equals(filter)
                || "Out of Stock".equals(filter)) {

            loadStockTable(filter);

        } else {

            loadRequestTable(filter);
        }

        revalidate();
        repaint();
    }

    private void loadStockTable(String filter) {

        reportModel.setRowCount(0);
        reportModel.setColumnCount(0);

        reportModel.setColumnIdentifiers(new Object[]{
            "Supply",
            "Category",
            "Stock",
            "Reorder Level",
            "Notification"
        });

        for (Supply supply : currentSupplies) {

            String notification = stockNotificationFor(supply);

            if (notification.isEmpty()) {
                continue;
            }

            if (!notification.equals(filter)) {
                continue;
            }

            reportModel.addRow(new Object[]{
                supply.getName(),
                supply.getCategory(),
                supply.getStock(),
                supply.getReorderLevel(),
                notification
            });
        }
    }

    private void loadRequestTable(String filter) {

        reportModel.setRowCount(0);
        reportModel.setColumnCount(0);

        reportModel.setColumnIdentifiers(new Object[]{
            "Request ID",
            "Requester",
            "Department",
            "Supply",
            "Qty",
            "Date",
            "Status"
        });

        for (SupplyRequest request : currentRequests) {

            if (!shouldShowRequest(request, filter)) {
                continue;
            }

            reportModel.addRow(new Object[]{
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

    private boolean shouldShowRequest(
            SupplyRequest request,
            String selectedFilter
    ) {

        if ("Pending Requests".equals(selectedFilter)) {
            return request.getStatus().equalsIgnoreCase("Pending");
        }

        if ("Approved Requests".equals(selectedFilter)) {
            return request.getStatus().equalsIgnoreCase("Approved");
        }

        if ("Rejected Requests".equals(selectedFilter)) {
            return request.getStatus().equalsIgnoreCase("Rejected");
        }

        return false;
    }

    private String currentFilter() {
        Object selected = notificationFilter.getSelectedItem();
        return selected == null
                ? "Low Stock"
                : selected.toString();
    }

    private int countLowStock() {
        return (int) currentSupplies.stream()
                .filter(supply ->
                        "Low Stock".equals(stockNotificationFor(supply)))
                .count();
    }

    private int countOutOfStock() {
        return (int) currentSupplies.stream()
                .filter(supply ->
                        "Out of Stock".equals(stockNotificationFor(supply)))
                .count();
    }

    private String stockNotificationFor(Supply supply) {

        if (!supply.isAvailable()
                || supply.getStock() <= 0) {
            return "Out of Stock";
        }

        if (supply.getStock()
                <= supply.getReorderLevel()) {
            return "Low Stock";
        }

        return "";
    }
}