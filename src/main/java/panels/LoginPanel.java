package panels;

import Database.OfficeSyncDatabase;
import constants.AppColors;
import constants.AppFonts;
import dialogs.AppDialog;
import controls.DashboardPage;
import java.awt.Color;
import java.awt.Cursor;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import models.User;

public class LoginPanel extends JPanel {
    private final JFrame parentFrame;
    private JTextField txtEmail;
    private JPasswordField txtPassword;

    public LoginPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setBackground(AppColors.SURFACE);
        buildLeftPanel();
        buildRightPanel();
    }

    private void buildLeftPanel() {
        JPanel left = new JPanel(null);
        left.setBounds(0, 0, 390, 600);
        left.setBackground(AppColors.NAVY);
        add(left);

        JLabel logo = new JLabel("OS");
        logo.setBounds(135, 55, 120, 100);
        logo.setHorizontalAlignment(JLabel.CENTER);
        logo.setOpaque(true);
        logo.setBackground(AppColors.SURFACE);
        logo.setForeground(AppColors.NAVY);
        logo.setFont(AppFonts.TITLE.deriveFont(42f));
        left.add(logo);

        JLabel name = new JLabel("<html><center>OFFICESYNC<br>INVENTORY SYSTEM</center></html>");
        name.setBounds(45, 170, 300, 85);
        name.setForeground(Color.WHITE);
        name.setFont(AppFonts.TITLE.deriveFont(25f));
        left.add(name);

        JLabel description = new JLabel("<html><center>A simple office supplies inventory<br>and request management system.</center></html>");
        description.setBounds(45, 265, 300, 55);
        description.setForeground(AppColors.SOFT_TEXT);
        description.setFont(AppFonts.BODY);
        left.add(description);

        JLabel bullets = new JLabel("<html>"
                + "- Monitor supplies<br>"
                + "- Submit requests<br>"
                + "- Approve requests by role<br>"
                + "- View low stock reports"
                + "</html>");
        bullets.setBounds(95, 345, 230, 110);
        bullets.setForeground(Color.WHITE);
        bullets.setFont(AppFonts.BODY);
        left.add(bullets);

        JLabel footer = new JLabel("OfficeSync 2026");
        footer.setBounds(130, 515, 160, 25);
        footer.setForeground(AppColors.SOFT_TEXT);
        footer.setFont(AppFonts.SMALL);
        left.add(footer);
    }

    private void buildRightPanel() {
        JPanel right = new JPanel(null);
        right.setBounds(390, 0, 610, 600);
        right.setBackground(new Color(240, 246, 255));
        add(right);

        JPanel card = new JPanel(null);
        card.setBounds(85, 45, 440, 500);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));
        right.add(card);

        JLabel title = new JLabel("Sign in");
        title.setBounds(55, 35, 260, 45);
        title.setFont(AppFonts.TITLE);
        title.setForeground(AppColors.TEXT);
        card.add(title);

        JLabel subtitle = new JLabel("Login using your OfficeSync account");
        subtitle.setBounds(55, 78, 320, 25);
        subtitle.setFont(AppFonts.BODY);
        subtitle.setForeground(AppColors.MUTED_TEXT);
        card.add(subtitle);

        JLabel lblEmail = new JLabel("Email");
        lblEmail.setBounds(55, 130, 100, 30);
        lblEmail.setFont(AppFonts.BODY);
        card.add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setBounds(55, 165, 330, 42);
        txtEmail.setFont(AppFonts.BODY);
        txtEmail.setForeground(AppColors.TEXT);
        txtEmail.setBackground(Color.WHITE);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        card.add(txtEmail);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setBounds(55, 230, 100, 30);
        lblPassword.setFont(AppFonts.BODY);
        card.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(55, 265, 330, 42);
        txtPassword.setFont(AppFonts.BODY);
        txtPassword.setForeground(Color.BLACK);
        txtPassword.setBackground(Color.WHITE);
        txtPassword.setEchoChar('*');
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        card.add(txtPassword);

        JButton login = new JButton("Login");
        login.setBounds(55, 340, 330, 45);
        login.setBackground(AppColors.HEADER);
        login.setForeground(Color.WHITE);
        login.setFont(AppFonts.BUTTON);
        login.setFocusPainted(false);
        login.setOpaque(true);
        login.setContentAreaFilled(true);
        login.setBorderPainted(false);
        login.setCursor(new Cursor(Cursor.HAND_CURSOR));
        login.addActionListener(event -> login());
        card.add(login);

        JButton help = new JButton("Forgot Password?");
        help.setBounds(55, 395, 330, 30);
        help.setForeground(AppColors.HEADER);
        help.setFont(AppFonts.BODY);
        help.setContentAreaFilled(false);
        help.setBorderPainted(false);
        help.setFocusPainted(false);
        help.addActionListener(event -> AppDialog.info(this, "Please contact your administrator to reset your password."));
        card.add(help);

        JLabel demo = new JLabel("<html>"
                + "<b>Demo accounts</b><br>"
                + "Admin: admin@officesync.local<br>"
                + "Head: head@officesync.local<br>"
                + "Employee: employee@officesync.local<br>"
                + "Password: 1234"
                + "</html>");
        demo.setBounds(55, 435, 340, 70);
        demo.setForeground(AppColors.MUTED_TEXT);
        demo.setFont(AppFonts.SMALL);
        card.add(demo);
    }

    private void login() {
        String email = txtEmail.getText().trim().toLowerCase();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            AppDialog.warning(this, "Please enter email and password.");
            return;
        }

        try {
            User user = OfficeSyncDatabase.authenticate(email, password);
            if (user == null) {
                AppDialog.error(this, "Invalid email or password.");
                return;
            }

            AppDialog.info(this, "Welcome, " + user.getFullName() + "!");
            new DashboardPage(user).setVisible(true);
            parentFrame.dispose();
        } catch (SQLException ex) {
            AppDialog.error(this, "Database connection error. Is MySQL/XAMPP running?\n" + ex.getMessage());
        }
    }
}
