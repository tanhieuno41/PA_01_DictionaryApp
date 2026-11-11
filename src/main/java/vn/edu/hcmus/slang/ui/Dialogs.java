package vn.edu.hcmus.slang.ui;

import vn.edu.hcmus.slang.model.Slang;
import vn.edu.hcmus.slang.service.HistoryService;
import vn.edu.hcmus.slang.ui.component.RoundedPanel;
import vn.edu.hcmus.slang.util.TextNormalizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/** Hộp thoại dùng chung. */
public final class Dialogs {

    public enum DuplicateChoice {
        OVERWRITE, ADD_DUPLICATE, CANCEL
    }

    private Dialogs() {
    }

    public static void error(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void info(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static boolean confirm(Component parent, String title, String message, String okText) {
        Object[] options = {okText, "Hủy"};
        int choice = JOptionPane.showOptionDialog(parent, message, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
        return choice == 0;
    }

    public static boolean confirmDelete(Component parent, Slang slang) {
        String message = "<html><div style='width:420px'>Bạn có chắc muốn xóa slang word "
                + "<b>" + TextNormalizer.escapeHtml(slang.getWord()) + "</b>?<br><br>"
                + "<span style='color:#525252'>" + TextNormalizer.escapeHtml(
                        shorten(slang.getJoinedDefinitions(), 220)) + "</span><br><br>"
                + "Hành động này không thể hoàn tác.</div></html>";
        return confirm(parent, "Xác nhận xóa", message, "Xóa");
    }

    public static DuplicateChoice duplicateChoice(Component parent, String word, List<Slang> existing) {
        StringBuilder html = new StringBuilder();
        html.append("<html><div style='width:460px'>Slang word <b>")
                .append(TextNormalizer.escapeHtml(word))
                .append("</b> đã tồn tại. Bạn muốn xử lý thế nào?<br><br>");
        int shown = 0;
        for (Slang slang : existing) {
            if (shown >= 3) {
                break;
            }
            html.append("<div style='background:#F5F5F5;padding:6px 8px;margin-bottom:4px'>")
                    .append(TextNormalizer.escapeHtml(shorten(slang.getJoinedDefinitions(), 180)))
                    .append("</div>");
            shown++;
        }
        if (existing.size() > shown) {
            html.append("<i>… và ").append(existing.size() - shown).append(" mục khác</i>");
        }
        html.append("<br><b>Ghi đè</b>: cập nhật mục đầu tiên. ")
                .append("<b>Thêm bản trùng</b>: tạo một mục mới cùng tên.")
                .append("</div></html>");

        Object[] options = {"Ghi đè", "Thêm bản trùng", "Hủy"};
        int choice = JOptionPane.showOptionDialog(parent, html.toString(), "Slang word đã tồn tại",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        return switch (choice) {
            case 0 -> DuplicateChoice.OVERWRITE;
            case 1 -> DuplicateChoice.ADD_DUPLICATE;
            default -> DuplicateChoice.CANCEL;
        };
    }

    public static void showEntry(Component parent, Slang slang) {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel word = Ui.label(slang.getWord(), 24, Font.BOLD, Theme.TEXT);
        word.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(word);

        JLabel meta = Ui.label("ID #" + slang.getId() + " · cập nhật "
                + HistoryService.formatTimestamp(slang.getUpdatedAt()), 11, Font.PLAIN, Theme.FAINT);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(Box.createVerticalStrut(2));
        content.add(meta);
        content.add(Box.createVerticalStrut(14));

        for (int i = 0; i < slang.getDefinitions().size(); i++) {
            RoundedPanel row = new RoundedPanel(Theme.ACCENT_SOFT, 10);
            row.setLayout(new java.awt.BorderLayout());
            row.setBorder(new EmptyBorder(8, 12, 8, 12));
            row.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel text = Ui.htmlLabel("<html><div style='width:470px'><b>" + (i + 1) + ".</b> "
                    + TextNormalizer.escapeHtml(slang.getDefinitions().get(i)) + "</div></html>",
                    13, Font.PLAIN, Theme.TEXT);
            row.add(text, java.awt.BorderLayout.CENTER);
            content.add(row);
            content.add(Box.createVerticalStrut(6));
        }

        JOptionPane.showMessageDialog(parent, content, "Chi tiết slang word",
                JOptionPane.PLAIN_MESSAGE);
    }

    public static String shorten(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 1) + "…";
    }
}
