package vn.edu.hcmus.slang.ui.panel;

import vn.edu.hcmus.slang.model.Slang;
import vn.edu.hcmus.slang.ui.AppHost;
import vn.edu.hcmus.slang.ui.DemoScreen;
import vn.edu.hcmus.slang.ui.Dialogs;
import vn.edu.hcmus.slang.ui.Refreshable;
import vn.edu.hcmus.slang.ui.Theme;
import vn.edu.hcmus.slang.ui.Ui;
import vn.edu.hcmus.slang.ui.component.EmptyState;
import vn.edu.hcmus.slang.ui.component.FlatButton;
import vn.edu.hcmus.slang.ui.component.RoundedPanel;
import vn.edu.hcmus.slang.ui.component.RoundedTextField;
import vn.edu.hcmus.slang.ui.component.SlangTableModel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/** Chức năng 6: xóa slang word, luôn xác nhận trước khi xóa. */
public final class DeletePanel extends JPanel implements Refreshable, DemoScreen {

    private final AppHost host;
    private final RoundedTextField searchField = new RoundedTextField("Nhập slang word cần xóa", 24);
    private final SlangTableModel model = new SlangTableModel();
    private final JTable table = new JTable(model);
    private final JLabel resultLabel = Ui.label("Tìm slang word để bắt đầu", 13, Font.BOLD, Theme.TEXT);
    private final JLabel selectedLabel = Ui.label("Chưa chọn mục từ", 12, Font.BOLD, Theme.MUTED);
    private final FlatButton deleteButton = new FlatButton("Xóa slang word", FlatButton.Variant.DANGER);
    private final EmptyState emptyState = new EmptyState("Chưa có mục từ nào được chọn",
            "Nhập slang word, chọn đúng dòng cần xóa. Ứng dụng sẽ hỏi xác nhận trước khi xóa.");
    private final CardLayout resultCards = new CardLayout();
    private final JPanel resultBody = new JPanel(resultCards);
    private String lastKeyword;
    private Slang selected;

    public DeletePanel(AppHost host) {
        super(new BorderLayout());
        this.host = host;
        setBackground(Theme.BG);
        deleteButton.setEnabled(false);
        add(Ui.page("Xóa slang word",
                "Chức năng 6 · Xóa một mục từ cụ thể, có bước xác nhận an toàn",
                buildBody()), BorderLayout.CENTER);
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

        RoundedPanel resultCard = Ui.card(16);
        resultCard.setLayout(new BorderLayout(0, 10));
        resultCard.add(resultLabel, BorderLayout.NORTH);

        Ui.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(0).setMaxWidth(300);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::selectionChanged);

        resultBody.setOpaque(false);
        resultBody.add(emptyState, "empty");
        resultBody.add(Ui.tableScroll(table), "table");
        resultCard.add(resultBody, BorderLayout.CENTER);

        RoundedPanel actionBar = Ui.card(14);
        actionBar.setLayout(new BorderLayout());
        actionBar.setPreferredSize(new Dimension(0, 64));
        selectedLabel.setForeground(Theme.MUTED);
        actionBar.add(selectedLabel, BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout(12, 0));
        right.setOpaque(false);
        JLabel note = Ui.label("Không thể hoàn tác sau khi xóa.", 11, Font.PLAIN, Theme.FAINT);
        right.add(note, BorderLayout.WEST);
        deleteButton.addActionListener(e -> deleteSelected());
        right.add(deleteButton, BorderLayout.EAST);
        actionBar.add(right, BorderLayout.EAST);

        body.add(searchCard, BorderLayout.NORTH);
        body.add(resultCard, BorderLayout.CENTER);
        body.add(actionBar, BorderLayout.SOUTH);
        return body;
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
        selected = null;
        updateSelection();
        if (results.isEmpty()) {
            emptyState.setContent("Không tìm thấy “" + query + "”",
                    "Kiểm tra lại chính tả, hoặc tìm theo định nghĩa để xác định đúng slang word.");
            resultCards.show(resultBody, "empty");
            resultLabel.setText("Không tìm thấy kết quả");
        } else {
            resultCards.show(resultBody, "table");
            resultLabel.setText("Tìm thấy " + results.size() + " mục từ — chọn một dòng để xóa");
        }
    }

    private void selectionChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        int row = table.getSelectedRow();
        selected = row < 0 ? null : model.getSlangAt(table.convertRowIndexToModel(row));
        updateSelection();
    }

    private void updateSelection() {
        if (selected == null) {
            selectedLabel.setText("Chưa chọn mục từ");
            selectedLabel.setForeground(Theme.MUTED);
            deleteButton.setEnabled(false);
        } else {
            selectedLabel.setText("Đang chọn: " + selected.getWord() + "  ·  ID #" + selected.getId());
            selectedLabel.setForeground(Theme.TEXT);
            deleteButton.setEnabled(true);
        }
    }

    private void deleteSelected() {
        if (selected == null) {
            return;
        }
        Slang target = selected;
        if (!Dialogs.confirmDelete(this, target)) {
            return;
        }
        host.dictionary().delete(target.getId());
        if (PanelSupport.save(host, this, "Đã xóa slang word “" + target.getWord() + "”")) {
            if (lastKeyword != null) {
                runSearch(lastKeyword);
            }
        }
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
