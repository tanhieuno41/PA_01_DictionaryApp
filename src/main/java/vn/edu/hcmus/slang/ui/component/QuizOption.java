package vn.edu.hcmus.slang.ui.component;

import vn.edu.hcmus.slang.ui.Theme;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Một đáp án trắc nghiệm với badge A/B/C/D và trạng thái đúng/sai (đen–trắng). */
public final class QuizOption extends JPanel {

    public enum State {
        DEFAULT, CORRECT, WRONG, MUTED
    }

    private final char letter;
    private final Runnable onClick;
    private final JLabel textLabel;
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
        textLabel = new JLabel(html);
        textLabel.setFont(Theme.font(13, Font.PLAIN));
        textLabel.setForeground(Theme.TEXT);
        textLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(textLabel, java.awt.BorderLayout.CENTER);

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
        textLabel.addMouseListener(interaction);
    }

    public void setState(State state) {
        this.state = state;
        textLabel.setForeground(state == State.MUTED ? Theme.FAINT : Theme.TEXT);
        repaint();
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
        setCursor(Cursor.getPredefinedCursor(clickable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        textLabel.setCursor(Cursor.getPredefinedCursor(clickable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = Color.WHITE;
            Color border = Theme.BORDER;
            Color badgeFill = Theme.ACCENT_SOFT;
            Color badgeText = Theme.MUTED;
            Stroke borderStroke = new BasicStroke(1f);

            switch (state) {
                case CORRECT -> {
                    fill = new Color(0xE5E5E5);
                    border = Theme.TEXT;
                    badgeFill = Theme.TEXT;
                    badgeText = Color.WHITE;
                    borderStroke = new BasicStroke(2f);
                }
                case WRONG -> {
                    fill = Color.WHITE;
                    border = Theme.TEXT;
                    badgeFill = Color.WHITE;
                    badgeText = Theme.TEXT;
                    borderStroke = new BasicStroke(1.6f, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER, 10f, new float[]{5f, 4f}, 0f);
                }
                case MUTED -> {
                    fill = new Color(0xFAFAFA);
                    border = Theme.BORDER_SOFT;
                    badgeFill = new Color(0xF0F0F0);
                    badgeText = Theme.FAINT;
                }
                case DEFAULT -> {
                    if (hovered && clickable) {
                        fill = new Color(0xFAFAFA);
                        border = Theme.TEXT;
                    }
                }
            }

            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g.setColor(border);
            g.setStroke(borderStroke);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g.setStroke(new BasicStroke(1f));

            int size = 28;
            int x = 15;
            int y = (getHeight() - size) / 2;
            g.setColor(badgeFill);
            g.fillOval(x, y, size, size);
            if (state == State.WRONG) {
                g.setColor(Theme.TEXT);
                g.setStroke(new BasicStroke(1.2f));
                g.drawOval(x, y, size, size);
                g.setStroke(new BasicStroke(1f));
            }
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
