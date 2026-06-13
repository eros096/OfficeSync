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
import javax.swing.JButton; // Added import
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
    private final DefaultTableModel reportModel = new DefaultTableModel(
            new Object[]{"Type", "Name", "Department/Category", "Supply", "Qty/Stock", "Date/Reorder", "Status"}, 0
    );
    private final JComboBox<String> reportFilter = new JComboBox<>(
            new String[]{"All", "Low Stock", "Out of Stock", "Pending Requests", "Approved Requests", "Rejected Requests"}
    );
    private final PanelCard lowStockCard = new PanelCard("Low Stock", "0", AppColors.DANGER);
    private final PanelCard pendingCard = new PanelCard("Pending", "0", AppColors.WARNING);
    private final PanelCard outStockCard = new PanelCard("Out of Stock", "0", AppColors.DANGER);
    private final PanelCard statusCard = new PanelCard("Reports", "Ready", AppColors.SUCCESS);
    private TablePanel tablePanel;
    private List<Supply> currentSupplies = new ArrayList<>();
    private List<SupplyRequest> currentRequests = new ArrayList<>();

    public ReportsPanel(User user) {
        this.user = user;
        setLayout(null);
        setBackground(AppColors.BACKGROUND);
        buildHeader();
        buildCards();
        buildFilter();
        buildTable();
        refresh();
    }

    public void refresh() {
        try {
            currentSupplies = OfficeSyncDatabase.findAllSupplies();
            currentRequests = OfficeSyncDatabase.findVisibleRequests(user);
            lowStockCard.setValue(String.valueOf(countLowStock()));
            outStockCard.setValue(String.valueOf(countOutOfStock()));
            pendingCard.setValue(String.valueOf(countRequestsByStatus("Pending")));
            statusCard.setValue("Ready");
            repaintReportTable();
        } catch (SQLException ex) {
            statusCard.setValue("Error");
            AppDialog.error(this, "Unable to load reports:\n" + ex.getMessage());
        }
    }

    private void buildHeader() {
        JLabel title = new JLabel("Inventory Reports");
        title.setBounds(28, 20, 360, 34);
        title.setFont(AppFonts.HEADING);
        title.setForeground(AppColors.TEXT);
        add(title);

        JLabel subtitle = new JLabel("Review stock alerts and request records.");
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

    private void buildFilter() {
        JPanel filterPanel = new JPanel(null);
        filterPanel.setBounds(CONTENT_X, 214, CONTENT_WIDTH, 74);
        filterPanel.setBackground(AppColors.SURFACE);
        filterPanel.setBorder(javax.swing.BorderFactory.createLineBorder(AppColors.BORDER));
        add(filterPanel);

        JLabel filterLabel = new JLabel("Report Filter");
        filterLabel.setBounds(20, 12, 150, 22);
        filterLabel.setFont(AppFonts.LABEL);
        filterLabel.setForeground(AppColors.TEXT);
        filterPanel.add(filterLabel);

        reportFilter.setBounds(20, 36, 250, 30);
        reportFilter.setFont(AppFonts.BODY);
        reportFilter.addActionListener(event -> repaintReportTable());
        filterPanel.add(reportFilter);

        // --- Added Refresh Button ---
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBounds(290, 36, 120, 30);
        refreshButton.setFont(AppFonts.BODY);
        refreshButton.addActionListener(event -> refresh());
        filterPanel.add(refreshButton);
    }

    private void buildTable() {
        tablePanel = new TablePanel("Report Records", reportModel, 330);
        tablePanel.setBounds(CONTENT_X, 308, CONTENT_WIDTH, 606);
        add(tablePanel);
    }

    private void repaintReportTable() {
        reportModel.setRowCount(0);
        String selectedFilter = currentFilter();

        if ("All".equals(selectedFilter) || "Low Stock".equals(selectedFilter) || "Out of Stock".equals(selectedFilter)) {
            loadStockRows(selectedFilter);
        }

        if ("All".equals(selectedFilter) || selectedFilter.endsWith(" Requests")) {
            loadRequestRows(selectedFilter);
        }

        if (tablePanel != null) {
            tablePanel.getTable().clearSelection();
            tablePanel.getTable().revalidate();
            tablePanel.getTable().repaint();
        }
    }

    private void loadStockRows(String selectedFilter) {
        for (Supply supply : currentSupplies) {
            String reportStatus = stockReportStatusFor(supply);
            if (reportStatus.isEmpty()) {
                continue;
            }
            if (!"All".equals(selectedFilter) && !reportStatus.equals(selectedFilter)) {
                continue;
            }
            reportModel.addRow(new Object[]{
                "Stock",
                supply.getName(),
                supply.getCategory(),
                supply.getName(),
                supply.getStock(),
                supply.getReorderLevel(),
                reportStatus
            });
        }
    }

    private void loadRequestRows(String selectedFilter) {
        for (SupplyRequest request : currentRequests) {
            if (!shouldShowRequest(request, selectedFilter)) {
                continue;
            }
            reportModel.addRow(new Object[]{
                "Request",
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
        String status = selectedFilter.replace(" Requests", "");
        return request.getStatus().equalsIgnoreCase(status);
    }

    private String currentFilter() {
        return reportFilter.getSelectedItem() == null ? "All" : reportFilter.getSelectedItem().toString();
    }

    private int countLowStock() {
        return (int) currentSupplies.stream()
                .filter(supply -> "Low Stock".equals(stockReportStatusFor(supply)))
                .count();
    }

    private int countOutOfStock() {
        return (int) currentSupplies.stream()
                .filter(supply -> "Out of Stock".equals(stockReportStatusFor(supply)))
                .count();
    }

    private int countRequestsByStatus(String status) {
        return (int) currentRequests.stream()
                .filter(request -> request.getStatus().equalsIgnoreCase(status))
                .count();
    }

    private String stockReportStatusFor(Supply supply) {
        if (!supply.isAvailable() || supply.getStock() <= 0) {
            return "Out of Stock";
        }
        if (supply.getStock() <= supply.getReorderLevel()) {
            return "Low Stock";
        }
        return "";
    }
}