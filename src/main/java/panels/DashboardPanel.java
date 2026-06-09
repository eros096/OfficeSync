package panels;

import constants.AppColors;
import constants.AppFonts;
import controls.LoginPage;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import models.User;

public class DashboardPanel extends JPanel {
    private static final int FRAME_WIDTH = 1920;
    private static final int FRAME_HEIGHT = 1080;
    private static final int TOP_HEIGHT = 92;
    private static final int SIDE_WIDTH = 300;
    private final JFrame parentFrame;
    private final User user;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);
    private JPanel top;
    private JPanel side;
    private JLabel account;
    private JButton activeButton;
    private JButton dashboardButton;
    private JButton suppliesButton;
    private JButton requestsButton;
    private JButton reportsButton;
    private JButton logoutButton;
    private OverviewPanel overviewPanel;
    private SuppliesPanel suppliesPanel;
    private RequestsPanel requestsPanel;
    private ReportsPanel reportsPanel;

    public DashboardPanel(JFrame parentFrame, User user) {
        this.parentFrame = parentFrame;
        this.user = user;
        setLayout(null);
        setBackground(AppColors.BACKGROUND);
        buildTopPanel();
        buildSidePanel();
        buildPages();
        showPage(dashboardButton, "dashboard");
    }

    private void buildTopPanel() {
        top = new JPanel(null);
        top.setBounds(0, 0, FRAME_WIDTH, TOP_HEIGHT);
        top.setBackground(AppColors.NAVY);
        add(top);

        JLabel title = new JLabel("OfficeSync Inventory System");
        title.setBounds(38, 22, 620, 48);
        title.setForeground(Color.WHITE);
        title.setFont(AppFonts.HEADING.deriveFont(28f));
        top.add(title);

        account = new JLabel(user.getFullName() + " | " + user.getRole().getDisplayName() + " | " + user.getDepartment());
        account.setBounds(1180, 30, 680, 30);
        account.setHorizontalAlignment(JLabel.RIGHT);
        account.setForeground(AppColors.SOFT_TEXT);
        account.setFont(AppFonts.BODY);
        top.add(account);
    }

    private void buildSidePanel() {
        side = new JPanel(null);
        side.setBounds(0, TOP_HEIGHT, SIDE_WIDTH, FRAME_HEIGHT - TOP_HEIGHT);
        side.setBackground(AppColors.NAVY);
        add(side);

        int y = 30;
        dashboardButton = createSideButton("Dashboard", y, side);
        dashboardButton.addActionListener(event -> showPage(dashboardButton, "dashboard"));
        y += 55;

        suppliesButton = createSideButton("Supplies", y, side);
        suppliesButton.addActionListener(event -> showPage(suppliesButton, "supplies"));
        y += 55;

        requestsButton = createSideButton("Requests", y, side);
        requestsButton.addActionListener(event -> showPage(requestsButton, "requests"));
        y += 55;

        if (user.getRole() == User.Role.ADMIN) {
            reportsButton = createSideButton("Notifications", y, side);
            reportsButton.addActionListener(event -> showPage(reportsButton, "reports"));
        }

        logoutButton = createSideButton("Logout", 850, side);
        logoutButton.addActionListener(event -> {
            new LoginPage().setVisible(true);
            parentFrame.dispose();
        });

    }

    private JButton createSideButton(String text, int y, JPanel side) {
        JButton button = new JButton(text);
        button.setBounds(32, y, 236, 48);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setBackground(AppColors.NAVY);
        button.setForeground(Color.WHITE);
        button.setFont(AppFonts.BUTTON);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        side.add(button);
        return button;
    }

    private void buildPages() {
        container.setBounds(SIDE_WIDTH, TOP_HEIGHT, FRAME_WIDTH - SIDE_WIDTH, FRAME_HEIGHT - TOP_HEIGHT);
        container.setBackground(AppColors.BACKGROUND);
        add(container);

        overviewPanel = new OverviewPanel(user, this::selectPage);
        suppliesPanel = new SuppliesPanel(user);
        requestsPanel = new RequestsPanel(user);

        container.add(overviewPanel, "dashboard");
        container.add(suppliesPanel, "supplies");
        container.add(requestsPanel, "requests");

        if (user.getRole() == User.Role.ADMIN) {
            reportsPanel = new ReportsPanel(user);
            container.add(reportsPanel, "reports");
        }
    }

    private void showPage(JButton button, String pageName) {
        if (activeButton != null) {
            activeButton.setBackground(AppColors.NAVY);
        }
        activeButton = button;
        activeButton.setBackground(AppColors.HEADER);
        refreshPage(pageName);
        cardLayout.show(container, pageName);
    }

    private void refreshPage(String pageName) {
        if ("dashboard".equals(pageName) && overviewPanel != null) {
            overviewPanel.refresh();
        } else if ("supplies".equals(pageName) && suppliesPanel != null) {
            suppliesPanel.refresh();
        } else if ("requests".equals(pageName) && requestsPanel != null) {
            requestsPanel.refresh();
        } else if ("reports".equals(pageName) && reportsPanel != null) {
            reportsPanel.refresh();
        }
    }

    private void selectPage(String pageName) {
        if ("supplies".equals(pageName)) {
            showPage(suppliesButton, "supplies");
        } else if ("requests".equals(pageName)) {
            showPage(requestsButton, "requests");
        } else if ("reports".equals(pageName) && reportsButton != null) {
            showPage(reportsButton, "reports");
        } else {
            showPage(dashboardButton, "dashboard");
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());
        top.setBounds(0, 0, width, TOP_HEIGHT);
        account.setBounds(Math.max(660, width - 720), 30, 680, 30);
        side.setBounds(0, TOP_HEIGHT, SIDE_WIDTH, height - TOP_HEIGHT);
        logoutButton.setBounds(32, Math.max(30, height - TOP_HEIGHT - 78), 236, 48);
        container.setBounds(SIDE_WIDTH, TOP_HEIGHT, width - SIDE_WIDTH, height - TOP_HEIGHT);
    }
}
