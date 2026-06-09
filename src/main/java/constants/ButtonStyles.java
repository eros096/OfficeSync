package constants;

import java.awt.Color;
import java.awt.Cursor;
import javax.swing.JButton;

public final class ButtonStyles {
    private ButtonStyles() {
    }

    public static JButton primary(String text) {
        JButton button = base(text);
        button.setBackground(AppColors.TEAL);
        button.setForeground(Color.WHITE);
        button.setFont(AppFonts.BODY.deriveFont(java.awt.Font.BOLD));
        button.setBorderPainted(false);
        return button;
    }

    public static JButton secondary(String text) {
        JButton button = base(text);
        button.setBackground(AppColors.SURFACE);
        button.setForeground(AppColors.TEXT);
        button.setFont(AppFonts.BODY);
        return button;
    }

    private static JButton base(String text) {
        JButton button = new JButton(text);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        return button;
    }
}
