package vn.edu.hcmus.slang.ui.component;

import vn.edu.hcmus.slang.ui.Theme;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Một đáp án trắc nghiệm với badge A/B/C/D và trạng thái đúng/sai. */
public final class QuizOption extends JPanel {

    public enum State {
        DEFAULT, CORRECT, WRONG, MUTED
    }

    private final char letter;
    private final Runnable onClick;
    private State state = State.DEFAULT;
    private boolean hovered;
    private boolean clickable = true;

    public QuizOption(char letter, String text, Runnable onClick) {
        this.letter = letter;
        this.onClick = onClick;
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());
        setBorder(new javax.swing.border.EmptyBorder(10, 58, 10, 18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(0, 64));

        String html = "<html><div style='width:670px'>" + text + "</div></html>";
        JLabel label = new JLabel(html);
        label.setFont(Theme.font(13, Font.PLAIN));
        label.setForeground(Theme.TEXT);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(label, java.awt.BorderLayout.CENTER);

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
                if (clickable && QuizOption.this.onClick != null) {
                    QuizOption.this.onClick.run();
                }
            }
        };
        addMouseListener(interaction);
        label.addMouseListener(interaction);
    }

    public void setState(State state) {
        this.state = state;
        repaint();
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
        setCursor(Cursor.getPredefinedCursor(clickable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = Color.WHITE;
            Color border = Theme.BORDER;
            Color badgeFill = Theme.ACCENT_SOFT;
            Color badgeText = Theme.ACCENT;

            switch (state) {
                case CORRECT -> {
                    fill = Theme.SUCCESS_SOFT;
                    border = new Color(0x6EE7B7);
                    badgeFill = new Color(0xD1FAE5);
                    badgeText = Theme.SUCCESS;
                }
                case WRONG -> {
                    fill = Theme.DANGER_SOFT;
                    border = new Color(0xFCA5A5);
                    badgeFill = new Color(0xFEE2E2);
                    badgeText = Theme.DANGER;
                }
                case MUTED -> {
                    fill = new Color(0xFBFCFE);
                    border = Theme.BORDER_SOFT;
                    badgeFill = new Color(0xF1F5F9);
                    badgeText = Theme.FAINT;
                }
                case DEFAULT -> {
                    if (hovered && clickable) {
                        fill = new Color(0xF8FAFF);
                        border = new Color(0xC7D2FE);
                    }
                }
            }

            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g.setColor(border);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            int size = 28;
            int x = 15;
            int y = (getHeight() - size) / 2;
            g.setColor(badgeFill);
            g.fillOval(x, y, size, size);
            g.setColor(badgeText);
            g.setFont(Theme.font(12, Font.BOLD));
            String value = String.valueOf(letter);
            int textWidth = g.getFontMetrics().stringWidth(value);
            g.drawString(value, x + (size - textWidth) / 2,
                    y + (size + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2);
        } finally {
            g.dispose();
        }
        super.paintComponent(graphics);
    }
}
