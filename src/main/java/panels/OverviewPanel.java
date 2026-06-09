package panels;

import Database.OfficeSyncDatabase;
import constants.AppColors;
import constants.AppFonts;
import constants.PanelCard;
import dialogs.AppDialog;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import models.User;

public class OverviewPanel extends JPanel {
    private static final int PANEL_WIDTH = 1620;
    private static final int GAP = 32;
    private static final int CARD_WIDTH = 360;
    private final User user;
    private final Consumer<String> navigate;
    private final PanelCard suppliesCard = new PanelCard("Total Supplies", "0", AppColors.INFO);
    private final PanelCard lowStockCard = new PanelCard("Low Stock", "0", AppColors.DANGER);
    private final PanelCard pendingCard = new PanelCard("Pending Requests", "0", AppColors.WARNING);
    private final PanelCard roleCard = new PanelCard("Role Access", "-", AppColors.SUCCESS);

    public OverviewPanel(User user, Consumer<String> navigate) {
        this.user = user;
        this.navigate = navigate;
        setLayout(null);
        setBackground(AppColors.BACKGROUND);
        buildHeader();
        buildCards();
        buildInfoPanels();
        refresh();
    }

    public void refresh() {
        try {
            suppliesCard.setValue(String.valueOf(OfficeSyncDatabase.countSupplies()));
            lowStockCard.setValue(String.valueOf(OfficeSyncDatabase.countLowStockSupplies()));
            pendingCard.setValue(String.valueOf(OfficeSyncDatabase.countPendingRequestsFor(user)));
            roleCard.setValue(user.getRole().getDisplayName());
        } catch (SQLException ex) {
            AppDialog.error(this, "Unable to load dashboard summary:\n" + ex.getMessage());
        }
    }

    private void buildHeader() {
        JLabel title = new JLabel("Dashboard Overview");
        title.setBounds(28, 20, 360, 34);
        title.setFont(AppFonts.HEADING);
        title.setForeground(AppColors.TEXT);
        add(title);

        JLabel subtitle = new JLabel("OfficeSync inventory summary and account scope.");
        subtitle.setBounds(28, 52, 520, 24);
        subtitle.setFont(AppFonts.BODY);
        subtitle.setForeground(AppColors.MUTED_TEXT);
        add(subtitle);
    }

    private void buildCards() {
        suppliesCard.setBounds(36, 104, CARD_WIDTH, 96);
        lowStockCard.setBounds(36 + CARD_WIDTH + GAP, 104, CARD_WIDTH, 96);
        pendingCard.setBounds(36 + (CARD_WIDTH + GAP) * 2, 104, CARD_WIDTH, 96);
        roleCard.setBounds(36 + (CARD_WIDTH + GAP) * 3, 104, CARD_WIDTH, 96);
        add(suppliesCard);
        add(lowStockCard);
        add(pendingCard);
        add(roleCard);
    }

    private void buildInfoPanels() {
        JPanel info = cardPanel(36, 236, 720, 330);
        JLabel infoTitle = panelTitle("System Information");
        infoTitle.setBounds(24, 22, 300, 30);
        info.add(infoTitle);

        JLabel account = new JLabel("<html>"
                + "<b>Logged in as:</b> " + user.getFullName() + "<br><br>"
                + "<b>Department:</b> " + user.getDepartment() + "<br><br>"
                + "<b>Role:</b> " + user.getRole().getDisplayName() + "<br><br>"
                + "Use the side menu to manage supplies, requests, and reports."
                + "</html>");
        account.setBounds(34, 78, 620, 180);
        account.setFont(AppFonts.BODY);
        account.setForeground(AppColors.TEXT);
        info.add(account);
        add(info);

        JPanel actions = cardPanel(832, 236, 720, 330);
        JLabel actionTitle = panelTitle("Quick Actions");
        actionTitle.setBounds(24, 22, 300, 30);
        actions.add(actionTitle);

        int buttonX = 240;
        JButton supplies = quickButton("Open Supplies", buttonX, 82, AppColors.INFO);
        supplies.addActionListener(event -> navigate.accept("supplies"));
        actions.add(supplies);

        JButton requests = quickButton(user.getRole() == User.Role.EMPLOYEE ? "Submit Request" : "Review Requests", buttonX, 142, AppColors.SUCCESS);
        requests.addActionListener(event -> navigate.accept("requests"));
        actions.add(requests);

        if (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.DEPARTMENT_HEAD) {
            JButton reports = quickButton("View Reports", buttonX, 202, AppColors.WARNING);
            reports.setForeground(AppColors.TEXT);
            reports.addActionListener(event -> navigate.accept("reports"));
            actions.add(reports);
        }
        add(actions);
    }

    private JPanel cardPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel(null);
        panel.setBounds(x, y, width, height);
        panel.setBackground(AppColors.SURFACE);
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(AppColors.BORDER));
        return panel;
    }

    private JLabel panelTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppFonts.HEADING);
        label.setForeground(AppColors.TEXT);
        return label;
    }

    private JButton quickButton(String text, int x, int y, java.awt.Color color) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 240, 44);
        button.setFont(AppFonts.LABEL);
        button.setBackground(color);
        button.setForeground(java.awt.Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        return button;
    }
}
