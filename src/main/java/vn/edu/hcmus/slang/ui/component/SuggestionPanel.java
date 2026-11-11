package vn.edu.hcmus.slang.ui.component;

import vn.edu.hcmus.slang.ui.Theme;
import vn.edu.hcmus.slang.ui.Ui;
import vn.edu.hcmus.slang.util.TextNormalizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Khối gợi ý (autocomplete) dưới ô tìm kiếm.
 *
 * <p>Panel này chỉ hiển thị danh sách do caller truyền vào; nhờ vậy màn hình
 * tìm theo slang word có thể chỉ gợi ý word, còn màn hình tìm theo định nghĩa
 * chỉ gợi ý definition.</p>
 */
public final class SuggestionPanel extends RoundedPanel {

    public record Item(String primary, String secondary, boolean boldPrimary, Runnable action) {
    }

    private final JLabel titleLabel = Ui.label("", 9, Font.BOLD, Theme.FAINT);
    private final JPanel rows = Ui.vbox();

    public SuggestionPanel(String title) {
        super(new Color(0xF7F7F7), null, 10);
        setLayout(new java.awt.BorderLayout(0, 3));
        setBorder(new EmptyBorder(7, 9, 7, 9));

        JPanel header = new JPanel(new java.awt.BorderLayout());
        header.setOpaque(false);
        titleLabel.setText(title);
        JLabel hint = Ui.label("nhấn để tìm", 9, Font.PLAIN, Theme.FAINT);
        header.add(titleLabel, java.awt.BorderLayout.WEST);
        header.add(hint, java.awt.BorderLayout.EAST);
        add(header, java.awt.BorderLayout.NORTH);
        add(rows, java.awt.BorderLayout.CENTER);
        setVisible(false);
    }

    public void setItems(String title, List<Item> items) {
        titleLabel.setText(title);
        rows.removeAll();
        for (Item item : items) {
            rows.add(new SuggestionRow(item));
            rows.add(Box.createVerticalStrut(2));
        }
        setVisible(!items.isEmpty());
        revalidate();
        if (getParent() != null) {
            getParent().revalidate();
        }
        repaint();
    }

    public void clear() {
        setItems("", List.of());
    }

    private static final class SuggestionRow extends JPanel {

        private boolean hovered;
        private final Item item;

        SuggestionRow(Item item) {
            this.item = item;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(5, 7, 5, 7));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel primary = new JLabel("<html><div style='width:680px'>"
                    + TextNormalizer.escapeHtml(item.primary()) + "</div></html>");
            primary.setFont(Theme.font(12, item.boldPrimary() ? Font.BOLD : Font.PLAIN));
            primary.setForeground(Theme.TEXT);

            JLabel secondary = null;
            if (item.secondary() != null && !item.secondary().isBlank()) {
                secondary = new JLabel("<html><div style='width:680px'>"
                        + TextNormalizer.escapeHtml(item.secondary()) + "</div></html>");
                secondary.setFont(Theme.font(10, Font.PLAIN));
                secondary.setForeground(Theme.MUTED);
            }

            primary.setAlignmentX(LEFT_ALIGNMENT);
            add(primary);
            if (secondary != null) {
                secondary.setAlignmentX(LEFT_ALIGNMENT);
                add(secondary);
            }
            setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));

            MouseAdapter interaction = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    item.action().run();
                }
            };
            addMouseListener(interaction);
            primary.addMouseListener(interaction);
            if (secondary != null) {
                secondary.addMouseListener(interaction);
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            if (hovered) {
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(new Color(0xE9E9E9));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g.dispose();
                }
            }
            super.paintComponent(graphics);
        }
    }
}
