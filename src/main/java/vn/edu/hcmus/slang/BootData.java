package vn.edu.hcmus.slang;

import vn.edu.hcmus.slang.data.DictionaryStore;
import vn.edu.hcmus.slang.data.SlangDictionary;
import vn.edu.hcmus.slang.service.HistoryService;

/** Kết quả nạp dữ liệu lúc khởi động. */
public record BootData(
        SlangDictionary dictionary,
        DictionaryStore store,
        HistoryService history,
        DictionaryStore.LoadReport loadReport) {
}
