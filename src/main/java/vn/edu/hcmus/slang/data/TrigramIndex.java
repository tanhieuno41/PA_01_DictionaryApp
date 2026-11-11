package vn.edu.hcmus.slang.data;

import vn.edu.hcmus.slang.model.Slang;
import vn.edu.hcmus.slang.util.TextNormalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Chỉ mục nghịch đảo theo trigram cho phần định nghĩa.
 *
 * <p>Mỗi trigram (3 ký tự liên tiếp sau khi chuẩn hoá) ánh xạ tới danh sách
 * chỉ số các mục từ có chứa trigram đó. Truy vấn "definition chứa keyword"
 * được xử lý bằng giao các posting list rồi kiểm tra lại bằng
 * {@link String#contains(CharSequence)} để loại false-positive.</p>
 */
public final class TrigramIndex {

    private static final int MIN_TRIGRAM_QUERY = 3;

    private final Map<String, int[]> postings;

    private TrigramIndex(Map<String, int[]> postings) {
        this.postings = postings;
    }

    public static TrigramIndex empty() {
        return new TrigramIndex(new HashMap<>());
    }

    public static TrigramIndex build(List<Slang> entries) {
        Map<String, IntVector> building = new HashMap<>(Math.max(16, entries.size() * 2));
        Set<String> seenInDocument = new HashSet<>();

        for (int index = 0; index < entries.size(); index++) {
            String text = entries.get(index).getSearchText();
            seenInDocument.clear();
            for (int i = 0; i + MIN_TRIGRAM_QUERY <= text.length(); i++) {
                String trigram = text.substring(i, i + MIN_TRIGRAM_QUERY);
                if (seenInDocument.add(trigram)) {
                    building.computeIfAbsent(trigram, key -> new IntVector()).add(index);
                }
            }
        }

        Map<String, int[]> result = new HashMap<>(building.size() * 2);
        for (Map.Entry<String, IntVector> entry : building.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toSortedUniqueArray());
        }
        return new TrigramIndex(result);
    }

    public static TrigramIndex fromPostings(Map<String, int[]> postings) {
        return new TrigramIndex(new HashMap<>(postings));
    }

    /** Bản sao chỉ đọc của posting list, phục vụ lưu xuống file. */
    public Map<String, int[]> postings() {
        return Collections.unmodifiableMap(postings);
    }

    public int trigramCount() {
        return postings.size();
    }

    /**
     * Tìm các mục từ có định nghĩa chứa {@code keyword}.
     *
     * @param keyword từ khoá (không phân biệt hoa thường)
     * @param entries danh sách mục từ theo đúng thứ tự đã dùng khi build
     */
    public List<Slang> search(String keyword, List<Slang> entries) {
        String query = TextNormalizer.normalizeSearchText(keyword);
        if (query.isEmpty() || entries.isEmpty()) {
            return List.of();
        }

        List<Slang> result = new ArrayList<>();
        if (query.length() < MIN_TRIGRAM_QUERY) {
            for (Slang entry : entries) {
                if (entry.matchesDefinition(query)) {
                    result.add(entry);
                }
            }
            result.sort(null);
            return result;
        }

        int[] smallest = null;
        List<int[]> others = new ArrayList<>(query.length());
        for (int i = 0; i + MIN_TRIGRAM_QUERY <= query.length(); i++) {
            String trigram = query.substring(i, i + MIN_TRIGRAM_QUERY);
            int[] posting = postings.get(trigram);
            if (posting == null) {
                return List.of();
            }
            if (smallest == null || posting.length < smallest.length) {
                if (smallest != null) {
                    others.add(smallest);
                }
                smallest = posting;
            } else {
                others.add(posting);
            }
        }
        if (smallest == null) {
            return List.of();
        }

        for (int candidate : smallest) {
            if (candidate < 0 || candidate >= entries.size()) {
                continue;
            }
            boolean inAll = true;
            for (int[] other : others) {
                if (Arrays.binarySearch(other, candidate) < 0) {
                    inAll = false;
                    break;
                }
            }
            if (inAll) {
                Slang entry = entries.get(candidate);
                if (entry.matchesDefinition(query)) {
                    result.add(entry);
                }
            }
        }
        result.sort(null);
        return result;
    }
}
