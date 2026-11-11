package vn.edu.hcmus.slang.ui.panel;

import vn.edu.hcmus.slang.model.Slang;
import vn.edu.hcmus.slang.ui.AppHost;
import vn.edu.hcmus.slang.ui.DemoScreen;
import vn.edu.hcmus.slang.ui.Dialogs;
import vn.edu.hcmus.slang.ui.Refreshable;
import vn.edu.hcmus.slang.ui.Theme;
import vn.edu.hcmus.slang.ui.Ui;
import vn.edu.hcmus.slang.ui.component.FlatButton;
import vn.edu.hcmus.slang.ui.component.RoundedPanel;
import vn.edu.hcmus.slang.ui.component.RoundedTextField;
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
import java.util.List;

/** Chức năng 4: thêm slang word mới, có xử lý trùng. */
public final class AddPanel extends JPanel implements Refreshable, DemoScreen {

    private final AppHost host;
    private final RoundedTextField wordField = new RoundedTextField("Ví dụ: GHOSTING", 30);
    private final JTextArea definitionsArea = new JTextArea(5, 40);
    private final JLabel errorLabel = Ui.label("", 12, Font.BOLD, Theme.DANGER);
    private final JLabel noteLabel = Ui.label("Dữ liệu được lưu ngay sau khi thêm.", 11, Font.PLAIN, Theme.FAINT);

    public AddPanel(AppHost host) {
        super(new BorderLayout());
        this.host = host;
        setBackground(Theme.BG);
        add(Ui.page("Thêm slang word",
                "Chức năng 4 · Nếu từ đã tồn tại, bạn có thể ghi đè hoặc thêm một bản trùng",
                buildBody()), BorderLayout.CENTER);
    }

    private JPanel buildBody() {
        RoundedPanel card = Ui.card(24);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 26, 22, 26));

        JLabel title = Ui.sectionTitle("Thông tin slang word");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = Ui.label("Các trường có dấu * là bắt buộc.", 12, Font.PLAIN, Theme.MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20));

        JPanel wordGroup = Ui.fieldGroup("Slang word *", wordField, null);
        card.add(wordGroup);
        card.add(Box.createVerticalStrut(16));

        JPanel definitionGroup = Ui.fieldGroup("Định nghĩa *",
                Ui.textAreaScroll(definitionsArea, 132),
                "Mỗi dòng là một định nghĩa, hoặc phân tách các định nghĩa bằng dấu |. "
                        + "Ví dụ: Hành vi cắt liên lạc đột ngột | Im lặng không lý do.");
        card.add(definitionGroup);
        card.add(Box.createVerticalStrut(8));

        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setVisible(false);
        card.add(errorLabel);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(18));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        footer.add(noteLabel, BorderLayout.WEST);
        FlatButton save = new FlatButton("Thêm slang word", FlatButton.Variant.PRIMARY);
        save.addActionListener(e -> saveEntry());
        footer.add(save, BorderLayout.EAST);
        card.add(footer);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 70, 0, 70));
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private void saveEntry() {
        String word = wordField.getText().trim();
        List<String> definitions = TextNormalizer.splitDefinitions(definitionsArea.getText());
        if (word.isEmpty()) {
            showError("Hãy nhập slang word.");
            wordField.requestFocusInWindow();
            return;
        }
        if (definitions.isEmpty()) {
            showError("Hãy nhập ít nhất một định nghĩa.");
            definitionsArea.requestFocusInWindow();
            return;
        }
        hideError();

        List<Slang> existing = host.dictionary().searchByWord(word);
        String status;
        if (!existing.isEmpty()) {
            Dialogs.DuplicateChoice choice = Dialogs.duplicateChoice(this, word, existing);
            if (choice == Dialogs.DuplicateChoice.CANCEL) {
                return;
            }
            if (choice == Dialogs.DuplicateChoice.OVERWRITE) {
                host.dictionary().overwrite(existing.get(0).getId(), definitions);
                status = "Đã ghi đè slang word “" + word + "”";
            } else {
                host.dictionary().add(word, definitions);
                status = "Đã thêm bản trùng cho “" + word + "”";
            }
        } else {
            host.dictionary().add(word, definitions);
            status = "Đã thêm slang word “" + word + "”";
        }

        if (PanelSupport.save(host, this, status)) {
            clearForm();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        revalidate();
        repaint();
    }

    private void hideError() {
        errorLabel.setVisible(false);
        revalidate();
        repaint();
    }

    private void clearForm() {
        wordField.setText("");
        definitionsArea.setText("");
        hideError();
        wordField.requestFocusInWindow();
    }

    @Override
    public void onShown() {
        wordField.requestFocusInWindow();
    }

    @Override
    public void prepareDemo() {
        wordField.setText("GHOSTING");
        definitionsArea.setText("Hành vi đột ngột cắt liên lạc\nIm lặng không lý do");
        definitionsArea.setCaretPosition(0);
    }
}
