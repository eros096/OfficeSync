package controls;
import java.awt.Dimension;
import javax.swing.JFrame;
import panels.LoginPanel;

public class LoginPage extends JFrame {
    public LoginPage() {
        setTitle("OfficeSync Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1000, 600));
        setResizable(false);
        setLocationRelativeTo(null);
        setContentPane(new LoginPanel(this));
    }

    public static void main(String[] args) {
        LoginPage login = new LoginPage();
        login.setVisible(true);
    }
}
