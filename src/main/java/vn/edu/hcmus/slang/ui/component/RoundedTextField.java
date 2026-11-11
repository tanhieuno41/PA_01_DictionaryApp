package vn.edu.hcmus.slang.ui.component;

import vn.edu.hcmus.slang.ui.Theme;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Ô nhập liệu bo góc, có placeholder và viền focus. */
public final class RoundedTextField extends JTextField {

    private final int radius = 11;
    private String placeholder = "";
    private boolean focused;

    public RoundedTextField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder == null ? "" : placeholder;
        setOpaque(false);
        setFont(Theme.font(14, Font.PLAIN));
        setForeground(Theme.TEXT);
        setCaretColor(Theme.ACCENT);
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(11, 14, 11, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                repaint();
            }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(focused ? Theme.ACCENT : Theme.BORDER);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        } finally {
            g.dispose();
        }
        super.paintComponent(graphics);

        if (getText().isEmpty() && !placeholder.isEmpty()) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(Theme.FAINT);
                g2.setFont(getFont());
                int x = getInsets().left + 1;
                int y = (getHeight() + g2.getFontMetrics().getAscent()
                        - g2.getFontMetrics().getDescent()) / 2;
                g2.drawString(placeholder, x, y);
            } finally {
                g2.dispose();
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width, Math.max(42, size.height));
    }
}
