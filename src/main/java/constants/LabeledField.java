package constants;

import constants.AppColors;
import constants.AppFonts;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LabeledField extends JPanel {
    private final JTextField field;

    public LabeledField(String labelText) {
        this(labelText, new JTextField());
    }

    public LabeledField(String labelText, JTextField field) {
        super(new BorderLayout(0, 6));
        this.field = field;
        setOpaque(false);
        setPreferredSize(new Dimension(180, 62));

        JLabel label = new JLabel(labelText);
        label.setFont(AppFonts.LABEL);
        label.setForeground(AppColors.TEXT);
        add(label, BorderLayout.NORTH);

        field.setPreferredSize(new Dimension(180, 38));
        field.setMinimumSize(new Dimension(80, 38));
        field.setFont(AppFonts.BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        add(field, BorderLayout.CENTER);
    }

    public String getText() {
        return field.getText().trim();
    }

    public JTextField getField() {
        return field;
    }

    public void clear() {
        field.setText("");
    }
}
