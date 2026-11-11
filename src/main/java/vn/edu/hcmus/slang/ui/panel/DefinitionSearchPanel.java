package vn.edu.hcmus.slang.ui.panel;

import vn.edu.hcmus.slang.model.DefinitionSuggestion;
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
import vn.edu.hcmus.slang.ui.component.SuggestionPanel;
import vn.edu.hcmus.slang.util.Snippet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** Chức năng 2: tìm các slang word có định nghĩa chứa keyword. */
public final class DefinitionSearchPanel extends JPanel implements Refreshable, DemoScreen {

    private final AppHost host;
    private final RoundedTextField field = new RoundedTextField("Nhập một cụm từ trong định nghĩa, ví dụ: money", 24);
    private final SlangTableModel model = new SlangTableModel();
    private final JTable table = new JTable(model);
    private final JLabel resultLabel = Ui.label("Sẵn sàng tra cứu", 14, Font.BOLD, Theme.TEXT);
    private final JLabel timeLabel = Ui.label("", 11, Font.PLAIN, Theme.FAINT);
    private final EmptyState emptyState = new EmptyState("Tìm theo định nghĩa",
            "Nhập một chuỗi con của định nghĩa. Ứng dụng dùng chỉ mục trigram nên vẫn nhanh với 100.000 slang word.");
    private final CardLayout resultCards = new CardLayout();
    private final JPanel resultBody = new JPanel(resultCards);
    private final SuggestionPanel suggestions = new SuggestionPanel("Gợi ý định nghĩa");
    private final Timer suggestionTimer = new Timer(180, e -> refreshSuggestions());
    private String lastKeyword;

    public DefinitionSearchPanel(AppHost host) {
        super(new BorderLayout());
        this.host = host;
        setBackground(Theme.BG);
        suggestionTimer.setRepeats(false);
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleSuggestions();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleSuggestions();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleSuggestions();
            }
        });
        add(Ui.page("Tìm theo định nghĩa",
                "Chức năng 2 · Liệt kê tất cả slang word có định nghĩa chứa từ khoá (tìm chuỗi con)",
                buildBody()), BorderLayout.CENTER);
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);

        RoundedPanel searchCard = Ui.card(14);
        searchCard.setLayout(new BorderLayout(10, 8));
        FlatButton button = new FlatButton("Tìm kiếm", FlatButton.Variant.PRIMARY);
        button.addActionListener(e -> runSearch(field.getText(), true));
        field.addActionListener(e -> runSearch(field.getText(), true));
        searchCard.add(field, BorderLayout.CENTER);
        searchCard.add(button, BorderLayout.EAST);
        searchCard.add(suggestions, BorderLayout.SOUTH);

        RoundedPanel resultCard = Ui.card(16);
        resultCard.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(resultLabel, BorderLayout.WEST);
        header.add(timeLabel, BorderLayout.EAST);
        resultCard.add(header, BorderLayout.NORTH);

        Ui.styleTable(table);
        table.setRowHeight(52);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(0).setMaxWidth(300);
        table.getColumnModel().getColumn(1).setCellRenderer(new DefinitionRenderer(model));
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    Slang slang = model.getSlangAt(table.convertRowIndexToModel(row));
                    if (slang != null) {
                        Dialogs.showEntry(DefinitionSearchPanel.this, slang);
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
        suggestions.clear();
        suggestionTimer.stop();
        String query = keyword == null ? "" : keyword.trim();
        if (query.isEmpty()) {
            resultLabel.setText("Vui lòng nhập từ khoá");
            field.requestFocusInWindow();
            return;
        }
        long start = System.nanoTime();
        List<Slang> results = host.dictionary().searchByDefinition(query);
        long millis = Math.max(0, (System.nanoTime() - start) / 1_000_000);
        lastKeyword = query;
        model.setRows(results, query, true);
        table.clearSelection();

        if (results.isEmpty()) {
            emptyState.setContent("Không có định nghĩa nào chứa “" + query + "”",
                    "Thử một từ khoá ngắn hơn, hoặc kiểm tra lại chính tả. Tìm kiếm không phân biệt hoa thường.");
            resultCards.show(resultBody, "empty");
            resultLabel.setText("Không tìm thấy kết quả");
        } else {
            resultCards.show(resultBody, "table");
            resultLabel.setText("Tìm thấy " + results.size() + " slang word chứa “" + query + "”");
        }
        timeLabel.setText(millis + " ms");

        if (recordHistory) {
            host.history().record(HistoryEntry.SearchType.DEFINITION, query, results.size());
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

    private void scheduleSuggestions() {
        suggestionTimer.restart();
    }

    private void refreshSuggestions() {
        String query = field.getText().trim();
        if (query.isEmpty()) {
            suggestions.clear();
            return;
        }
        List<DefinitionSuggestion> matches = host.dictionary().suggestDefinitions(query, 6);
        List<SuggestionPanel.Item> items = new ArrayList<>();
        for (DefinitionSuggestion match : matches) {
            items.add(new SuggestionPanel.Item(
                    Dialogs.shorten(match.definition(), 130),
                    "→ " + match.word(),
                    false,
                    () -> chooseSuggestion(match.definition())));
        }
        suggestions.setItems("Gợi ý định nghĩa", items);
    }

    private void chooseSuggestion(String definition) {
        suggestionTimer.stop();
        field.setText(definition);
        suggestions.clear();
        runSearch(definition, true);
    }

    @Override
    public void prepareDemo() {
        field.setText("money");
        runSearch("money", true);
    }

    /** Dùng cho ảnh chụp: chỉ hiện gợi ý định nghĩa, chưa chạy tìm kiếm. */
    public void prepareSuggestionDemo() {
        field.setText("money");
        suggestionTimer.stop();
        refreshSuggestions();
    }

    /** Renderer highlight từ khoá trong đoạn trích. */
    private static final class DefinitionRenderer extends DefaultTableCellRenderer {

        private final SlangTableModel model;

        DefinitionRenderer(SlangTableModel model) {
            this.model = model;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            int width = Math.max(220, table.getColumnModel().getColumn(column).getWidth() - 28);
            String text = value == null ? "" : value.toString();
            String html = "<html><div style='width:" + width + "px'>"
                    + Ui.highlightHtml(text, model.getKeyword()) + "</div></html>";
            super.getTableCellRendererComponent(table, html, isSelected, false, row, column);
            setFont(Theme.font(13, Font.PLAIN));
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            setBackground(isSelected ? Theme.ACCENT_SOFT
                    : (row % 2 == 0 ? Theme.CARD : new Color(0xFAFAFA)));
            setForeground(Theme.TEXT);
            Slang slang = model.getSlangAt(row);
            setToolTipText(slang == null ? null : slang.getJoinedDefinitions());
            return this;
        }
    }
}
