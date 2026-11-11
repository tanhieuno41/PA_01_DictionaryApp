package vn.edu.hcmus.slang.ui.component;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Panel nền trắng bo góc, có viền nhẹ. */
public class RoundedPanel extends JPanel {

    private final int radius;
    private Color fill;
    private Color outline;

    public RoundedPanel(Color fill, int radius) {
        this(fill, null, radius);
    }

    public RoundedPanel(Color fill, Color outline, int radius) {
        this.fill = fill;
        this.outline = outline;
        this.radius = radius;
        setOpaque(false);
    }

    public void setFill(Color fill) {
        this.fill = fill;
        repaint();
    }

    public void setOutline(Color outline) {
        this.outline = outline;
        repaint();
    }

    public int getRadius() {
        return radius;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            if (fill != null) {
                g.setColor(fill);
                g.fillRoundRect(0, 0, width - 1, height - 1, radius, radius);
            }
            if (outline != null) {
                g.setColor(outline);
                g.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);
            }
        } finally {
            g.dispose();
        }
        super.paintComponent(graphics);
    }
}
