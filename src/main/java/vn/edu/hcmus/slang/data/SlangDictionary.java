package vn.edu.hcmus.slang.data;

import vn.edu.hcmus.slang.model.DefinitionSuggestion;
import vn.edu.hcmus.slang.model.Slang;
import vn.edu.hcmus.slang.util.TextNormalizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Từ điển slang trong bộ nhớ.
 *
 * <p>Cấu trúc dữ liệu chính:</p>
 * <ul>
 *   <li>{@code byId}: giữ toàn bộ mục từ theo thứ tự chèn (LinkedHashMap).</li>
 *   <li>{@code byWord}: chỉ mục từ khoá chuẩn hoá -&gt; danh sách mục từ.
 *       Cho phép tra cứu chính xác O(1), và vẫn hỗ trợ nhiều mục từ trùng
 *       tên.</li>
 *   <li>{@code definitionIndex}: chỉ mục trigram nghịch đảo cho định nghĩa.</li>
 * </ul>
 */
public final class SlangDictionary {

    private final Map<Long, Slang> byId = new LinkedHashMap<>();
    private final Map<String, List<Slang>> byWord = new HashMap<>();
    private List<Slang> ordered = new ArrayList<>();
    private TrigramIndex definitionIndex = TrigramIndex.empty();
    private boolean wordIndexDirty = true;
    private boolean trigramIndexDirty = true;
    private long nextId = 1;

    private SlangDictionary() {
    }

    public static SlangDictionary fromEntries(Collection<Slang> entries) {
        SlangDictionary dictionary = new SlangDictionary();
        for (Slang entry : entries) {
            dictionary.byId.put(entry.getId(), entry);
            dictionary.nextId = Math.max(dictionary.nextId, entry.getId() + 1);
        }
        dictionary.rebuildIndex();
        return dictionary;
    }

    public static SlangDictionary fromSnapshot(DictionarySnapshot snapshot) {
        SlangDictionary dictionary = new SlangDictionary();
        for (Slang entry : snapshot.entries()) {
            dictionary.byId.put(entry.getId(), entry);
            dictionary.nextId = Math.max(dictionary.nextId, entry.getId() + 1);
        }
        dictionary.ordered = new ArrayList<>(snapshot.entries());
        for (Map.Entry<String, List<Slang>> entry : snapshot.wordIndex().entrySet()) {
            dictionary.byWord.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        dictionary.definitionIndex = snapshot.definitionIndex();
        dictionary.wordIndexDirty = false;
        dictionary.trigramIndexDirty = false;
        return dictionary;
    }

    public synchronized int size() {
        return byId.size();
    }

    public synchronized int definitionCount() {
        int total = 0;
        for (Slang entry : byId.values()) {
            total += entry.getDefinitions().size();
        }
        return total;
    }

    public synchronized int uniqueWordCount() {
        ensureWordIndex();
        return byWord.size();
    }

    public synchronized Slang getById(long id) {
        return byId.get(id);
    }

    /** Tra cứu chính xác theo slang word (không phân biệt hoa thường). */
    public synchronized List<Slang> searchByWord(String keyword) {
        String key = TextNormalizer.normalizeWord(keyword);
        if (key.isEmpty()) {
            return List.of();
        }
        ensureWordIndex();
        List<Slang> found = byWord.get(key);
        if (found == null || found.isEmpty()) {
            return List.of();
        }
        return List.copyOf(found);
    }

    /** Gợi ý các slang word bắt đầu bằng prefix. */
    public synchronized List<String> suggestWords(String prefix, int limit) {
        String key = TextNormalizer.normalizeWord(prefix);
        if (key.isEmpty()) {
            return List.of();
        }
        ensureWordIndex();
        return byWord.keySet().stream()
                .filter(word -> word.startsWith(key))
                .sorted((a, b) -> {
                    int byLength = Integer.compare(a.length(), b.length());
                    return byLength != 0 ? byLength : a.compareTo(b);
                })
                .limit(Math.max(1, limit))
                .toList();
    }

    /**
     * Gợi ý slang word có chứa chuỗi người dùng đang nhập. Dùng cho
     * autocomplete ở chức năng tìm theo slang word; chỉ trả về word, không
     * trộn với gợi ý định nghĩa.
     */
    public synchronized List<Slang> suggestEntries(String keyword, int limit) {
        String query = TextNormalizer.normalizeWord(keyword);
        if (query.isEmpty() || limit <= 0) {
            return List.of();
        }
        ensureWordIndex();
        List<Map.Entry<String, List<Slang>>> matches = new ArrayList<>();
        for (Map.Entry<String, List<Slang>> entry : byWord.entrySet()) {
            if (entry.getKey().contains(query)) {
                matches.add(entry);
            }
        }
        matches.sort(Comparator
                .comparingInt((Map.Entry<String, List<Slang>> entry) -> entry.getKey().indexOf(query))
                .thenComparingInt(entry -> entry.getKey().length())
                .thenComparing(Map.Entry::getKey));
        List<Slang> result = new ArrayList<>();
        for (Map.Entry<String, List<Slang>> match : matches) {
            for (Slang slang : match.getValue()) {
                result.add(slang);
                if (result.size() >= limit) {
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * Gợi ý các định nghĩa có chứa chuỗi người dùng đang nhập. Chỉ trả về
     * definition (kèm word hiển thị), không trộn với danh sách slang word.
     */
    public synchronized List<DefinitionSuggestion> suggestDefinitions(String keyword, int limit) {
        String query = TextNormalizer.normalizeSearchText(keyword);
        if (query.isEmpty() || limit <= 0) {
            return List.of();
        }
        ensureAllIndexes();
        List<Slang> candidates = definitionIndex.search(query, ordered);
        List<DefinitionSuggestion> suggestions = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int collectLimit = Math.max(40, limit * 20);
        for (Slang entry : candidates) {
            for (String definition : entry.getDefinitions()) {
                String normalized = TextNormalizer.normalizeSearchText(definition);
                if (!normalized.contains(query)) {
                    continue;
                }
                String signature = entry.getNormalizedWord() + "\u0000" + normalized;
                if (seen.add(signature)) {
                    suggestions.add(new DefinitionSuggestion(entry.getWord(), definition));
                    if (suggestions.size() >= collectLimit) {
                        break;
                    }
                }
            }
            if (suggestions.size() >= collectLimit) {
                break;
            }
        }
        suggestions.sort(Comparator
                .comparingInt((DefinitionSuggestion item) ->
                        TextNormalizer.normalizeSearchText(item.definition()).indexOf(query))
                .thenComparingInt(item -> item.definition().length())
                .thenComparing(DefinitionSuggestion::word));
        if (suggestions.size() > limit) {
            return new ArrayList<>(suggestions.subList(0, limit));
        }
        return suggestions;
    }

    /** Tìm tất cả mục từ có định nghĩa chứa keyword. */
    public synchronized List<Slang> searchByDefinition(String keyword) {
        String query = TextNormalizer.normalizeSearchText(keyword);
        if (query.isEmpty()) {
            return List.of();
        }
        ensureAllIndexes();
        List<Slang> result = new ArrayList<>(definitionIndex.search(query, ordered));
        result.sort(null);
        return result;
    }

    public synchronized Slang add(String word, List<String> definitions) {
        Slang entry = new Slang(nextId++, word, definitions);
        byId.put(entry.getId(), entry);
        markDirty();
        return entry;
    }

    public synchronized boolean overwrite(long id, List<String> definitions) {
        Slang entry = byId.get(id);
        if (entry == null) {
            return false;
        }
        entry.setDefinitions(definitions);
        markDirty();
        return true;
    }

    public synchronized boolean update(long id, String word, List<String> definitions) {
        Slang entry = byId.get(id);
        if (entry == null) {
            return false;
        }
        entry.setWord(word);
        entry.setDefinitions(definitions);
        markDirty();
        return true;
    }

    public synchronized boolean delete(long id) {
        Slang removed = byId.remove(id);
        if (removed == null) {
            return false;
        }
        markDirty();
        return true;
    }

    /** Thay toàn bộ dữ liệu (dùng cho chức năng reset). */
    public synchronized void replaceAll(Collection<Slang> entries) {
        byId.clear();
        nextId = 1;
        for (Slang entry : entries) {
            byId.put(entry.getId(), entry);
            nextId = Math.max(nextId, entry.getId() + 1);
        }
        markDirty();
        rebuildIndex();
    }

    public synchronized List<Slang> allEntries() {
        ensureWordIndex();
        return List.copyOf(ordered);
    }

    public synchronized Slang randomEntry(Random random) {
        ensureWordIndex();
        if (ordered.isEmpty()) {
            return null;
        }
        return ordered.get(random.nextInt(ordered.size()));
    }

    /** Ảnh chụp đầy đủ để lưu xuống file nhị phân. */
    public synchronized DictionarySnapshot snapshot() {
        ensureAllIndexes();
        Map<String, List<Slang>> wordCopy = new HashMap<>(byWord.size() * 2);
        for (Map.Entry<String, List<Slang>> entry : byWord.entrySet()) {
            wordCopy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return new DictionarySnapshot(List.copyOf(ordered), wordCopy, definitionIndex);
    }

    public synchronized int trigramCount() {
        ensureAllIndexes();
        return definitionIndex.trigramCount();
    }

    /** Xây lại toàn bộ chỉ mục (dùng sau khi dữ liệu thay đổi). */
    public synchronized void rebuildIndex() {
        ordered = new ArrayList<>(byId.values());
        byWord.clear();
        for (Slang entry : ordered) {
            byWord.computeIfAbsent(entry.getNormalizedWord(), key -> new ArrayList<>()).add(entry);
        }
        definitionIndex = TrigramIndex.build(ordered);
        wordIndexDirty = false;
        trigramIndexDirty = false;
    }

    void ensureAllIndexes() {
        ensureWordIndex();
        if (trigramIndexDirty) {
            definitionIndex = TrigramIndex.build(ordered);
            trigramIndexDirty = false;
        }
    }

    private void ensureWordIndex() {
        if (!wordIndexDirty) {
            return;
        }
        ordered = new ArrayList<>(byId.values());
        byWord.clear();
        for (Slang entry : ordered) {
            byWord.computeIfAbsent(entry.getNormalizedWord(), key -> new ArrayList<>()).add(entry);
        }
        wordIndexDirty = false;
    }

    private void markDirty() {
        wordIndexDirty = true;
        trigramIndexDirty = true;
    }

    /** Chỉ dùng cho kiểm thử: kiểm tra tính nhất quán của chỉ mục. */
    public synchronized void validateIndexes() {
        ensureAllIndexes();
        Objects.requireNonNull(definitionIndex);
        if (ordered.size() != byId.size()) {
            throw new IllegalStateException("Số mục từ không khớp");
        }
        int words = 0;
        for (List<Slang> list : byWord.values()) {
            words += list.size();
        }
        if (words != byId.size()) {
            throw new IllegalStateException("Chỉ mục từ khoá không khớp");
        }
    }

    public synchronized List<Slang> immutableEntries() {
        return Collections.unmodifiableList(ordered);
    }
}
