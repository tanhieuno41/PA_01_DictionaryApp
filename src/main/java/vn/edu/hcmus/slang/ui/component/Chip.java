package vn.edu.hcmus.slang.ui.component;

import vn.edu.hcmus.slang.ui.Theme;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Nhãn nhỏ bo tròn dùng cho trạng thái / thẻ thông tin. */
public final class Chip extends JLabel {

    private Color background;

    public Chip(String text, Color background, Color foreground) {
        super(text);
        this.background = background;
        setOpaque(false);
        setFont(Theme.font(11, Font.BOLD));
        setForeground(foreground);
        setBorder(new EmptyBorder(5, 10, 5, 10));
    }

    public void setPalette(Color background, Color foreground) {
        this.background = background;
        setForeground(foreground);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(background);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        } finally {
            g.dispose();
        }
        super.paintComponent(graphics);
    }
}
