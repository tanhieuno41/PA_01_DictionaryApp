package vn.edu.hcmus.slang.ui.panel;

import vn.edu.hcmus.slang.model.Slang;
import vn.edu.hcmus.slang.ui.AppHost;
import vn.edu.hcmus.slang.ui.DemoScreen;
import vn.edu.hcmus.slang.ui.Dialogs;
import vn.edu.hcmus.slang.ui.Refreshable;
import vn.edu.hcmus.slang.ui.Theme;
import vn.edu.hcmus.slang.ui.Ui;
import vn.edu.hcmus.slang.ui.component.Chip;
import vn.edu.hcmus.slang.ui.component.EmptyState;
import vn.edu.hcmus.slang.ui.component.FlatButton;
import vn.edu.hcmus.slang.ui.component.RoundedPanel;
import vn.edu.hcmus.slang.ui.component.RoundedTextField;
import vn.edu.hcmus.slang.ui.component.SlangTableModel;
import vn.edu.hcmus.slang.util.TextNormalizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/** Chức năng 5: chỉnh sửa một slang word. */
public final class EditPanel extends JPanel implements Refreshable, DemoScreen {

    private final AppHost host;
    private final RoundedTextField searchField = new RoundedTextField("Nhập slang word cần sửa", 24);
    private final SlangTableModel model = new SlangTableModel();
    private final JTable table = new JTable(model);
    private final JLabel resultLabel = Ui.label("Tìm slang word để bắt đầu", 13, Font.BOLD, Theme.TEXT);
    private final EmptyState emptyState = new EmptyState("Chưa có mục từ nào được chọn",
            "Nhập slang word, chọn một dòng trong bảng rồi chỉnh sửa nội dung ở khung bên dưới.");

    private final RoundedTextField wordField = new RoundedTextField("Slang word", 30);
    private final JTextArea definitionsArea = new JTextArea(4, 40);
    private final Chip idChip = new Chip("Chưa chọn mục từ", Theme.ACCENT_SOFT, Theme.ACCENT);
    private final JLabel errorLabel = Ui.label("", 12, Font.BOLD, Theme.DANGER);
    private final FlatButton saveButton = new FlatButton("Lưu thay đổi", FlatButton.Variant.PRIMARY);
    private final CardLayout resultCards = new CardLayout();
    private final JPanel resultBody = new JPanel(resultCards);
    private String lastKeyword;
    private Slang editing;

    public EditPanel(AppHost host) {
        super(new BorderLayout());
        this.host = host;
        setBackground(Theme.BG);
        add(Ui.page("Chỉnh sửa slang word",
                "Chức năng 5 · Tìm từ, chọn đúng mục từ rồi cập nhật nội dung",
                buildBody()), BorderLayout.CENTER);
        setFormEnabled(false);
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);

        RoundedPanel searchCard = Ui.card(14);
        searchCard.setLayout(new BorderLayout(10, 0));
        FlatButton searchButton = new FlatButton("Tìm", FlatButton.Variant.PRIMARY);
        searchButton.addActionListener(e -> runSearch(searchField.getText()));
        searchField.addActionListener(e -> runSearch(searchField.getText()));
        searchCard.add(searchField, BorderLayout.CENTER);
        searchCard.add(searchButton, BorderLayout.EAST);
        body.add(searchCard, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(buildResultCard(), BorderLayout.CENTER);
        center.add(buildEditCard(), BorderLayout.SOUTH);
        body.add(center, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildResultCard() {
        RoundedPanel card = Ui.card(16);
        card.setLayout(new BorderLayout(0, 10));
        card.add(resultLabel, BorderLayout.NORTH);

        Ui.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(0).setMaxWidth(300);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::selectionChanged);

        resultBody.setOpaque(false);
        resultBody.add(emptyState, "empty");
        resultBody.add(Ui.tableScroll(table), "table");
        card.add(resultBody, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildEditCard() {
        RoundedPanel card = Ui.card(18);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(0, 360));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        header.add(Ui.sectionTitle("Nội dung chỉnh sửa"), BorderLayout.WEST);
        header.add(idChip, BorderLayout.EAST);
        card.add(header);
        card.add(Box.createVerticalStrut(12));

        JPanel wordGroup = Ui.fieldGroup("Slang word *", wordField, null);
        card.add(wordGroup);
        card.add(Box.createVerticalStrut(10));

        JPanel definitionGroup = Ui.fieldGroup("Định nghĩa *",
                Ui.textAreaScroll(definitionsArea, 82),
                "Mỗi dòng là một định nghĩa, hoặc phân tách bằng dấu |.");
        card.add(definitionGroup);
        card.add(Box.createVerticalStrut(6));

        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setVisible(false);
        card.add(errorLabel);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel hint = Ui.label("Thay đổi được lưu vào file dữ liệu ngay khi bấm Lưu.", 11, Font.PLAIN, Theme.FAINT);
        footer.add(hint, BorderLayout.WEST);
        saveButton.addActionListener(e -> saveChanges());
        footer.add(saveButton, BorderLayout.EAST);
        card.add(footer);
        return card;
    }

    private void runSearch(String keyword) {
        String query = keyword == null ? "" : keyword.trim();
        if (query.isEmpty()) {
            resultLabel.setText("Vui lòng nhập slang word");
            searchField.requestFocusInWindow();
            return;
        }
        List<Slang> results = host.dictionary().searchByWord(query);
        lastKeyword = query;
        model.setRows(results, query, false);
        table.clearSelection();
        clearForm();
        if (results.isEmpty()) {
            emptyState.setContent("Không tìm thấy “" + query + "”",
                    "Kiểm tra lại chính tả hoặc dùng chức năng Tìm theo định nghĩa.");
            resultCards.show(resultBody, "empty");
            resultLabel.setText("Không tìm thấy kết quả");
        } else {
            resultCards.show(resultBody, "table");
            resultLabel.setText("Tìm thấy " + results.size() + " mục từ — chọn một dòng để sửa");
            table.setRowSelectionInterval(0, 0);
        }
    }

    private void selectionChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            clearForm();
            return;
        }
        Slang slang = model.getSlangAt(table.convertRowIndexToModel(row));
        if (slang == null) {
            clearForm();
            return;
        }
        editing = slang;
        wordField.setText(slang.getWord());
        definitionsArea.setText(String.join("\n", slang.getDefinitions()));
        definitionsArea.setCaretPosition(0);
        idChip.setText("ID #" + slang.getId());
        idChip.setPalette(Theme.SUCCESS_SOFT, Theme.SUCCESS);
        setFormEnabled(true);
        hideError();
    }

    private void saveChanges() {
        if (editing == null) {
            Dialogs.error(this, "Chưa chọn mục từ", "Hãy tìm và chọn một slang word trước khi lưu.");
            return;
        }
        String word = wordField.getText().trim();
        List<String> definitions = TextNormalizer.splitDefinitions(definitionsArea.getText());
        if (word.isEmpty()) {
            showError("Slang word không được để trống.");
            wordField.requestFocusInWindow();
            return;
        }
        if (definitions.isEmpty()) {
            showError("Cần ít nhất một định nghĩa.");
            definitionsArea.requestFocusInWindow();
            return;
        }

        List<Slang> sameWord = host.dictionary().searchByWord(word).stream()
                .filter(item -> item.getId() != editing.getId())
                .toList();
        if (!sameWord.isEmpty()) {
            boolean confirmed = Dialogs.confirm(this, "Slang word đã tồn tại",
                    "<html><div style='width:420px'>Đã có " + sameWord.size()
                            + " mục từ khác tên <b>" + TextNormalizer.escapeHtml(word)
                            + "</b>.<br><br>Vẫn lưu thành một bản trùng?</div></html>",
                    "Vẫn lưu");
            if (!confirmed) {
                return;
            }
        }

        long id = editing.getId();
        host.dictionary().update(id, word, definitions);
        if (PanelSupport.save(host, this, "Đã cập nhật slang word “" + word + "”")) {
            runSearch(word);
            selectRowById(id);
            hideError();
        }
    }

    private void selectRowById(long id) {
        for (int row = 0; row < model.getRowCount(); row++) {
            Slang slang = model.getSlangAt(row);
            if (slang != null && slang.getId() == id) {
                table.setRowSelectionInterval(row, row);
                table.scrollRectToVisible(table.getCellRect(row, 0, true));
                return;
            }
        }
    }

    private void setFormEnabled(boolean enabled) {
        wordField.setEnabled(enabled);
        definitionsArea.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        wordField.setEditable(enabled);
        definitionsArea.setEditable(enabled);
        if (!enabled) {
            idChip.setText("Chưa chọn mục từ");
            idChip.setPalette(Theme.ACCENT_SOFT, Theme.ACCENT);
        }
    }

    private void clearForm() {
        editing = null;
        wordField.setText("");
        definitionsArea.setText("");
        setFormEnabled(false);
        hideError();
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

    @Override
    public void onShown() {
        searchField.requestFocusInWindow();
        if (lastKeyword != null && !lastKeyword.isBlank()) {
            runSearch(lastKeyword);
        }
    }

    @Override
    public void onDataChanged() {
        if (lastKeyword != null && !lastKeyword.isBlank()) {
            runSearch(lastKeyword);
        }
    }

    @Override
    public void prepareDemo() {
        searchField.setText("BOMB");
        runSearch("BOMB");
        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }
}
