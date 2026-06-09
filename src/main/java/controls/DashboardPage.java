package controls;
import java.awt.Dimension;
import javax.swing.JFrame;
import models.User;
import panels.DashboardPanel;

public class DashboardPage extends JFrame {
    public DashboardPage(User user) {
        setTitle("OfficeSync Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new DashboardPanel(this, user));
        setMinimumSize(new Dimension(1280, 720));
        setSize(new Dimension(1920, 1080));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
}
