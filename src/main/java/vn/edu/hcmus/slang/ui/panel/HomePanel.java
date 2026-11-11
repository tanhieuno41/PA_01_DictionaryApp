package vn.edu.hcmus.slang.ui.panel;

import vn.edu.hcmus.slang.model.Slang;
import vn.edu.hcmus.slang.ui.AppHost;
import vn.edu.hcmus.slang.ui.DemoScreen;
import vn.edu.hcmus.slang.ui.Refreshable;
import vn.edu.hcmus.slang.ui.Theme;
import vn.edu.hcmus.slang.ui.Ui;
import vn.edu.hcmus.slang.ui.component.Chip;
import vn.edu.hcmus.slang.ui.component.FlatButton;
import vn.edu.hcmus.slang.ui.component.GradientPanel;
import vn.edu.hcmus.slang.ui.component.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/** Trang tổng quan với thống kê, từ của ngày và thao tác nhanh. */
public final class HomePanel extends JPanel implements Refreshable, DemoScreen {

    private final AppHost host;
    private final StatCard entriesCard = new StatCard("Tổng slang word", "mục từ trong từ điển", Theme.ACCENT);
    private final StatCard wordsCard = new StatCard("Từ duy nhất", "sau khi chuẩn hoá khoá", new Color(0x525252));
    private final StatCard definitionsCard = new StatCard("Định nghĩa", "tổng số nghĩa đã lưu", Theme.SUCCESS);
    private final StatCard historyCard = new StatCard("Lịch sử", "lượt tra cứu gần đây", Theme.WARNING);

    private final JLabel dailyWordLabel = Ui.label("—", 44, Font.BOLD, Color.WHITE);
    private final JLabel dailyDateLabel = Ui.label("", 11, Font.PLAIN, new Color(0xE5E5E5));
    private final JTextArea dailyDefinitions = new JTextArea();
    private final JLabel dailyCaption = Ui.label("SLANG WORD OF THE DAY", 10, Font.BOLD, new Color(0xD4D4D4));
    private final JLabel sourceLabel = Ui.label("", 12, Font.BOLD, Theme.TEXT);
    private final Chip indexChip = new Chip("Đang kiểm tra", Theme.ACCENT_SOFT, Theme.ACCENT);
    private Slang dailyWord;

    public HomePanel(AppHost host) {
        super(new BorderLayout());
        this.host = host;
        setBackground(Theme.BG);
        setOpaque(true);
        add(Ui.page("Tổng quan", "Slang Dictionary · Đồ án CSC13002 · Java IO, OOP, Generic & Collections",
                buildBody()), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(entriesCard);
        stats.add(wordsCard);
        stats.add(definitionsCard);
        stats.add(historyCard);
        stats.setPreferredSize(new Dimension(0, 104));

        JPanel middle = new JPanel(new GridBagLayout());
        middle.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.weightx = 0.62;
        constraints.insets = new Insets(0, 0, 0, 16);
        middle.add(buildHero(), constraints);
        constraints.gridx = 1;
        constraints.weightx = 0.38;
        constraints.insets = new Insets(0, 0, 0, 0);
        middle.add(buildQuickActions(), constraints);

        body.add(stats, BorderLayout.NORTH);
        body.add(middle, BorderLayout.CENTER);
        body.add(buildSystemCard(), BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildHero() {
        GradientPanel hero = new GradientPanel(new Color(0x0A0A0A), new Color(0x3A3A3A), 20);
        hero.setLayout(new BorderLayout(0, 12));
        hero.setBorder(new EmptyBorder(22, 26, 22, 26));

        JPanel top = Ui.hbox();
        Chip today = new Chip("ON THIS DAY", new Color(255, 255, 255, 38), Color.WHITE);
        top.add(today);
        top.add(Box.createHorizontalStrut(10));
        JLabel caption = dailyCaption;
        caption.setForeground(new Color(0xE5E5E5));
        top.add(caption);
        top.add(Box.createHorizontalGlue());
        dailyDateLabel.setForeground(new Color(0xE5E5E5));
        top.add(dailyDateLabel);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        dailyWordLabel.setForeground(Color.WHITE);
        dailyWordLabel.setBorder(new EmptyBorder(0, 0, 2, 0));
        center.add(dailyWordLabel, BorderLayout.NORTH);

        dailyDefinitions.setEditable(false);
        dailyDefinitions.setFocusable(false);
        dailyDefinitions.setOpaque(false);
        dailyDefinitions.setFont(Theme.font(13, Font.PLAIN));
        dailyDefinitions.setForeground(new Color(0xE5E5E5));
        dailyDefinitions.setLineWrap(true);
        dailyDefinitions.setWrapStyleWord(true);
        dailyDefinitions.setBorder(new EmptyBorder(0, 0, 0, 0));
        dailyDefinitions.setPreferredSize(new Dimension(300, 88));
        center.add(dailyDefinitions, BorderLayout.CENTER);

        JPanel bottom = Ui.hbox();
        FlatButton another = new FlatButton("Từ ngẫu nhiên khác", FlatButton.Variant.LIGHT);
        another.addActionListener(e -> {
            dailyWord = host.dictionary().randomEntry(new Random());
            renderDailyWord();
        });
        bottom.add(another);
        bottom.add(Box.createHorizontalGlue());

        hero.add(top, BorderLayout.NORTH);
        hero.add(center, BorderLayout.CENTER);
        hero.add(bottom, BorderLayout.SOUTH);
        return hero;
    }

    private JPanel buildQuickActions() {
        RoundedPanel card = Ui.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = Ui.vbox();
        JLabel title = Ui.sectionTitle("Thao tác nhanh");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hint = Ui.label("Đi thẳng tới chức năng thường dùng", 12, Font.PLAIN, Theme.MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(hint);

        JPanel buttons = Ui.vbox();
        buttons.add(quickButton("Tra cứu slang word", "search"));
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(quickButton("Thêm slang word", "add"));
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(quickButton("Đố vui: đoán định nghĩa", "quiz-word"));
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(quickButton("Slang word ngẫu nhiên", "random"));

        card.add(header, BorderLayout.NORTH);
        card.add(buttons, BorderLayout.CENTER);
        return card;
    }

    private FlatButton quickButton(String text, String target) {
        FlatButton button = new FlatButton(text, FlatButton.Variant.SECONDARY, 42);
        button.addActionListener(e -> host.navigate(target));
        return button;
    }

    private JPanel buildSystemCard() {
        RoundedPanel card = Ui.card(16);
        card.setLayout(new BorderLayout(24, 0));
        card.setPreferredSize(new Dimension(0, 104));

        JPanel left = Ui.vbox();
        JLabel title = Ui.label("CẤU TRÚC DỮ LIỆU", 10, Font.BOLD, Theme.FAINT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(title);
        left.add(Box.createVerticalStrut(8));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        chips.add(new Chip("HashMap O(1)", Theme.ACCENT_SOFT, Theme.ACCENT));
        chips.add(new Chip("Trigram inverted index", Theme.SUCCESS_SOFT, Theme.SUCCESS));
        chips.add(new Chip("Binary snapshot", Theme.WARNING_SOFT, Theme.WARNING));
        left.add(chips);
        left.add(Box.createVerticalStrut(7));
        JLabel note = Ui.label("Tra cứu tức thời · không đánh chỉ mục lại khi khởi động", 11, Font.PLAIN, Theme.MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(note);

        JPanel right = Ui.vbox();
        right.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.BORDER),
                new EmptyBorder(0, 22, 0, 0)));
        JLabel dataTitle = Ui.label("DỮ LIỆU", 10, Font.BOLD, Theme.FAINT);
        dataTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sourceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        indexChip.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(dataTitle);
        right.add(Box.createVerticalStrut(7));
        right.add(sourceLabel);
        right.add(Box.createVerticalStrut(6));
        right.add(indexChip);
        right.setPreferredSize(new Dimension(360, 0));

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    @Override
    public void onShown() {
        refresh();
    }

    @Override
    public void onDataChanged() {
        dailyWord = null;
        dailyDefinitions.setText("");
        refresh();
    }

    private void refresh() {
        entriesCard.setValue(Ui.formatInt(host.dictionary().size()));
        wordsCard.setValue(Ui.formatInt(host.dictionary().uniqueWordCount()));
        definitionsCard.setValue(Ui.formatInt(host.dictionary().definitionCount()));
        historyCard.setValue(Ui.formatInt(host.history().size()));
        dailyDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (dailyWord == null || host.dictionary().getById(dailyWord.getId()) == null) {
            dailyWord = host.dictionary().randomEntry(new Random(LocalDate.now().toEpochDay()));
        }
        renderDailyWord();

        sourceLabel.setText(host.loadReport().sourceFile().getFileName().toString());
        sourceLabel.setToolTipText(host.loadReport().sourceFile().toString());
        if (host.loadReport().indexLoaded()) {
            indexChip.setText("Chỉ mục nạp từ đĩa · " + host.loadReport().loadMillis() + " ms");
            indexChip.setPalette(Theme.SUCCESS_SOFT, Theme.SUCCESS);
        } else {
            indexChip.setText("Đã xây chỉ mục · " + host.loadReport().indexBuildMillis() + " ms");
            indexChip.setPalette(Theme.WARNING_SOFT, Theme.WARNING);
        }
    }

    private void renderDailyWord() {
        if (dailyWord == null) {
            dailyWordLabel.setText("Chưa có dữ liệu");
            dailyDefinitions.setText("Hãy thêm slang word hoặc khôi phục dữ liệu gốc.");
            return;
        }
        dailyWordLabel.setText(dailyWord.getWord());
        StringBuilder text = new StringBuilder();
        for (String definition : dailyWord.getDefinitions()) {
            if (text.length() > 0) {
                text.append('\n');
            }
            text.append("• ").append(definition);
        }
        dailyDefinitions.setText(text.toString());
        dailyDefinitions.setCaretPosition(0);
    }

    @Override
    public void prepareDemo() {
        onShown();
    }

    /** Thẻ thống kê nhỏ. */
    private static final class StatCard extends RoundedPanel {

        private final JLabel value = Ui.label("0", 28, Font.BOLD, Theme.TEXT);

        StatCard(String title, String hint, Color accent) {
            super(Theme.CARD, Theme.BORDER, 16);
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(15, 16, 14, 16));

            RoundedPanel bar = new RoundedPanel(accent, 2);
            bar.setPreferredSize(new Dimension(4, 0));
            add(bar, BorderLayout.WEST);

            JPanel text = Ui.vbox();
            JLabel titleLabel = Ui.label(title, 11, Font.BOLD, Theme.MUTED);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            value.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel hintLabel = Ui.label(hint, 10, Font.PLAIN, Theme.FAINT);
            hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.add(titleLabel);
            text.add(Box.createVerticalStrut(7));
            text.add(value);
            text.add(Box.createVerticalStrut(2));
            text.add(hintLabel);
            add(text, BorderLayout.CENTER);
        }

        void setValue(String value) {
            this.value.setText(value);
        }
    }
}
