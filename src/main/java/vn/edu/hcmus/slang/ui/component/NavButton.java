package vn.edu.hcmus.slang.ui.component;

import vn.edu.hcmus.slang.ui.Theme;

import javax.swing.JButton;
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

/** Mục điều hướng trong sidebar. */
public final class NavButton extends JButton {

    private boolean active;
    private boolean hovered;

    public NavButton(String text) {
        super(text);
        setFont(Theme.font(13, Font.PLAIN));
        setHorizontalAlignment(LEFT);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setBorder(new EmptyBorder(0, 20, 0, 12));
        setForeground(new Color(0xCBD5E1));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(220, 38));
        addMouseListener(new MouseAdapter() {
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
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? Color.WHITE : new Color(0xCBD5E1));
        setFont(Theme.font(13, active ? Font.BOLD : Font.PLAIN));
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 38);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g.setColor(Theme.SIDEBAR_ACTIVE);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g.setColor(Theme.ACCENT);
                g.fillRoundRect(0, 9, 3, getHeight() - 18, 3, 3);
            } else if (hovered) {
                g.setColor(Theme.SIDEBAR_HOVER);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        } finally {
            g.dispose();
        }
        super.paintComponent(graphics);
    }
}
