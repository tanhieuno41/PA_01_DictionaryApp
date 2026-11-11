package vn.edu.hcmus.slang.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Chuẩn hoá chuỗi dùng cho việc tìm kiếm và lưu trữ.
 *
 * <p>Quy ước dữ liệu của ứng dụng: mỗi slang word nằm trên một dòng, phần định
 * nghĩa đứng sau dấu ` (backtick), nhiều định nghĩa phân tách bằng dấu |.</p>
 */
public final class TextNormalizer {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern DEFINITION_SEPARATOR = Pattern.compile("\\r?\\n|\\|");

    private TextNormalizer() {
    }

    /** Chuẩn hoá khoá tra cứu: NFC, gộp khoảng trắng, chữ thường. */
    public static String normalizeWord(String value) {
        if (value == null) {
            return "";
        }
        String text = Normalizer.normalize(value, Normalizer.Form.NFC);
        text = WHITESPACE.matcher(text).replaceAll(" ").trim();
        return text.toLowerCase(Locale.ROOT);
    }

    /**
     * Chuẩn hoá văn bản để đánh chỉ mục / so khớp. Không dùng cho hiển thị.
     */
    public static String normalizeSearchText(String value) {
        return normalizeWord(value);
    }

    /** Làm sạch một slang word trước khi lưu / hiển thị. */
    public static String cleanWord(String value) {
        if (value == null) {
            return "";
        }
        String text = Normalizer.normalize(value, Normalizer.Form.NFC);
        text = WHITESPACE.matcher(text).replaceAll(" ").trim();
        // Key có thể chứa ký tự | (ví dụ |O|, :|); chỉ backtick là dấu phân
        // tách key/meaning nên không được phép xuất hiện trong key.
        return text.replace('`', '\'');
    }

    /** Làm sạch một định nghĩa đơn lẻ. */
    public static String cleanDefinition(String value) {
        if (value == null) {
            return "";
        }
        String text = Normalizer.normalize(value, Normalizer.Form.NFC);
        text = WHITESPACE.matcher(text).replaceAll(" ").trim();
        // Backtick và pipe là ký tự phân tách của định dạng file.
        return text.replace('`', '\'').replace('|', '/');
    }

    /**
     * Tách định nghĩa do người dùng nhập: mỗi dòng hoặc mỗi dấu | là một
     * định nghĩa riêng.
     */
    public static List<String> splitDefinitions(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return result;
        }
        String[] parts = DEFINITION_SEPARATOR.split(raw);
        for (String part : parts) {
            String cleaned = cleanDefinition(part);
            if (!cleaned.isEmpty()) {
                result.add(cleaned);
            }
        }
        return result;
    }

    /** Tách ô định nghĩa trong file dữ liệu (phân tách bằng dấu |). */
    public static List<String> splitDefinitionCell(String cell) {
        List<String> result = new ArrayList<>();
        if (cell == null || cell.isEmpty()) {
            return result;
        }
        for (String part : cell.split("\\|", -1)) {
            String cleaned = cleanDefinition(part);
            if (!cleaned.isEmpty()) {
                result.add(cleaned);
            }
        }
        return result;
    }

    /** Tạo chuỗi HTML an toàn cho JLabel. */
    public static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
