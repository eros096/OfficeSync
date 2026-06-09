package panels;

import Database.OfficeSyncDatabase;
import constants.AppColors;
import constants.AppFonts;
import constants.PanelCard;
import constants.TablePanel;
import dialogs.AppDialog;
import java.sql.SQLException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import models.Supply;
import models.User;

public class ReportsPanel extends JPanel {
    private static final int CONTENT_X = 28;
    private static final int CONTENT_WIDTH = 1564;
    private static final int CARD_WIDTH = 380;
    private static final int CARD_GAP = 14;
    private final User user;
    private final DefaultTableModel reportModel = new DefaultTableModel(
            new Object[]{"Report Name", "Description", "Current Result"}, 0
    );
    private final PanelCard lowStockCard = new PanelCard("Low Stock", "0", AppColors.DANGER);
    private final PanelCard pendingCard = new PanelCard("Pending", "0", AppColors.WARNING);
    private final PanelCard scopeCard = new PanelCard("Scope", "-", AppColors.INFO);
    private final PanelCard statusCard = new PanelCard("Report Status", "Ready", AppColors.SUCCESS);

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
            int lowStock = OfficeSyncDatabase.countLowStockSupplies();
            int pending = OfficeSyncDatabase.countPendingRequestsFor(user);
            lowStockCard.setValue(String.valueOf(lowStock));
            pendingCard.setValue(String.valueOf(pending));
            scopeCard.setValue(user.getRole().getDisplayName());
            statusCard.setValue("Ready");

            reportModel.setRowCount(0);
            reportModel.addRow(new Object[]{
                "Low Stock Report",
                "Supplies at or below reorder level",
                lowStockSummary()
            });
            reportModel.addRow(new Object[]{
                "Request Summary",
                "Pending requests visible to your role",
                "Pending: " + pending
            });
            reportModel.addRow(new Object[]{
                "Role Scope",
                "Data filtered by current account role",
                user.getRole().getDisplayName() + " - " + user.getDepartment()
            });
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

        JLabel subtitle = new JLabel("Review inventory and request summaries.");
        subtitle.setBounds(28, 52, 520, 24);
        subtitle.setFont(AppFonts.BODY);
        subtitle.setForeground(AppColors.MUTED_TEXT);
        add(subtitle);
    }

    private void buildCards() {
        lowStockCard.setBounds(CONTENT_X, 96, CARD_WIDTH, 88);
        pendingCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP), 96, CARD_WIDTH, 88);
        scopeCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP) * 2, 96, CARD_WIDTH, 88);
        statusCard.setBounds(CONTENT_X + (CARD_WIDTH + CARD_GAP) * 3, 96, CARD_WIDTH, 88);
        add(lowStockCard);
        add(pendingCard);
        add(scopeCard);
        add(statusCard);
    }

    private void buildTable() {
        TablePanel tablePanel = new TablePanel("Report Records", reportModel, 330);
        tablePanel.setBounds(CONTENT_X, 214, CONTENT_WIDTH, 700);
        add(tablePanel);
    }

    private String lowStockSummary() throws SQLException {
        StringBuilder builder = new StringBuilder();
        for (Supply supply : OfficeSyncDatabase.findLowStockSupplies()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(supply.getName());
        }
        return builder.length() == 0 ? "No low-stock supplies" : builder.toString();
    }
}
