package vn.edu.hcmus.slang.data;

import vn.edu.hcmus.slang.model.Slang;

import java.util.List;
import java.util.Map;

/** Ảnh chụp toàn bộ cấu trúc dữ liệu đã đánh chỉ mục, dùng cho lưu / nạp. */
public record DictionarySnapshot(
        List<Slang> entries,
        Map<String, List<Slang>> wordIndex,
        TrigramIndex definitionIndex) {
}
