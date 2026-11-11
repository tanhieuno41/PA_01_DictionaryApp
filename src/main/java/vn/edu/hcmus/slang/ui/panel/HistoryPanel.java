package vn.edu.hcmus.slang.ui.panel;

import vn.edu.hcmus.slang.model.HistoryEntry;
import vn.edu.hcmus.slang.service.HistoryService;
import vn.edu.hcmus.slang.ui.AppHost;
import vn.edu.hcmus.slang.ui.DemoScreen;
import vn.edu.hcmus.slang.ui.Refreshable;
import vn.edu.hcmus.slang.ui.Theme;
import vn.edu.hcmus.slang.ui.Ui;
import vn.edu.hcmus.slang.ui.component.EmptyState;
import vn.edu.hcmus.slang.ui.component.FlatButton;
import vn.edu.hcmus.slang.ui.component.RoundedPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** Chức năng 3: lịch sử các slang word / từ khoá đã tìm. */
public final class HistoryPanel extends JPanel implements Refreshable, DemoScreen {

    private final AppHost host;
    private final HistoryTableModel model = new HistoryTableModel();
    private final JTable table = new JTable(model);
    private final JLabel countLabel = Ui.label("0 lượt tra cứu", 13, Font.BOLD, Theme.TEXT);
    private final EmptyState emptyState = new EmptyState("Chưa có lịch sử",
            "Mỗi lần tra cứu theo slang word hoặc theo định nghĩa sẽ được lưu tự động vào đây.");
    private final CardLayout cards = new CardLayout();
    private final JPanel bodyCards = new JPanel(cards);

    public HistoryPanel(AppHost host) {
        super(new BorderLayout());
        this.host = host;
        setBackground(Theme.BG);
        add(Ui.page("Lịch sử tra cứu",
                "Chức năng 3 · Danh sách các từ khoá đã tìm, mới nhất ở trên cùng",
                buildBody()), BorderLayout.CENTER);
    }

    private JPanel buildBody() {
        RoundedPanel card = Ui.card(16);
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(countLabel, BorderLayout.WEST);
        FlatButton clear = new FlatButton("Xóa lịch sử", FlatButton.Variant.GHOST, 36);
        clear.addActionListener(e -> clearHistory());
        header.add(clear, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        Ui.styleTable(table);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(320);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setMaxWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setMaxWidth(110);

        bodyCards.setOpaque(false);
        bodyCards.add(emptyState, "empty");
        bodyCards.add(Ui.tableScroll(table), "table");
        card.add(bodyCards, BorderLayout.CENTER);
        return card;
    }

    private void clearHistory() {
        if (host.history().size() == 0) {
            return;
        }
        boolean confirmed = vn.edu.hcmus.slang.ui.Dialogs.confirm(this, "Xóa lịch sử",
                "<html><div style='width:380px'>Xóa toàn bộ lịch sử tra cứu?<br><br>"
                        + "<span style='color:#64748B'>Thao tác này không ảnh hưởng tới dữ liệu slang word.</span>"
                        + "</div></html>", "Xóa lịch sử");
        if (!confirmed) {
            return;
        }
        host.history().clear();
        refresh();
        host.setStatus("Đã xóa lịch sử tra cứu");
    }

    @Override
    public void onShown() {
        refresh();
    }

    @Override
    public void onDataChanged() {
        refresh();
    }

    private void refresh() {
        List<HistoryEntry> entries = host.history().entries();
        model.setRows(entries);
        countLabel.setText(Ui.formatInt(entries.size()) + " lượt tra cứu");
        cards.show(bodyCards, entries.isEmpty() ? "empty" : "table");
    }

    @Override
    public void prepareDemo() {
        refresh();
    }

    private static final class HistoryTableModel extends AbstractTableModel {

        private final String[] columns = {"#", "Từ khoá", "Loại", "Kết quả", "Thời gian"};
        private List<HistoryEntry> rows = new ArrayList<>();

        void setRows(List<HistoryEntry> entries) {
            rows = new ArrayList<>(entries);
            fireTableDataChanged();
        }

        HistoryEntry getAt(int row) {
            return rows.get(row);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            HistoryEntry entry = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> entry.keyword();
                case 2 -> entry.type().getLabel();
                case 3 -> entry.resultCount();
                case 4 -> HistoryService.formatTimestamp(entry.timestamp());
                default -> "";
            };
        }
    }
}
