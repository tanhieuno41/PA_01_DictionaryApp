package vn.edu.hcmus.slang.ui.panel;

import vn.edu.hcmus.slang.model.HistoryEntry;
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
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/** Chức năng 1: tìm kiếm chính xác theo slang word. */
public final class SearchWordPanel extends JPanel implements Refreshable, DemoScreen {

    private final AppHost host;
    private final RoundedTextField field = new RoundedTextField("Nhập slang word, ví dụ: BOMB", 24);
    private final SlangTableModel model = new SlangTableModel();
    private final JTable table = new JTable(model);
    private final JLabel resultLabel = Ui.label("Sẵn sàng tra cứu", 14, Font.BOLD, Theme.TEXT);
    private final JLabel timeLabel = Ui.label("", 11, Font.PLAIN, Theme.FAINT);
    private final EmptyState emptyState = new EmptyState("Bắt đầu tra cứu",
            "Nhập một slang word và nhấn Tìm kiếm. Kết quả hiển thị đầy đủ các định nghĩa.");
    private final CardLayout resultCards = new CardLayout();
    private final JPanel resultBody = new JPanel(resultCards);
    private String lastKeyword;

    public SearchWordPanel(AppHost host) {
        super(new BorderLayout());
        this.host = host;
        setBackground(Theme.BG);
        add(Ui.page("Tra cứu slang word", "Chức năng 1 · Tìm chính xác theo từ khoá, không phân biệt hoa thường",
                buildBody()), BorderLayout.CENTER);
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);

        RoundedPanel searchCard = Ui.card(14);
        searchCard.setLayout(new BorderLayout(10, 0));
        FlatButton button = new FlatButton("Tìm kiếm", FlatButton.Variant.PRIMARY);
        button.addActionListener(e -> runSearch(field.getText(), true));
        field.addActionListener(e -> runSearch(field.getText(), true));
        searchCard.add(field, BorderLayout.CENTER);
        searchCard.add(button, BorderLayout.EAST);

        RoundedPanel resultCard = Ui.card(16);
        resultCard.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(resultLabel, BorderLayout.WEST);
        header.add(timeLabel, BorderLayout.EAST);
        resultCard.add(header, BorderLayout.NORTH);

        Ui.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(230);
        table.getColumnModel().getColumn(0).setMaxWidth(320);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    Slang slang = model.getSlangAt(table.convertRowIndexToModel(row));
                    if (slang != null) {
                        Dialogs.showEntry(SearchWordPanel.this, slang);
                    }
                }
            }
        });

        resultBody.setOpaque(false);
        resultBody.add(emptyState, "empty");
        resultBody.add(Ui.tableScroll(table), "table");
        resultCard.add(resultBody, BorderLayout.CENTER);

        body.add(searchCard, BorderLayout.NORTH);
        body.add(resultCard, BorderLayout.CENTER);
        return body;
    }

    private void runSearch(String keyword, boolean recordHistory) {
        String query = keyword == null ? "" : keyword.trim();
        if (query.isEmpty()) {
            resultLabel.setText("Vui lòng nhập slang word");
            field.requestFocusInWindow();
            return;
        }
        long start = System.nanoTime();
        List<Slang> results = host.dictionary().searchByWord(query);
        long millis = Math.max(0, (System.nanoTime() - start) / 1_000_000);
        lastKeyword = query;
        model.setRows(results, query, false);
        table.clearSelection();

        if (results.isEmpty()) {
            List<String> suggestions = host.dictionary().suggestWords(query, 5);
            String hint = "Không có mục từ nào khớp chính xác. Hãy thử tìm theo định nghĩa.";
            if (!suggestions.isEmpty()) {
                hint = "Có phải ý bạn: " + String.join(", ", suggestions) + "?";
            }
            emptyState.setContent("Không tìm thấy “" + query + "”", hint);
            resultCards.show(resultBody, "empty");
            resultLabel.setText("Không tìm thấy kết quả");
        } else {
            resultCards.show(resultBody, "table");
            resultLabel.setText("Tìm thấy " + results.size() + " kết quả cho “" + query + "”");
        }
        timeLabel.setText(millis + " ms");

        if (recordHistory) {
            host.history().record(HistoryEntry.SearchType.WORD, query, results.size());
        }
    }

    @Override
    public void onShown() {
        field.requestFocusInWindow();
        if (lastKeyword != null && !lastKeyword.isBlank()) {
            runSearch(lastKeyword, false);
        }
    }

    @Override
    public void onDataChanged() {
        if (lastKeyword != null && !lastKeyword.isBlank()) {
            runSearch(lastKeyword, false);
        }
    }

    @Override
    public void prepareDemo() {
        field.setText("BOMB");
        runSearch("BOMB", true);
    }
}
