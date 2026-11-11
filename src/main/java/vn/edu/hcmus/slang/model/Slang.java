package vn.edu.hcmus.slang.model;

import vn.edu.hcmus.slang.util.TextNormalizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Một mục từ (slang word) cùng một hoặc nhiều định nghĩa.
 *
 * <p>Hai mục từ có cùng {@link #getId() id} được xem là bằng nhau. Một slang
 * word có thể xuất hiện nhiều lần trong từ điển (bản ghi trùng), vì vậy không
 * dùng word làm khoá duy nhất.</p>
 */
public final class Slang implements Comparable<Slang> {

    private final long id;
    private String word;
    private String normalizedWord;
    private final List<String> definitions;
    private String searchText;
    private final long createdAt;
    private long updatedAt;

    public Slang(long id, String word, List<String> definitions) {
        this(id, word, definitions, System.currentTimeMillis(), System.currentTimeMillis());
    }

    public Slang(long id, String word, List<String> definitions, long createdAt, long updatedAt) {
        String cleanedWord = TextNormalizer.cleanWord(word);
        if (cleanedWord.isEmpty()) {
            throw new IllegalArgumentException("Slang word không được để trống");
        }
        List<String> cleaned = new ArrayList<>();
        if (definitions != null) {
            for (String definition : definitions) {
                String value = TextNormalizer.cleanDefinition(definition);
                if (!value.isEmpty()) {
                    cleaned.add(value);
                }
            }
        }
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Định nghĩa không được để trống");
        }
        this.id = id;
        this.word = cleanedWord;
        this.normalizedWord = TextNormalizer.normalizeWord(cleanedWord);
        this.definitions = cleaned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        refreshSearchText();
    }

    public static Slang create(long id, String word, String rawDefinitions) {
        return new Slang(id, word, TextNormalizer.splitDefinitions(rawDefinitions));
    }

    public long getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public String getNormalizedWord() {
        return normalizedWord;
    }

    public void setWord(String newWord) {
        String cleaned = TextNormalizer.cleanWord(newWord);
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Slang word không được để trống");
        }
        this.word = cleaned;
        this.normalizedWord = TextNormalizer.normalizeWord(cleaned);
        this.updatedAt = System.currentTimeMillis();
    }

    public List<String> getDefinitions() {
        return Collections.unmodifiableList(definitions);
    }

    public void setDefinitions(List<String> newDefinitions) {
        List<String> cleaned = new ArrayList<>();
        if (newDefinitions != null) {
            for (String definition : newDefinitions) {
                String value = TextNormalizer.cleanDefinition(definition);
                if (!value.isEmpty()) {
                    cleaned.add(value);
                }
            }
        }
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Định nghĩa không được để trống");
        }
        definitions.clear();
        definitions.addAll(cleaned);
        this.updatedAt = System.currentTimeMillis();
        refreshSearchText();
    }

    /** Dùng bởi bộ đọc file khi gặp dòng định nghĩa nối tiếp. */
    public void addDefinition(String definition) {
        String value = TextNormalizer.cleanDefinition(definition);
        if (value.isEmpty()) {
            return;
        }
        definitions.add(value);
        this.updatedAt = System.currentTimeMillis();
        refreshSearchText();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    /** Toàn bộ định nghĩa trên nhiều dòng (dùng để hiển thị). */
    public String getDefinitionText() {
        return String.join("\n", definitions);
    }

    /** Toàn bộ định nghĩa trên một dòng, phân tách bằng dấu |. */
    public String getJoinedDefinitions() {
        return String.join(" | ", definitions);
    }

    /** Văn bản đã chuẩn hoá dùng cho chỉ mục định nghĩa. */
    public String getSearchText() {
        return searchText;
    }

    private void refreshSearchText() {
        this.searchText = TextNormalizer.normalizeSearchText(String.join(" ", definitions));
    }

    @Override
    public int compareTo(Slang other) {
        int byWord = normalizedWord.compareTo(other.normalizedWord);
        if (byWord != 0) {
            return byWord;
        }
        return Long.compare(id, other.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Slang other)) {
            return false;
        }
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return word + "`" + String.join("|", definitions);
    }
}
