package vn.edu.hcmus.slang.ui;

import vn.edu.hcmus.slang.ui.component.RoundedPanel;
import vn.edu.hcmus.slang.util.TextNormalizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.Locale;

/** Các helper dựng giao diện. */
public final class Ui {

    private Ui() {
    }

    public static JPanel vbox() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    public static JPanel hbox() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        return panel;
    }

    public static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(size, style));
        label.setForeground(color);
        return label;
    }

    public static JLabel htmlLabel(String html, int size, int style, Color color) {
        JLabel label = new JLabel(html);
        label.setFont(Theme.font(size, style));
        label.setForeground(color);
        return label;
    }

    /** Trang chuẩn: tiêu đề + mô tả + nội dung. */
    public static JPanel page(String title, String subtitle, JComponent body) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Theme.BG);
        page.setBorder(new EmptyBorder(22, 28, 20, 28));

        JPanel header = vbox();
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel titleLabel = label(title, 25, Font.BOLD, Theme.TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitleLabel = label(subtitle, 13, Font.PLAIN, Theme.MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);

        page.add(header, BorderLayout.NORTH);
        if (body != null) {
            page.add(body, BorderLayout.CENTER);
        }
        return page;
    }

    public static RoundedPanel card() {
        return card(18);
    }

    public static RoundedPanel card(int padding) {
        RoundedPanel panel = new RoundedPanel(Theme.CARD, Theme.BORDER, 16);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(padding, padding, padding, padding));
        return panel;
    }

    public static JPanel fieldGroup(String labelText, JComponent field, String hint) {
        JPanel group = vbox();
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = label(labelText, 12, Font.BOLD, Theme.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(42, field.getPreferredSize().height)));
        group.add(label);
        group.add(Box.createVerticalStrut(6));
        group.add(field);
        if (hint != null && !hint.isBlank()) {
            JLabel hintLabel = htmlLabel("<html><div style='width:720px'>"
                    + TextNormalizer.escapeHtml(hint) + "</div></html>", 11, Font.PLAIN, Theme.FAINT);
            hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(Box.createVerticalStrut(5));
            group.add(hintLabel);
        }
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, group.getPreferredSize().height));
        return group;
    }

    public static JScrollPane textAreaScroll(javax.swing.JTextArea area, int preferredHeight) {
        area.setFont(Theme.font(14, Font.PLAIN));
        area.setForeground(Theme.TEXT);
        area.setBackground(Theme.CARD);
        area.setCaretColor(Theme.ACCENT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        scroll.getViewport().setBackground(Theme.CARD);
        scroll.setPreferredSize(new Dimension(400, preferredHeight));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
        return scroll;
    }

    public static void styleTable(JTable table) {
        table.setFont(Theme.font(13, Font.PLAIN));
        table.setForeground(Theme.TEXT);
        table.setBackground(Theme.CARD);
        table.setRowHeight(46);
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(Theme.ACCENT_SOFT);
        table.setSelectionForeground(Theme.TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new PaddingRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 34));
        header.setDefaultRenderer(new HeaderRenderer());
        header.setBackground(new Color(0xF7F7F7));
        header.setForeground(Theme.MUTED);
    }

    public static JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.CARD);
        scroll.setBackground(Theme.CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        return scroll;
    }

    public static JPanel emptyState(String title, String hint) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel box = vbox();
        box.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedPanel badge = new RoundedPanel(Theme.ACCENT_SOFT, 32);
        badge.setLayout(new GridBagLayout());
        badge.setPreferredSize(new Dimension(64, 64));
        badge.setMaximumSize(new Dimension(64, 64));
        JLabel icon = label("?", 26, Font.BOLD, Theme.ACCENT);
        badge.add(icon);
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = label(title, 16, Font.BOLD, Theme.TEXT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hintLabel = htmlLabel("<html><div style='text-align:center;width:420px'>"
                + TextNormalizer.escapeHtml(hint) + "</div></html>", 12, Font.PLAIN, Theme.MUTED);
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(badge);
        box.add(Box.createVerticalStrut(14));
        box.add(titleLabel);
        box.add(Box.createVerticalStrut(6));
        box.add(hintLabel);
        wrapper.add(box);
        return wrapper;
    }

    public static String formatInt(int value) {
        return String.format(Locale.US, "%,d", value);
    }

    /** Trả về phần HTML đã escape, bọc từ khoá bằng span highlight. */
    public static String highlightHtml(String text, String keyword) {
        if (text == null) {
            return "";
        }
        if (keyword == null || keyword.isBlank()) {
            return TextNormalizer.escapeHtml(text);
        }
        String lowerText = text.toLowerCase(Locale.ROOT);
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(text.length() + 64);
        int index = 0;
        while (true) {
            int found = lowerText.indexOf(lowerKeyword, index);
            if (found < 0) {
                out.append(TextNormalizer.escapeHtml(text.substring(index)));
                break;
            }
            out.append(TextNormalizer.escapeHtml(text.substring(index, found)));
            out.append("<span style='background-color:#E5E5E5;color:#000000;'>");
            out.append(TextNormalizer.escapeHtml(text.substring(found, found + keyword.length())));
            out.append("</span>");
            index = found + keyword.length();
            if (index >= text.length()) {
                break;
            }
        }
        return out.toString();
    }

    public static JLabel sectionTitle(String text) {
        return label(text, 16, Font.BOLD, Theme.TEXT);
    }

    private static final class PaddingRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setFont(Theme.font(13, column == 0 ? Font.BOLD : Font.PLAIN));
            if (isSelected) {
                setBackground(Theme.ACCENT_SOFT);
            } else {
                setBackground(row % 2 == 0 ? Theme.CARD : new Color(0xFAFAFA));
            }
            setForeground(Theme.TEXT);
            setToolTipText(value == null ? null : value.toString());
            return this;
        }
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setBackground(new Color(0xF7F7F7));
            setForeground(Theme.MUTED);
            setFont(Theme.font(11, Font.BOLD));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                    new EmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }
}
