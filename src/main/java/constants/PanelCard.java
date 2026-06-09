package constants;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PanelCard extends JPanel {
    private final JPanel top;
    private final JLabel titleLabel;
    private final JLabel valueLabel;

    public PanelCard(String title, String value, Color topColor) {
        setLayout(null);
        setBackground(AppColors.SURFACE);
        setBorder(javax.swing.BorderFactory.createLineBorder(AppColors.BORDER));

        top = new JPanel();
        top.setBounds(0, 0, 210, 8);
        top.setBackground(topColor);
        add(top);

        titleLabel = new JLabel(title);
        titleLabel.setBounds(18, 20, 170, 22);
        titleLabel.setForeground(AppColors.MUTED_TEXT);
        titleLabel.setFont(AppFonts.BODY);
        add(titleLabel);

        valueLabel = new JLabel(value);
        valueLabel.setBounds(18, 43, 170, 34);
        valueLabel.setForeground(AppColors.TEXT);
        valueLabel.setFont(AppFonts.HEADING);
        add(valueLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int width = Math.max(1, getWidth());
        top.setBounds(0, 0, width, 8);
        titleLabel.setBounds(18, 20, width - 36, 22);
        valueLabel.setBounds(18, 43, width - 36, 34);
    }
}
