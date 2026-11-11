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

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/** Chức năng 8: random slang word (On this day). */
public final class RandomPanel extends JPanel implements Refreshable, DemoScreen {

    private final AppHost host;
    private final JLabel wordLabel = Ui.label("—", 52, Font.BOLD, Color.WHITE);
    private final JTextArea definitionsArea = new JTextArea();
    private final JLabel dateLabel = Ui.label("", 11, Font.PLAIN, new Color(0xE0E7FF));
    private final Chip counterChip = new Chip("", new Color(255, 255, 255, 30), Color.WHITE);
    private Slang current;

    public RandomPanel(AppHost host) {
        super(new BorderLayout());
        this.host = host;
        setBackground(Theme.BG);
        add(Ui.page("Slang word mỗi ngày",
                "Chức năng 8 · Một slang word ngẫu nhiên để khám phá mỗi lần mở",
                buildBody()), BorderLayout.CENTER);
    }

    private JPanel buildBody() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        GradientPanel hero = new GradientPanel(new Color(0x4F46E5), new Color(0x9333EA), 24);
        hero.setLayout(new BorderLayout(0, 16));
        hero.setBorder(new EmptyBorder(30, 36, 30, 36));
        hero.setPreferredSize(new Dimension(900, 440));

        JPanel top = Ui.hbox();
        top.add(new Chip("ON THIS DAY", new Color(255, 255, 255, 36), Color.WHITE));
        top.add(Box.createHorizontalStrut(10));
        top.add(counterChip);
        top.add(Box.createHorizontalGlue());
        top.add(dateLabel);
        hero.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        wordLabel.setForeground(Color.WHITE);
        center.add(wordLabel, BorderLayout.NORTH);

        definitionsArea.setEditable(false);
        definitionsArea.setFocusable(false);
        definitionsArea.setOpaque(false);
        definitionsArea.setFont(Theme.font(16, Font.PLAIN));
        definitionsArea.setForeground(new Color(0xEDE9FE));
        definitionsArea.setLineWrap(true);
        definitionsArea.setWrapStyleWord(true);
        definitionsArea.setBorder(new EmptyBorder(0, 0, 0, 0));
        center.add(definitionsArea, BorderLayout.CENTER);
        hero.add(center, BorderLayout.CENTER);

        JPanel bottom = Ui.hbox();
        FlatButton another = new FlatButton("Từ ngẫu nhiên khác", FlatButton.Variant.LIGHT, 44);
        another.addActionListener(e -> nextWord());
        bottom.add(another);
        bottom.add(Box.createHorizontalStrut(10));
        FlatButton quiz = new FlatButton("Thử đố vui", FlatButton.Variant.LIGHT, 44);
        quiz.addActionListener(e -> host.navigate("quiz-word"));
        bottom.add(quiz);
        bottom.add(Box.createHorizontalGlue());
        hero.add(bottom, BorderLayout.SOUTH);

        wrapper.add(hero);
        return wrapper;
    }

    private void nextWord() {
        current = host.dictionary().randomEntry(new Random());
        render();
    }

    private void render() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        if (current == null) {
            wordLabel.setText("Chưa có dữ liệu");
            definitionsArea.setText("Hãy thêm slang word hoặc khôi phục dữ liệu gốc.");
            counterChip.setText("0 mục từ");
            return;
        }
        wordLabel.setText(current.getWord());
        StringBuilder text = new StringBuilder();
        for (String definition : current.getDefinitions()) {
            if (text.length() > 0) {
                text.append('\n');
            }
            text.append("• ").append(definition);
        }
        definitionsArea.setText(text.toString());
        definitionsArea.setCaretPosition(0);
        counterChip.setText(Ui.formatInt(host.dictionary().size()) + " mục từ");
    }

    @Override
    public void onShown() {
        if (current == null || host.dictionary().getById(current.getId()) == null) {
            nextWord();
        } else {
            render();
        }
    }

    @Override
    public void prepareDemo() {
        if (current == null) {
            nextWord();
        }
    }
}
