package vn.edu.hcmus.slang.ui.component;

import vn.edu.hcmus.slang.model.Slang;
import vn.edu.hcmus.slang.util.Snippet;
import vn.edu.hcmus.slang.util.TextNormalizer;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Model bảng 2 cột: slang word + định nghĩa (hoặc trích đoạn). */
public final class SlangTableModel extends AbstractTableModel {

    private final String[] columns = {"Slang word", "Định nghĩa"};
    private List<Slang> rows = new ArrayList<>();
    private String keyword = "";
    private boolean snippet;

    public void setRows(List<Slang> entries, String keyword, boolean snippet) {
        this.rows = new ArrayList<>(entries);
        this.keyword = keyword == null ? "" : keyword;
        this.snippet = snippet;
        fireTableDataChanged();
    }

    public void clear() {
        setRows(List.of(), "", false);
    }

    public List<Slang> getRows() {
        return List.copyOf(rows);
    }

    public Slang getSlangAt(int row) {
        if (row < 0 || row >= rows.size()) {
            return null;
        }
        return rows.get(row);
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean isSnippet() {
        return snippet;
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
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Slang slang = rows.get(rowIndex);
        if (columnIndex == 0) {
            return slang.getWord();
        }
        if (snippet) {
            return Snippet.of(slang.getDefinitionText(), keyword, 210);
        }
        return slang.getJoinedDefinitions();
    }

    /** Dùng cho renderer highlight. */
    public String highlightInnerHtml(String text) {
        return TextNormalizer.escapeHtml(text);
    }
}
