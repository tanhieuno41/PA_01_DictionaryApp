package vn.edu.hcmus.slang;

import vn.edu.hcmus.slang.data.DictionaryStore;
import vn.edu.hcmus.slang.data.SlangDictionary;
import vn.edu.hcmus.slang.model.HistoryEntry;
import vn.edu.hcmus.slang.model.QuizQuestion;
import vn.edu.hcmus.slang.model.Slang;
import vn.edu.hcmus.slang.service.HistoryService;
import vn.edu.hcmus.slang.service.QuizService;
import vn.edu.hcmus.slang.util.TextNormalizer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Bộ kiểm thử tự động (không cần thư viện ngoài).
 *
 * <p>Chạy: {@code java -cp "build/classes;build/test-classes" vn.edu.hcmus.slang.AllTests}</p>
 */
public final class AllTests {

    private static final Path ROOT = Paths.get("slang.txt");
    private static final Path TEMP_ROOT = Paths.get("build", "test-tmp");

    private static int passed;
    private static int failed;
    private static final List<String> failures = new ArrayList<>();
    private static List<Slang> originalEntries;
    private static SlangDictionary fullDictionary;

    public static void main(String[] args) throws Exception {
        System.out.println("============================================================");
        System.out.println(" SLANG DICTIONARY - AUTOMATED TEST SUITE");
        System.out.println("============================================================");
        if (!Files.isRegularFile(ROOT)) {
            System.err.println("Không tìm thấy slang.txt ở thư mục hiện tại.");
            System.exit(2);
        }
        deleteRecursively(TEMP_ROOT);
        Files.createDirectories(TEMP_ROOT);

        originalEntries = DictionaryStore.parseTextFile(ROOT);
        fullDictionary = SlangDictionary.fromEntries(originalEntries);

        test("1. Parser & dataset", AllTests::testDataset);
        test("2. Search by slang word", AllTests::testSearchByWord);
        test("3. Search by definition", AllTests::testDefinitionSearch);
        test("4. Add / overwrite / edit / delete", AllTests::testMutations);
        test("5. Persistence & snapshot fallback", AllTests::testPersistence);
        test("6. Reset to original data", AllTests::testReset);
        test("7. History persistence", AllTests::testHistory);
        test("8. Quiz generation", AllTests::testQuiz);
        test("9. Performance with 100,000 slang words", AllTests::testPerformance100k);

        System.out.println("------------------------------------------------------------");
        System.out.println(" KẾT QUẢ: " + passed + " passed, " + failed + " failed");
        if (!failures.isEmpty()) {
            for (String failure : failures) {
                System.out.println("  - " + failure);
            }
        }
        System.out.println("============================================================");
        deleteRecursively(TEMP_ROOT);
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void testDataset() {
        check("dataset có 7.656 mục từ", originalEntries.size() == 7656,
                "actual=" + originalEntries.size());
        int definitions = originalEntries.stream().mapToInt(s -> s.getDefinitions().size()).sum();
        check("dataset có 8.959 định nghĩa", definitions == 8959, "actual=" + definitions);
        check("không có mục từ rỗng", originalEntries.stream().allMatch(s -> !s.getWord().isBlank()
                && !s.getDefinitions().isEmpty()));
        Slang ama = originalEntries.stream().filter(s -> s.getWord().equalsIgnoreCase("AMA"))
                .findFirst().orElse(null);
        check("đọc được dòng định nghĩa nối tiếp (AMA)", ama != null
                && ama.getDefinitions().stream().anyMatch(d -> d.equalsIgnoreCase("Against Medical Advice")));
        check("chuẩn hoá khoá từ", fullDictionary.uniqueWordCount() > 7500,
                "unique=" + fullDictionary.uniqueWordCount());
    }

    private static void testSearchByWord() {
        List<Slang> bomb = fullDictionary.searchByWord("BOMB");
        check("tìm thấy BOMB", bomb.size() == 1, "size=" + bomb.size());
        check("BOMB có 2 định nghĩa", bomb.size() == 1 && bomb.get(0).getDefinitions().size() == 2);
        List<Slang> lower = fullDictionary.searchByWord("bomb");
        check("tìm không phân biệt hoa thường", !lower.isEmpty()
                && lower.get(0).getId() == bomb.get(0).getId());
        List<Slang> mixed = fullDictionary.searchByWord("  BoMb  ");
        check("trim + mixed case", !mixed.isEmpty()
                && mixed.get(0).getId() == bomb.get(0).getId());
        check("không tìm thấy từ không tồn tại",
                fullDictionary.searchByWord("zzzz-not-a-word").isEmpty());
        check("gợi ý theo prefix", fullDictionary.suggestWords("bom", 5).stream()
                .anyMatch(word -> word.equalsIgnoreCase("bomb")));
        check("tra cứu từ khoá rỗng trả về rỗng", fullDictionary.searchByWord("   ").isEmpty());
    }

    private static void testDefinitionSearch() {
        String[] keywords = {"money", "go", "the", "has money", "friend", "zzzznotfound"};
        for (String keyword : keywords) {
            long start = System.nanoTime();
            List<Slang> actual = fullDictionary.searchByDefinition(keyword);
            long millis = (System.nanoTime() - start) / 1_000_000;
            List<Long> expectedIds = bruteForceDefinitionIds(originalEntries, keyword);
            List<Long> actualIds = actual.stream().map(Slang::getId).sorted().toList();
            check("definition search khớp brute-force: '" + keyword + "'",
                    actualIds.equals(expectedIds),
                    "actual=" + actualIds.size() + ", expected=" + expectedIds.size());
            String normalized = TextNormalizer.normalizeSearchText(keyword);
            check("mọi kết quả đều chứa keyword: '" + keyword + "'",
                    actual.stream().allMatch(s -> s.getSearchText().contains(normalized)));
            check("thời gian tìm '" + keyword + "' < 1000ms", millis < 1000, millis + "ms");
        }
        check("không tìm thấy định nghĩa trả về list rỗng",
                fullDictionary.searchByDefinition("qqqqzzzz").isEmpty());
    }

    private static void testMutations() {
        SlangDictionary dictionary = SlangDictionary.fromEntries(originalEntries.subList(0, 80));
        int base = dictionary.size();

        Slang added = dictionary.add("ZZTEST", TextNormalizer.splitDefinitions("first|second"));
        check("add tăng số mục", dictionary.size() == base + 1);
        check("add tìm thấy ngay", dictionary.searchByWord("zztest").size() == 1);
        check("add lưu nhiều định nghĩa", dictionary.searchByWord("zztest").get(0).getDefinitions().size() == 2);

        dictionary.add("zztest", TextNormalizer.splitDefinitions("duplicate"));
        List<Slang> duplicates = dictionary.searchByWord("ZZTEST");
        check("cho phép thêm bản trùng", duplicates.size() == 2, "size=" + duplicates.size());

        boolean overwritten = dictionary.overwrite(added.getId(),
                TextNormalizer.splitDefinitions("overwritten definition"));
        check("overwrite thành công", overwritten);
        List<Slang> afterOverwrite = dictionary.searchByWord("zztest");
        check("overwrite thay định nghĩa",
                afterOverwrite.get(0).getDefinitions().size() == 1
                        && afterOverwrite.get(0).getDefinitions().get(0).equals("overwritten definition"));

        long duplicateId = duplicates.get(1).getId();
        boolean updated = dictionary.update(duplicateId, "ZZTEST-TWO",
                TextNormalizer.splitDefinitions("edited"));
        check("edit thành công", updated);
        check("edit đổi khoá cũ", dictionary.searchByWord("zztest").size() == 1);
        check("edit tìm thấy khoá mới",
                dictionary.searchByWord("zztest-two").get(0).getDefinitions().get(0).equals("edited"));

        boolean deleted = dictionary.delete(added.getId());
        check("delete thành công", deleted);
        check("delete giảm số mục", dictionary.size() == base + 1);
        dictionary.validateIndexes();
        check("chỉ mục nhất quán sau thao tác", true);

        boolean thrownEmpty = false;
        try {
            dictionary.add("   ", List.of("x"));
        } catch (IllegalArgumentException ex) {
            thrownEmpty = true;
        }
        check("từ rỗng bị từ chối", thrownEmpty);
    }

    private static void testPersistence() throws Exception {
        Path dir = TEMP_ROOT.resolve("persistence");
        Files.createDirectories(dir);
        SlangDictionary source = SlangDictionary.fromEntries(originalEntries.subList(0, 1000));
        DictionaryStore store = new DictionaryStore(dir);
        store.save(source);
        check("save tạo file working", Files.isRegularFile(store.getWorkingFile()));
        check("save tạo snapshot", Files.isRegularFile(store.getSnapshotFile()));
        long snapshotSize = Files.size(store.getSnapshotFile());
        check("snapshot có dữ liệu", snapshotSize > 1024, snapshotSize + " bytes");

        DictionaryStore.LoadReport loaded = new DictionaryStore(dir).load();
        check("lần nạp sau dùng snapshot, không index lại", loaded.indexLoaded(),
                loaded.message());
        check("dữ liệu sau khi nạp khớp", loaded.dictionary().size() == source.size());
        check("tra cứu từ khoá sau khi nạp", loaded.dictionary().searchByWord("BOMB").size() == 1);
        check("tra cứu định nghĩa sau khi nạp",
                loaded.dictionary().searchByDefinition("money").size()
                        == source.searchByDefinition("money").size());

        // Snapshot hỏng -> phải rebuild từ text, không crash.
        Files.write(store.getSnapshotFile(), new byte[]{1, 2, 3, 4, 5});
        DictionaryStore.LoadReport recovered = new DictionaryStore(dir).load();
        check("snapshot hỏng được rebuild", !recovered.indexLoaded());
        check("rebuild giữ đúng dữ liệu", recovered.dictionary().size() == source.size());

        // File text bị thay đổi -> fingerprint lệch -> rebuild và thấy dữ liệu mới.
        Files.writeString(store.getWorkingFile(), "NEWWORD`new definition\n",
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        DictionaryStore.LoadReport stale = new DictionaryStore(dir).load();
        check("phát hiện snapshot cũ", !stale.indexLoaded());
        check("nạp dữ liệu mới sau khi file đổi",
                stale.dictionary().searchByWord("newword").size() == 1);
        check("file text lưu đúng header", Files.readAllLines(store.getWorkingFile(),
                StandardCharsets.UTF_8).get(0).startsWith("Slang`"));
    }

    private static void testReset() throws Exception {
        Path dir = TEMP_ROOT.resolve("reset");
        Files.createDirectories(dir);
        Files.copy(ROOT, dir.resolve("slang.txt"));
        DictionaryStore store = new DictionaryStore(dir);
        DictionaryStore.LoadReport report = store.load();
        SlangDictionary dictionary = report.dictionary();
        int originalCount = dictionary.size();
        dictionary.add("TEMPWORD", List.of("temporary"));
        store.save(dictionary);
        check("reset: có file modified sau khi sửa", store.hasModifiedData());
        check("reset: dữ liệu tạm tìm thấy", dictionary.searchByWord("TEMPWORD").size() == 1);

        store.resetToOriginal(dictionary);
        check("reset: số mục trở về gốc", dictionary.size() == originalCount,
                dictionary.size() + " vs " + originalCount);
        check("reset: từ tạm biến mất", dictionary.searchByWord("TEMPWORD").isEmpty());
        check("reset: xoá file modified", !store.hasModifiedData());

        DictionaryStore.LoadReport reloaded = new DictionaryStore(dir).load();
        check("reset: lần nạp sau dùng snapshot gốc", reloaded.indexLoaded());
        check("reset: dữ liệu gốc được giữ", reloaded.dictionary().size() == originalCount);
    }

    private static void testHistory() throws Exception {
        Path file = TEMP_ROOT.resolve("history").resolve("history.txt");
        HistoryService history = HistoryService.load(file);
        check("history bắt đầu rỗng", history.size() == 0);
        history.record(HistoryEntry.SearchType.WORD, "alpha", 1);
        history.record(HistoryEntry.SearchType.DEFINITION, "beta", 5);
        history.record(HistoryEntry.SearchType.WORD, "gamma", 2);
        check("history ghi 3 mục", history.size() == 3);
        check("history mới nhất ở đầu", history.entries().get(0).keyword().equals("gamma"));
        check("history giữ loại tra cứu",
                history.entries().get(1).type() == HistoryEntry.SearchType.DEFINITION);

        HistoryService reloaded = HistoryService.load(file);
        check("history đọc lại từ file", reloaded.size() == 3);
        check("history giữ đúng thứ tự", reloaded.entries().get(0).keyword().equals("gamma"));

        for (int i = 0; i < 205; i++) {
            history.record(HistoryEntry.SearchType.WORD, "cap" + i, 0);
        }
        check("history giới hạn 200 mục", history.size() == 200, "size=" + history.size());
        check("history bỏ mục cũ nhất", history.entries().get(0).keyword().equals("cap204"));

        history.clear();
        check("history clear", history.size() == 0);
        check("history clear ghi xuống file", HistoryService.load(file).size() == 0);
    }

    private static void testQuiz() {
        QuizService wordService = new QuizService(fullDictionary, new Random(7));
        QuizService definitionService = new QuizService(fullDictionary, new Random(11));
        int errors = 0;
        for (int i = 0; i < 200; i++) {
            QuizQuestion word = wordService.nextQuestion(QuizQuestion.Mode.WORD_TO_DEFINITION);
            if (word.choices().size() != 4) {
                errors++;
                continue;
            }
            Set<String> unique = new HashSet<>();
            for (String choice : word.choices()) {
                unique.add(TextNormalizer.normalizeSearchText(choice));
            }
            if (unique.size() != 4
                    || !word.choices().get(word.correctIndex()).equals(word.correctChoice())
                    || !word.slang().getDefinitions().contains(word.correctChoice())) {
                errors++;
            }

            QuizQuestion definition = definitionService.nextQuestion(
                    QuizQuestion.Mode.DEFINITION_TO_WORD);
            Set<String> uniqueWords = new HashSet<>();
            for (String choice : definition.choices()) {
                uniqueWords.add(TextNormalizer.normalizeWord(choice));
            }
            if (uniqueWords.size() != 4
                    || !definition.correctChoice().equals(definition.slang().getWord())
                    || !definition.slang().getDefinitions().contains(definition.prompt())) {
                errors++;
            }
        }
        check("200 câu word→nghĩa có 4 đáp án duy nhất và đúng", errors == 0,
                "errors=" + errors);
        check("200 câu nghĩa→word có 4 đáp án duy nhất và đúng", errors == 0,
                "errors=" + errors);
    }

    private static void testPerformance100k() throws Exception {
        System.out.println("  · Đang tạo 100.000 slang word...");
        List<Slang> big = new ArrayList<>(100_000);
        long start = System.nanoTime();
        for (int i = 0; i < 100_000; i++) {
            Slang base = originalEntries.get(i % originalEntries.size());
            big.add(new Slang(i + 1L, base.getWord() + "_" + i, base.getDefinitions(), 0, 0));
        }
        long generateMillis = millis(start);
        System.out.println("    generate = " + generateMillis + " ms");

        start = System.nanoTime();
        SlangDictionary dictionary = SlangDictionary.fromEntries(big);
        long buildMillis = millis(start);
        System.out.println("    build dictionary + trigram index = " + buildMillis + " ms");
        check("100k: nạp đủ 100.000 mục", dictionary.size() == 100_000);
        check("100k: xây chỉ mục < 5s", buildMillis < 5000, buildMillis + " ms");

        String target = big.get(99_999).getWord();
        start = System.nanoTime();
        for (int i = 0; i < 200; i++) {
            dictionary.searchByWord(target.toLowerCase(Locale.ROOT));
        }
        long exactMillis = millis(start);
        double average = exactMillis / 200.0;
        System.out.println("    200 exact searches = " + exactMillis + " ms (avg " + average + " ms)");
        check("100k: 200 tra cứu chính xác < 1s", exactMillis < 1000, exactMillis + " ms");

        String[] keywords = {"money", "go", "the", "has money", "zzzznotfound"};
        for (String keyword : keywords) {
            start = System.nanoTime();
            List<Slang> result = dictionary.searchByDefinition(keyword);
            long searchMillis = millis(start);
            System.out.println("    def '" + keyword + "' = " + result.size() + " kết quả / "
                    + searchMillis + " ms");
            check("100k: definition search '" + keyword + "' < 1s",
                    searchMillis < 1000, searchMillis + " ms");
        }

        Path dir = TEMP_ROOT.resolve("perf-100k");
        Files.createDirectories(dir);
        DictionaryStore store = new DictionaryStore(dir);
        start = System.nanoTime();
        store.save(dictionary);
        long saveMillis = millis(start);
        System.out.println("    save text + snapshot = " + saveMillis + " ms, snapshot="
                + Files.size(store.getSnapshotFile()) / 1024 / 1024 + " MB");
        check("100k: lưu snapshot thành công", Files.size(store.getSnapshotFile()) > 1024);

        start = System.nanoTime();
        DictionaryStore.LoadReport report = new DictionaryStore(dir).load();
        long loadMillis = millis(start);
        System.out.println("    load snapshot = " + loadMillis
                + " ms (loadedIndex=" + report.indexLoaded() + ")");
        check("100k: lần chạy sau nạp chỉ mục sẵn, không rebuild", report.indexLoaded());
        check("100k: nạp snapshot < 1.5s", loadMillis < 1500, loadMillis + " ms");

        start = System.nanoTime();
        List<Slang> afterLoad = report.dictionary().searchByDefinition("money");
        long afterLoadMillis = millis(start);
        System.out.println("    def 'money' sau khi nạp = " + afterLoad.size()
                + " kết quả / " + afterLoadMillis + " ms");
        check("100k: tìm kiếm sau khi nạp < 1s", afterLoadMillis < 1000,
                afterLoadMillis + " ms");
        check("100k: kết quả sau nạp khớp", afterLoad.size()
                == dictionary.searchByDefinition("money").size());
    }

    private static List<Long> bruteForceDefinitionIds(List<Slang> entries, String keyword) {
        String normalized = TextNormalizer.normalizeSearchText(keyword);
        return entries.stream()
                .filter(slang -> slang.matchesDefinition(normalized))
                .map(Slang::getId)
                .sorted()
                .toList();
    }

    private static long millis(long startNanos) {
        return Math.max(0, (System.nanoTime() - startNanos) / 1_000_000);
    }

    private static void check(String name, boolean condition) {
        check(name, condition, null);
    }

    private static void check(String name, boolean condition, String detail) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + name);
        } else {
            failed++;
            String message = name + (detail == null || detail.isBlank() ? "" : " (" + detail + ")");
            failures.add(message);
            System.out.println("  [FAIL] " + message);
        }
    }

    private static void test(String name, TestBody body) {
        System.out.println("\n" + name);
        try {
            body.run();
        } catch (Throwable ex) {
            failed++;
            failures.add(name + " threw " + ex);
            System.out.println("  [FAIL] " + name + " threw " + ex);
            ex.printStackTrace(System.out);
        }
    }

    @FunctionalInterface
    private interface TestBody {
        void run() throws Exception;
    }

    private static void deleteRecursively(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(item -> {
                try {
                    Files.deleteIfExists(item);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    private AllTests() {
    }
}
