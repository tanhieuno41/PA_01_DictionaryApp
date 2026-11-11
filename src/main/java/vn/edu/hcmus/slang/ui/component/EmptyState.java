package vn.edu.hcmus.slang.ui.component;

import vn.edu.hcmus.slang.ui.Theme;
import vn.edu.hcmus.slang.ui.Ui;
import vn.edu.hcmus.slang.util.TextNormalizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

/** Trạng thái rỗng có thể đổi tiêu đề / gợi ý. */
public final class EmptyState extends JPanel {

    private final JLabel titleLabel = Ui.label("", 16, Font.BOLD, Theme.TEXT);
    private final JLabel hintLabel = new JLabel();

    public EmptyState(String title, String hint) {
        super(new GridBagLayout());
        setOpaque(false);

        JPanel box = Ui.vbox();
        box.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedPanel badge = new RoundedPanel(Theme.ACCENT_SOFT, 32);
        badge.setLayout(new GridBagLayout());
        badge.setPreferredSize(new Dimension(64, 64));
        badge.setMaximumSize(new Dimension(64, 64));
        badge.add(Ui.label("?", 26, Font.BOLD, Theme.ACCENT));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        hintLabel.setFont(Theme.font(12, Font.PLAIN));
        hintLabel.setForeground(Theme.MUTED);
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(badge);
        box.add(Box.createVerticalStrut(14));
        box.add(titleLabel);
        box.add(Box.createVerticalStrut(6));
        box.add(hintLabel);
        add(box);
        setContent(title, hint);
    }

    public void setContent(String title, String hint) {
        titleLabel.setText(title);
        hintLabel.setText("<html><div style='text-align:center;width:440px'>"
                + TextNormalizer.escapeHtml(hint) + "</div></html>");
    }
}
