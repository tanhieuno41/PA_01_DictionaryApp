package vn.edu.hcmus.slang.ui;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * Bảng màu đen–trắng (grayscale) và font dùng chung.
 * Toàn bộ giá trị màu ở đây đều có R = G = B, không dùng màu nhấn.
 */
public final class Theme {

    public static final Color BG = new Color(0xF5F5F5);
    public static final Color CARD = Color.WHITE;
    public static final Color SIDEBAR = new Color(0x0A0A0A);
    public static final Color SIDEBAR_HOVER = new Color(0x1E1E1E);
    public static final Color SIDEBAR_ACTIVE = new Color(0x2E2E2E);
    public static final Color TEXT = new Color(0x0A0A0A);
    public static final Color MUTED = new Color(0x525252);
    public static final Color FAINT = new Color(0x737373);
    public static final Color BORDER = new Color(0xD4D4D4);
    public static final Color BORDER_SOFT = new Color(0xE5E5E5);
    public static final Color ACCENT = new Color(0x111111);
    public static final Color ACCENT_DARK = new Color(0x000000);
    public static final Color ACCENT_SOFT = new Color(0xF0F0F0);
    public static final Color ACCENT_SOFT_2 = new Color(0xE0E0E0);
    public static final Color SUCCESS = new Color(0x111111);
    public static final Color SUCCESS_SOFT = new Color(0xE8E8E8);
    public static final Color DANGER = new Color(0x111111);
    public static final Color DANGER_DARK = new Color(0x333333);
    public static final Color DANGER_SOFT = new Color(0xF0F0F0);
    public static final Color WARNING = new Color(0x333333);
    public static final Color WARNING_SOFT = new Color(0xEDEDED);
    public static final Color HIGHLIGHT = new Color(0xD4D4D4);
    public static final Color HIGHLIGHT_TEXT = new Color(0x000000);

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

        // Giữ toàn bộ component chuẩn của LAF cũng ở thang xám.
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Button.background", ACCENT_SOFT);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.select", ACCENT_SOFT_2);
        UIManager.put("Button.focus", new Color(0x8A8A8A));
        UIManager.put("MenuBar.background", CARD);
        UIManager.put("MenuBar.foreground", TEXT);
        UIManager.put("Menu.background", CARD);
        UIManager.put("MenuItem.background", CARD);
        UIManager.put("Menu.selectionBackground", ACCENT_SOFT_2);
        UIManager.put("Menu.selectionForeground", TEXT);
        UIManager.put("MenuItem.selectionBackground", ACCENT_SOFT_2);
        UIManager.put("MenuItem.selectionForeground", TEXT);
        UIManager.put("Table.selectionBackground", ACCENT_SOFT_2);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("TextField.selectionBackground", ACCENT_SOFT_2);
        UIManager.put("TextField.selectionForeground", TEXT);
        UIManager.put("TextArea.selectionBackground", ACCENT_SOFT_2);
        UIManager.put("TextArea.selectionForeground", TEXT);
        UIManager.put("List.selectionBackground", ACCENT_SOFT_2);
        UIManager.put("List.selectionForeground", TEXT);
        UIManager.put("ScrollPane.background", BG);
        UIManager.put("Viewport.background", CARD);
        UIManager.put("MenuBar.borderColor", BORDER);
        UIManager.put("PopupMenu.background", CARD);
    }
}
