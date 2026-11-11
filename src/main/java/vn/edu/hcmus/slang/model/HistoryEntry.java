package vn.edu.hcmus.slang.model;

/**
 * Một lần tra cứu được lưu vào lịch sử.
 *
 * @param timestamp  thời điểm tra cứu (epoch millis)
 * @param type       loại tra cứu
 * @param keyword    từ khoá người dùng nhập
 * @param resultCount số kết quả tìm được
 */
public record HistoryEntry(long timestamp, SearchType type, String keyword, int resultCount) {

    public enum SearchType {
        WORD("Slang word"),
        DEFINITION("Định nghĩa");

        private final String label;

        SearchType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public HistoryEntry {
        if (keyword == null) {
            keyword = "";
        }
    }
}
