package vn.edu.hcmus.slang.ui.component;

import vn.edu.hcmus.slang.ui.Theme;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Nút phẳng bo góc với các biến thể màu. */
public final class FlatButton extends JButton {

    public enum Variant {
        PRIMARY, SECONDARY, DANGER, GHOST, LIGHT
    }

    private final Variant variant;
    private final int height;
    private Color background;
    private Color hoverBackground;
    private Color foreground;
    private Color outline;
    private boolean hovered;

    public FlatButton(String text, Variant variant) {
        this(text, variant, 40);
    }

    public FlatButton(String text, Variant variant, int height) {
        super(text);
        this.variant = variant;
        this.height = height;
        setFont(Theme.font(13, Font.BOLD));
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(Theme.TEXT);
        applyPalette();
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

    public Variant getVariant() {
        return variant;
    }

    public void setPalette(Color background, Color foreground, Color hoverBackground) {
        this.background = background;
        this.foreground = foreground;
        this.hoverBackground = hoverBackground;
        setForeground(foreground);
        repaint();
    }

    private void applyPalette() {
        switch (variant) {
            case PRIMARY -> {
                background = Theme.ACCENT;
                hoverBackground = Theme.ACCENT_DARK;
                foreground = Color.WHITE;
            }
            case SECONDARY -> {
                background = Theme.ACCENT_SOFT;
                hoverBackground = Theme.ACCENT_SOFT_2;
                foreground = Theme.TEXT;
            }
            case DANGER -> {
                background = Color.WHITE;
                hoverBackground = new Color(0xE5E5E5);
                foreground = Theme.TEXT;
                outline = Theme.TEXT;
            }
            case GHOST -> {
                background = Color.WHITE;
                hoverBackground = new Color(0xF5F5F5);
                foreground = Theme.TEXT;
                outline = Theme.BORDER;
            }
            case LIGHT -> {
                background = Color.WHITE;
                hoverBackground = new Color(0xF5F5F5);
                foreground = Theme.TEXT;
            }
        }
        setForeground(foreground);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(Math.max(size.width + 20, 96), Math.max(height, size.height));
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill;
            Color text;
            if (!isEnabled()) {
                fill = new Color(0xE5E5E5);
                text = new Color(0x8A8A8A);
            } else {
                fill = hovered ? hoverBackground : background;
                text = foreground;
            }
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            if (outline != null) {
                g.setColor(isEnabled() ? outline : Theme.BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
            if (isFocusOwner()) {
                g.setColor(Theme.alpha(Theme.ACCENT, 90));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 11, 11);
            }
            setForeground(text);
        } finally {
            g.dispose();
        }
        super.paintComponent(graphics);
    }
}
