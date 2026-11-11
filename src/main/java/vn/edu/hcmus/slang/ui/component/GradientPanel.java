package vn.edu.hcmus.slang.ui.component;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Panel nền gradient bo góc, dùng cho hero card. */
public final class GradientPanel extends JPanel {

    private final Color start;
    private final Color end;
    private final int radius;

    public GradientPanel(Color start, Color end, int radius) {
        this.start = start;
        this.end = end;
        this.radius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            g.setPaint(new GradientPaint(0, 0, start, width, height, end));
            g.fillRoundRect(0, 0, width - 1, height - 1, radius, radius);

            // Hai vòng tròn trang trí mờ ở góc phải.
            g.setColor(new Color(255, 255, 255, 18));
            int diameter = Math.max(180, height);
            g.fillOval(width - diameter / 2, -diameter / 3, diameter, diameter);
            g.setColor(new Color(255, 255, 255, 12));
            g.fillOval(width - diameter / 3, height - diameter / 2, diameter / 2, diameter / 2);
        } finally {
            g.dispose();
        }
        super.paintComponent(graphics);
    }
}
