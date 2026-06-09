package dialogs;

import java.awt.Component;
import javax.swing.JOptionPane;

public final class AppDialog {
    private AppDialog() {
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "OfficeSync", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "OfficeSync", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "OfficeSync", JOptionPane.ERROR_MESSAGE);
    }
}
