package vn.edu.hcmus.slang.util;

/** Tạo đoạn trích ngắn quanh vị trí xuất hiện của từ khoá. */
public final class Snippet {

    private Snippet() {
    }

    public static String of(String text, String keyword, int maxLength) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String flat = text.replaceAll("\\s+", " ").trim();
        int max = Math.max(24, maxLength);
        int index = indexOfIgnoreCase(flat, keyword);
        if (index < 0) {
            return flat.length() <= max ? flat : flat.substring(0, max - 1) + "…";
        }
        int context = Math.max(0, (max - keyword.length()) / 2);
        int start = Math.max(0, index - context);
        int end = Math.min(flat.length(), start + max);
        if (end - start < max) {
            start = Math.max(0, end - max);
        }
        String result = flat.substring(start, end);
        if (start > 0) {
            result = "…" + result;
        }
        if (end < flat.length()) {
            result = result + "…";
        }
        return result;
    }

    public static int indexOfIgnoreCase(String text, String keyword) {
        if (text == null || keyword == null || keyword.isEmpty()) {
            return -1;
        }
        return text.toLowerCase(java.util.Locale.ROOT)
                .indexOf(keyword.toLowerCase(java.util.Locale.ROOT));
    }
}
