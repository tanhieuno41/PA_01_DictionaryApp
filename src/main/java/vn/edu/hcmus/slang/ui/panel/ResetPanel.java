package vn.edu.hcmus.slang.ui.panel;

import vn.edu.hcmus.slang.ui.AppHost;
import vn.edu.hcmus.slang.ui.DemoScreen;
import vn.edu.hcmus.slang.ui.Dialogs;
import vn.edu.hcmus.slang.ui.Refreshable;
import vn.edu.hcmus.slang.ui.Theme;
import vn.edu.hcmus.slang.ui.Ui;
import vn.edu.hcmus.slang.ui.component.FlatButton;
import vn.edu.hcmus.slang.ui.component.RoundedPanel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.io.IOException;

/** Chức năng 7: khôi phục danh sách slang word gốc. */
public final class ResetPanel extends JPanel implements Refreshable, DemoScreen {

    private final AppHost host;
    private final JLabel currentLabel = Ui.label("—", 22, Font.BOLD, Theme.TEXT);
    private final JLabel originalLabel = Ui.label("—", 22, Font.BOLD, Theme.TEXT);
    private final JLabel pathLabel = Ui.label("", 11, Font.PLAIN, Theme.FAINT);

    public ResetPanel(AppHost host) {
        super(new BorderLayout());
        this.host = host;
        setBackground(Theme.BG);
        add(Ui.page("Khôi phục dữ liệu gốc",
                "Chức năng 7 · Đưa danh sách slang word trở về đúng file slang.txt ban đầu",
                buildBody()), BorderLayout.CENTER);
        refreshCounts();
    }

    private JPanel buildBody() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        RoundedPanel card = Ui.card(28);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(700, 480));

        RoundedPanel badge = new RoundedPanel(Theme.WARNING_SOFT, 38);
        badge.setLayout(new GridBagLayout());
        badge.setPreferredSize(new Dimension(76, 76));
        badge.setMaximumSize(new Dimension(76, 76));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        badge.add(Ui.label("!", 34, Font.BOLD, Theme.WARNING));
        card.add(badge);
        card.add(Box.createVerticalStrut(18));

        JLabel title = Ui.label("Khôi phục danh sách slang word gốc", 22, Font.BOLD, Theme.TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(10));

        JLabel description = Ui.htmlLabel("<html><div style='text-align:center;width:600px'>"
                + "Toàn bộ thay đổi thêm / sửa / xóa sẽ bị hủy và dữ liệu được nạp lại từ file "
                + "<b>slang.txt</b> gốc.<br>Lịch sử tra cứu không bị ảnh hưởng."
                + "</div></html>", 13, Font.PLAIN, Theme.MUTED);
        description.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(description);
        card.add(Box.createVerticalStrut(24));

        JPanel stats = new JPanel(new java.awt.GridLayout(1, 2, 14, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(Component.CENTER_ALIGNMENT);
        stats.setMaximumSize(new Dimension(560, 92));
        stats.add(statCard("Hiện tại", currentLabel));
        stats.add(statCard("Dữ liệu gốc", originalLabel));
        card.add(stats);
        card.add(Box.createVerticalStrut(16));

        pathLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(pathLabel);
        card.add(Box.createVerticalGlue());

        FlatButton reset = new FlatButton("Khôi phục dữ liệu gốc", FlatButton.Variant.DANGER, 44);
        reset.setAlignmentX(Component.CENTER_ALIGNMENT);
        reset.addActionListener(e -> reset());
        card.add(reset);

        wrapper.add(card);
        return wrapper;
    }

    private JPanel statCard(String title, JLabel value) {
        RoundedPanel panel = new RoundedPanel(new java.awt.Color(0xFAFAFA), 12);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel titleLabel = Ui.label(title, 10, Font.BOLD, Theme.FAINT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        value.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(value);
        return panel;
    }

    private void reset() {
        boolean confirmed = Dialogs.confirm(this, "Khôi phục dữ liệu gốc",
                "<html><div style='width:420px'>Mọi thay đổi hiện tại sẽ bị mất.<br>"
                        + "<b>Bạn chắc chắn muốn khôi phục?</b></div></html>",
                "Khôi phục");
        if (!confirmed) {
            return;
        }
        try {
            int count = host.store().resetToOriginal(host.dictionary()).size();
            host.notifyDataChanged();
            host.setStatus("Đã khôi phục " + Ui.formatInt(count) + " slang word gốc");
            refreshCounts();
        } catch (IOException ex) {
            Dialogs.error(this, "Không khôi phục được",
                    "<html><div style='width:420px'>Không đọc được file dữ liệu gốc:<br><b>"
                            + ex.getMessage() + "</b></div></html>");
        }
    }

    private void refreshCounts() {
        currentLabel.setText(Ui.formatInt(host.dictionary().size()));
        try {
            originalLabel.setText(Ui.formatInt(host.store().originalEntryCount()));
        } catch (IOException ex) {
            originalLabel.setText("—");
        }
        pathLabel.setText(host.store().getOriginalFile().toString());
        pathLabel.setToolTipText(pathLabel.getText());
    }

    @Override
    public void onShown() {
        refreshCounts();
    }

    @Override
    public void prepareDemo() {
        refreshCounts();
    }
}
