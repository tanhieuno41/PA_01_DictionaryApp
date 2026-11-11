package vn.edu.hcmus.slang.ui.panel;

import vn.edu.hcmus.slang.model.QuizQuestion;
import vn.edu.hcmus.slang.service.QuizService;
import vn.edu.hcmus.slang.ui.AppHost;
import vn.edu.hcmus.slang.ui.DemoScreen;
import vn.edu.hcmus.slang.ui.Refreshable;
import vn.edu.hcmus.slang.ui.Theme;
import vn.edu.hcmus.slang.ui.Ui;
import vn.edu.hcmus.slang.ui.component.Chip;
import vn.edu.hcmus.slang.ui.component.FlatButton;
import vn.edu.hcmus.slang.ui.component.QuizOption;
import vn.edu.hcmus.slang.ui.component.RoundedPanel;
import vn.edu.hcmus.slang.util.TextNormalizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/** Chức năng 9 &amp; 10: đố vui 4 đáp án. */
public final class QuizPanel extends JPanel implements Refreshable, DemoScreen {

    private final AppHost host;
    private final QuizQuestion.Mode mode;
    private final QuizService quizService;
    private final JLabel typeLabel = Ui.label("", 10, Font.BOLD, Theme.FAINT);
    private final JLabel promptCaption = Ui.label("", 10, Font.BOLD, Theme.FAINT);
    private final JLabel promptWord = Ui.label("", 34, Font.BOLD, Theme.TEXT);
    private final JTextArea promptDefinition = new JTextArea(2, 40);
    private final RoundedPanel promptCard = new RoundedPanel(new java.awt.Color(0xFAFAFA), 12);
    private final JPanel promptContent = Ui.vbox();
    private final JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 0, 10));
    private final List<QuizOption> optionComponents = new ArrayList<>();
    private final JLabel feedbackLabel = Ui.htmlLabel("Chọn một đáp án để kiểm tra.", 12, Font.PLAIN, Theme.MUTED);
    private final Chip scoreChip = new Chip("Đúng 0/0", Theme.ACCENT_SOFT, Theme.ACCENT);
    private final FlatButton nextButton = new FlatButton("Câu tiếp theo", FlatButton.Variant.PRIMARY);
    private QuizQuestion question;
    private boolean answered;
    private int correctCount;
    private int totalCount;

    public QuizPanel(AppHost host, QuizQuestion.Mode mode) {
        super(new BorderLayout());
        this.host = host;
        this.mode = mode;
        this.quizService = new QuizService(host.dictionary());
        setBackground(Theme.BG);
        String title = mode == QuizQuestion.Mode.WORD_TO_DEFINITION
                ? "Đố vui: đoán định nghĩa" : "Đố vui: đoán slang word";
        String subtitle = mode == QuizQuestion.Mode.WORD_TO_DEFINITION
                ? "Chức năng 9 · Hiển thị một slang word và 4 định nghĩa để chọn"
                : "Chức năng 10 · Hiển thị một định nghĩa và 4 slang word để chọn";
        add(Ui.page(title, subtitle, buildBody()), BorderLayout.CENTER);
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JPanel left = Ui.hbox();
        typeLabel.setText(mode == QuizQuestion.Mode.WORD_TO_DEFINITION
                ? "WORD  →  ĐỊNH NGHĨA" : "ĐỊNH NGHĨA  →  WORD");
        Chip modeChip = new Chip(typeLabel.getText(), Theme.ACCENT_SOFT, Theme.ACCENT);
        left.add(modeChip);
        left.add(Box.createHorizontalStrut(8));
        left.add(scoreChip);
        topBar.add(left, BorderLayout.WEST);
        FlatButton resetScore = new FlatButton("Đặt lại điểm", FlatButton.Variant.GHOST, 34);
        resetScore.addActionListener(e -> resetScore());
        topBar.add(resetScore, BorderLayout.EAST);

        RoundedPanel card = Ui.card(22);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        promptCard.setLayout(new BoxLayout(promptCard, BoxLayout.Y_AXIS));
        promptCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        promptCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 138));
        promptCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        promptCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
        promptWord.setAlignmentX(Component.LEFT_ALIGNMENT);
        promptDefinition.setEditable(false);
        promptDefinition.setFocusable(false);
        promptDefinition.setOpaque(false);
        promptDefinition.setLineWrap(true);
        promptDefinition.setWrapStyleWord(true);
        promptDefinition.setFont(Theme.font(15, Font.PLAIN));
        promptDefinition.setForeground(Theme.TEXT);
        promptDefinition.setBorder(new EmptyBorder(2, 0, 0, 0));
        promptDefinition.setAlignmentX(Component.LEFT_ALIGNMENT);
        promptDefinition.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        promptContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        promptContent.add(Box.createVerticalGlue());
        promptContent.add(promptWord);
        promptContent.add(Box.createVerticalStrut(4));
        promptContent.add(promptDefinition);
        promptContent.add(Box.createVerticalGlue());
        promptCard.add(promptCaption);
        promptCard.add(Box.createVerticalStrut(2));
        promptCard.add(promptContent);
        card.add(promptCard);
        card.add(Box.createVerticalStrut(16));
        card.add(createOptionsPanel());
        card.add(Box.createVerticalStrut(12));
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(feedbackLabel);
        card.add(Box.createVerticalStrut(10));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JLabel hint = Ui.label("Đáp án đúng có viền đậm, đáp án sai có viền nét đứt.", 11, Font.PLAIN, Theme.FAINT);
        footer.add(hint, BorderLayout.WEST);
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> nextQuestion());
        footer.add(nextButton, BorderLayout.EAST);
        card.add(footer);

        body.add(topBar, BorderLayout.NORTH);
        body.add(card, BorderLayout.CENTER);
        return body;
    }

    private JPanel createOptionsPanel() {
        optionsPanel.setOpaque(false);
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 286));
        optionsPanel.setPreferredSize(new Dimension(600, 286));
        return optionsPanel;
    }

    private void nextQuestion() {
        try {
            question = quizService.nextQuestion(mode);
        } catch (RuntimeException ex) {
            feedbackLabel.setText("<html><span style='color:#000000'>" + TextNormalizer.escapeHtml(ex.getMessage())
                    + "</span></html>");
            nextButton.setEnabled(false);
            return;
        }
        answered = false;
        promptCaption.setText(mode == QuizQuestion.Mode.WORD_TO_DEFINITION
                ? "SLANG WORD" : "ĐỊNH NGHĨA");
        if (mode == QuizQuestion.Mode.WORD_TO_DEFINITION) {
            promptWord.setText(question.prompt());
            promptWord.setVisible(true);
            promptDefinition.setVisible(false);
            promptDefinition.setText("");
        } else {
            promptWord.setVisible(false);
            promptWord.setText("");
            promptDefinition.setVisible(true);
            promptDefinition.setText(question.prompt());
            promptDefinition.setCaretPosition(0);
        }
        rebuildOptions();
        feedbackLabel.setFont(Theme.font(12, Font.PLAIN));
        feedbackLabel.setForeground(Theme.MUTED);
        feedbackLabel.setText("Chọn một đáp án để kiểm tra.");
        nextButton.setEnabled(false);
        updateScore();
        promptCard.revalidate();
        promptCard.repaint();
    }

    private void rebuildOptions() {
        optionsPanel.removeAll();
        optionComponents.clear();
        char[] letters = {'A', 'B', 'C', 'D'};
        for (int i = 0; i < question.choices().size(); i++) {
            final int index = i;
            QuizOption option = new QuizOption(letters[i],
                    TextNormalizer.escapeHtml(question.choices().get(i)),
                    () -> answer(index));
            option.setState(QuizOption.State.DEFAULT);
            option.setClickable(true);
            optionComponents.add(option);
            optionsPanel.add(option);
        }
        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private void answer(int selectedIndex) {
        if (answered || question == null) {
            return;
        }
        answered = true;
        totalCount++;
        boolean correct = selectedIndex == question.correctIndex();
        if (correct) {
            correctCount++;
        }
        for (int i = 0; i < optionComponents.size(); i++) {
            QuizOption option = optionComponents.get(i);
            option.setClickable(false);
            if (i == question.correctIndex()) {
                option.setState(QuizOption.State.CORRECT);
            } else if (i == selectedIndex) {
                option.setState(QuizOption.State.WRONG);
            } else {
                option.setState(QuizOption.State.MUTED);
            }
        }
        String answer = TextNormalizer.escapeHtml(question.correctChoice());
        if (correct) {
            feedbackLabel.setForeground(Theme.SUCCESS);
            feedbackLabel.setText("<html><div style='width:800px'><b>Chính xác!</b> "
                    + TextNormalizer.escapeHtml(shorten(question.explanation(), 300)) + "</div></html>");
        } else {
            feedbackLabel.setForeground(Theme.DANGER);
            feedbackLabel.setText("<html><div style='width:800px'><b>Chưa đúng.</b> Đáp án đúng: "
                    + answer + "<br><span style='color:#525252'>"
                    + TextNormalizer.escapeHtml(shorten(question.explanation(), 280))
                    + "</span></div></html>");
        }
        updateScore();
        nextButton.setEnabled(true);
    }

    private void resetScore() {
        correctCount = 0;
        totalCount = 0;
        updateScore();
        nextQuestion();
    }

    private void updateScore() {
        if (totalCount == 0) {
            scoreChip.setText("Đúng 0/0");
            scoreChip.setPalette(Theme.ACCENT_SOFT, Theme.ACCENT);
            return;
        }
        int percent = (int) Math.round(correctCount * 100.0 / totalCount);
        scoreChip.setText("Đúng " + correctCount + "/" + totalCount + " · " + percent + "%");
        if (percent >= 70) {
            scoreChip.setPalette(Theme.SUCCESS_SOFT, Theme.SUCCESS);
        } else if (percent >= 40) {
            scoreChip.setPalette(Theme.WARNING_SOFT, Theme.WARNING);
        } else {
            scoreChip.setPalette(Theme.DANGER_SOFT, Theme.DANGER);
        }
    }

    private static String shorten(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    @Override
    public void onShown() {
        if (question == null) {
            nextQuestion();
        }
    }

    @Override
    public void onDataChanged() {
        if (question != null && host.dictionary().getById(question.slang().getId()) == null) {
            nextQuestion();
        }
    }

    @Override
    public void prepareDemo() {
        if (question == null) {
            nextQuestion();
        }
    }

    /** Dùng cho ảnh kiểm thử: chọn một đáp án sai để thấy cả trạng thái đúng/sai. */
    public void demoAnswerWrong() {
        if (question != null && !answered) {
            int wrong = (question.correctIndex() + 1) % question.choices().size();
            answer(wrong);
        }
    }
}
