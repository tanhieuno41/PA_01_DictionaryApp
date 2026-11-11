package vn.edu.hcmus.slang.ui;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/** Bảng màu, font và thiết lập Look &amp; Feel dùng chung. */
public final class Theme {

    public static final Color BG = new Color(0xF4F6FB);
    public static final Color CARD = Color.WHITE;
    public static final Color SIDEBAR = new Color(0x0F172A);
    public static final Color SIDEBAR_HOVER = new Color(0x1B2740);
    public static final Color SIDEBAR_ACTIVE = new Color(0x233149);
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color FAINT = new Color(0x94A3B8);
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color BORDER_SOFT = new Color(0xEDF1F7);
    public static final Color ACCENT = new Color(0x4F46E5);
    public static final Color ACCENT_DARK = new Color(0x4338CA);
    public static final Color ACCENT_SOFT = new Color(0xEEF2FF);
    public static final Color ACCENT_SOFT_2 = new Color(0xE0E7FF);
    public static final Color SUCCESS = new Color(0x059669);
    public static final Color SUCCESS_SOFT = new Color(0xECFDF5);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color DANGER_DARK = new Color(0xB91C1C);
    public static final Color DANGER_SOFT = new Color(0xFEF2F2);
    public static final Color WARNING = new Color(0xD97706);
    public static final Color WARNING_SOFT = new Color(0xFFFBEB);
    public static final Color HIGHLIGHT = new Color(0xFEF3C7);
    public static final Color HIGHLIGHT_TEXT = new Color(0x92400E);

    private static final String FONT_FAMILY = "Segoe UI";

    private Theme() {
    }

    public static Font font(int size, int style) {
        return new Font(FONT_FAMILY, style, size);
    }

    public static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Dùng LAF mặc định nếu hệ thống không hỗ trợ.
        }
        UIManager.put("Label.font", font(13, Font.PLAIN));
        UIManager.put("Button.font", font(13, Font.PLAIN));
        UIManager.put("TextField.font", font(14, Font.PLAIN));
        UIManager.put("TextArea.font", font(14, Font.PLAIN));
        UIManager.put("Table.font", font(13, Font.PLAIN));
        UIManager.put("TableHeader.font", font(11, Font.BOLD));
        UIManager.put("MenuBar.font", font(13, Font.PLAIN));
        UIManager.put("Menu.font", font(13, Font.PLAIN));
        UIManager.put("MenuItem.font", font(13, Font.PLAIN));
        UIManager.put("OptionPane.messageFont", font(13, Font.PLAIN));
        UIManager.put("OptionPane.buttonFont", font(13, Font.PLAIN));
        UIManager.put("ToolTip.font", font(12, Font.PLAIN));
        UIManager.put("List.font", font(13, Font.PLAIN));
        UIManager.put("ComboBox.font", font(13, Font.PLAIN));
        UIManager.put("ScrollPane.background", BG);
        UIManager.put("Viewport.background", CARD);
        UIManager.put("MenuBar.background", CARD);
        UIManager.put("MenuBar.borderColor", BORDER);
        UIManager.put("PopupMenu.background", CARD);
    }
}
