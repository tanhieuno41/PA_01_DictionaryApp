package vn.edu.hcmus.slang.ui;

import vn.edu.hcmus.slang.data.DictionaryStore;
import vn.edu.hcmus.slang.data.SlangDictionary;
import vn.edu.hcmus.slang.service.HistoryService;

/** Các dịch vụ và hành vi mà panel cần từ cửa sổ chính. */
public interface AppHost {

    SlangDictionary dictionary();

    DictionaryStore store();

    HistoryService history();

    DictionaryStore.LoadReport loadReport();

    void navigate(String key);

    void setStatus(String message);

    void notifyDataChanged();
}
