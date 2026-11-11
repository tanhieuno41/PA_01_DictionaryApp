package vn.edu.hcmus.slang.data;

import vn.edu.hcmus.slang.model.Slang;
import vn.edu.hcmus.slang.util.TextNormalizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

/**
 * Đọc / ghi dữ liệu slang và ảnh chụp chỉ mục nhị phân.
 *
 * <p>File {@code data/dictionary.bin} chứa nguyên cấu trúc đã đánh chỉ mục
 * (danh sách mục từ, chỉ mục từ khoá, posting list trigram). Nhờ vậy lần chạy
 * sau chỉ cần đọc tuần tự, không phải phân tích lại 100.000 định nghĩa.</p>
 */
public final class DictionaryStore {

    private static final int SNAPSHOT_MAGIC = 0x534C4E47; // "SLNG"
    private static final int SNAPSHOT_VERSION = 1;
    private static final int MAX_ENTRIES = 5_000_000;
    private static final int MAX_STRING_BYTES = 16 * 1024 * 1024;

    private final Path projectDir;
    private final Path originalFile;
    private final Path dataDir;
    private final Path workingFile;
    private final Path snapshotFile;
    private int originalCountCache = -1;

    public DictionaryStore(Path projectDir) {
        this.projectDir = projectDir.toAbsolutePath();
        this.originalFile = this.projectDir.resolve("slang.txt");
        this.dataDir = this.projectDir.resolve("data");
        this.workingFile = dataDir.resolve("slang_modified.txt");
        this.snapshotFile = dataDir.resolve("dictionary.bin");
    }

    public Path getProjectDir() {
        return projectDir;
    }

    public Path getOriginalFile() {
        return originalFile;
    }

    public Path getDataDir() {
        return dataDir;
    }

    public Path getWorkingFile() {
        return workingFile;
    }

    public Path getSnapshotFile() {
        return snapshotFile;
    }

    public boolean hasModifiedData() {
        return Files.isRegularFile(workingFile);
    }

    public record LoadReport(
            SlangDictionary dictionary,
            boolean indexLoaded,
            long loadMillis,
            long indexBuildMillis,
            Path sourceFile,
            String message) {
    }

    /**
     * Nạp từ điển. Ưu tiên đọc snapshot nhị phân nếu nó khớp với file dữ liệu
     * hiện tại; nếu không thì phân tích file text và xây chỉ mục một lần.
     */
    public LoadReport load() throws IOException {
        Files.createDirectories(dataDir);
        Path source = Files.isRegularFile(workingFile) ? workingFile : originalFile;
        if (!Files.isRegularFile(source)) {
            throw new IOException("Không tìm thấy file dữ liệu: " + source);
        }
        long fingerprint = fingerprint(source);
        String sourceName = source.getFileName().toString();

        if (Files.isRegularFile(snapshotFile)) {
            long start = System.nanoTime();
            try {
                DictionarySnapshot snapshot = readSnapshot(sourceName, fingerprint);
                SlangDictionary dictionary = SlangDictionary.fromSnapshot(snapshot);
                long millis = elapsedMillis(start);
                return new LoadReport(dictionary, true, millis, 0, source,
                        "Đã nạp chỉ mục có sẵn từ " + snapshotFile.getFileName() + " (" + millis + " ms)");
            } catch (IOException | RuntimeException ex) {
                // Snapshot cũ / hỏng: bỏ qua và xây lại từ file text.
                System.err.println("[DictionaryStore] Bỏ qua snapshot: " + ex.getMessage());
            }
        }

        long start = System.nanoTime();
        List<Slang> entries = parseTextFile(source);
        SlangDictionary dictionary = SlangDictionary.fromEntries(entries);
        long buildMillis = elapsedMillis(start);

        try {
            writeSnapshot(snapshotFile, dictionary.snapshot(), source, fingerprint);
        } catch (IOException ex) {
            System.err.println("[DictionaryStore] Không ghi được snapshot: " + ex.getMessage());
        }

        return new LoadReport(dictionary, false, buildMillis, buildMillis, source,
                "Đã xây dựng chỉ mục từ " + sourceName + " (" + buildMillis + " ms)");
    }

    /** Lưu dữ liệu hiện tại (text để đọc được + snapshot nhị phân). */
    public synchronized void save(SlangDictionary dictionary) throws IOException {
        Files.createDirectories(dataDir);
        DictionarySnapshot snapshot = dictionary.snapshot();
        writeTextFile(workingFile, snapshot.entries());
        long fingerprint = fingerprint(workingFile);
        try {
            writeSnapshot(snapshotFile, snapshot, workingFile, fingerprint);
        } catch (IOException ex) {
            // Text đã lưu thành công; snapshot chỉ là cache nên không làm hỏng dữ liệu.
            System.err.println("[DictionaryStore] Không ghi được snapshot: " + ex.getMessage());
        }
    }

    /** Khôi phục danh sách gốc từ slang.txt. */
    public synchronized List<Slang> resetToOriginal(SlangDictionary dictionary) throws IOException {
        if (!Files.isRegularFile(originalFile)) {
            throw new IOException("Không tìm thấy file gốc: " + originalFile);
        }
        List<Slang> entries = parseTextFile(originalFile);
        dictionary.replaceAll(entries);
        Files.deleteIfExists(workingFile);
        try {
            writeSnapshot(snapshotFile, dictionary.snapshot(), originalFile, fingerprint(originalFile));
        } catch (IOException ex) {
            System.err.println("[DictionaryStore] Không ghi được snapshot sau reset: " + ex.getMessage());
        }
        return entries;
    }

    public int originalEntryCount() throws IOException {
        if (originalCountCache < 0) {
            originalCountCache = parseTextFile(originalFile).size();
        }
        return originalCountCache;
    }

    /**
     * Phân tích file text. Dòng đầu là header. Dòng không có dấu ` được xem
     * là một định nghĩa nối tiếp của mục từ trước đó (định dạng gốc có một
     * vài trường hợp như vậy).
     */
    public static List<Slang> parseTextFile(Path path) throws IOException {
        List<Slang> entries = new ArrayList<>();
        long nextId = 1;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean firstContentLine = true;
            Slang current = null;
            while ((line = reader.readLine()) != null) {
                if (firstContentLine) {
                    line = line.replace("\uFEFF", "");
                }
                if (line.isBlank()) {
                    continue;
                }
                if (firstContentLine && isHeader(line)) {
                    firstContentLine = false;
                    continue;
                }
                firstContentLine = false;
                int separator = line.indexOf('`');
                if (separator >= 0) {
                    String word = TextNormalizer.cleanWord(line.substring(0, separator));
                    if (word.isEmpty()) {
                        continue;
                    }
                    List<String> definitions = TextNormalizer.splitDefinitionCell(line.substring(separator + 1));
                    if (definitions.isEmpty()) {
                        continue;
                    }
                    current = new Slang(nextId++, word, definitions);
                    entries.add(current);
                } else if (current != null) {
                    String continuation = TextNormalizer.cleanDefinition(line);
                    if (!continuation.isEmpty()) {
                        current.addDefinition(continuation);
                    }
                }
            }
        }
        return entries;
    }

    private static boolean isHeader(String line) {
        String lower = line.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("slag`") || lower.startsWith("slang`");
    }

    private static void writeTextFile(Path target, List<Slang> entries) throws IOException {
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            writer.write("Slang`Meaning");
            writer.newLine();
            for (Slang entry : entries) {
                writer.write(TextNormalizer.cleanWord(entry.getWord()));
                writer.write('`');
                List<String> definitions = entry.getDefinitions();
                for (int i = 0; i < definitions.size(); i++) {
                    if (i > 0) {
                        writer.write('|');
                    }
                    writer.write(TextNormalizer.cleanDefinition(definitions.get(i)));
                }
                writer.newLine();
            }
        }
        moveAtomic(temp, target);
    }

    private void writeSnapshot(Path target, DictionarySnapshot snapshot, Path source, long fingerprint)
            throws IOException {
        Files.createDirectories(target.getParent());
        Path temp = Files.createTempFile(target.getParent(), "dictionary", ".tmp");
        try (FileOut out = new FileOut(temp)) {
            DataOutputStream data = out.stream();
            data.writeInt(SNAPSHOT_MAGIC);
            data.writeInt(SNAPSHOT_VERSION);
            data.writeLong(fingerprint);
            writeString(data, source.getFileName().toString());

            data.writeInt(snapshot.entries().size());
            for (Slang entry : snapshot.entries()) {
                data.writeLong(entry.getId());
                writeString(data, entry.getWord());
                data.writeLong(entry.getCreatedAt());
                data.writeLong(entry.getUpdatedAt());
                data.writeInt(entry.getDefinitions().size());
                for (String definition : entry.getDefinitions()) {
                    writeString(data, definition);
                }
            }

            data.writeInt(snapshot.wordIndex().size());
            for (Map.Entry<String, List<Slang>> entry : snapshot.wordIndex().entrySet()) {
                writeString(data, entry.getKey());
                data.writeInt(entry.getValue().size());
                for (Slang slang : entry.getValue()) {
                    data.writeLong(slang.getId());
                }
            }

            Map<String, int[]> postings = snapshot.definitionIndex().postings();
            data.writeInt(postings.size());
            for (Map.Entry<String, int[]> entry : postings.entrySet()) {
                writeString(data, entry.getKey());
                int[] values = entry.getValue();
                data.writeInt(values.length);
                for (int value : values) {
                    data.writeInt(value);
                }
            }
            data.flush();
        } catch (IOException | RuntimeException ex) {
            Files.deleteIfExists(temp);
            throw ex;
        }
        moveAtomic(temp, target);
    }

    private DictionarySnapshot readSnapshot(String expectedSourceName, long expectedFingerprint)
            throws IOException {
        try (FileIn in = new FileIn(snapshotFile)) {
            DataInputStream data = in.stream();
            if (data.readInt() != SNAPSHOT_MAGIC) {
                throw new IOException("Magic không đúng");
            }
            int version = data.readInt();
            if (version != SNAPSHOT_VERSION) {
                throw new IOException("Phiên bản snapshot không hỗ trợ: " + version);
            }
            long fingerprint = data.readLong();
            String sourceName = readString(data);
            if (fingerprint != expectedFingerprint || !sourceName.equals(expectedSourceName)) {
                throw new IOException("Snapshot không khớp file dữ liệu hiện tại");
            }

            int entryCount = data.readInt();
            if (entryCount < 0 || entryCount > MAX_ENTRIES) {
                throw new IOException("Số mục từ không hợp lệ: " + entryCount);
            }
            List<Slang> entries = new ArrayList<>(entryCount);
            Map<Long, Slang> byId = new HashMap<>(entryCount * 2);
            for (int i = 0; i < entryCount; i++) {
                long id = data.readLong();
                String word = readString(data);
                long createdAt = data.readLong();
                long updatedAt = data.readLong();
                int definitionCount = data.readInt();
                if (definitionCount <= 0 || definitionCount > 10_000) {
                    throw new IOException("Số định nghĩa không hợp lệ: " + definitionCount);
                }
                List<String> definitions = new ArrayList<>(definitionCount);
                for (int d = 0; d < definitionCount; d++) {
                    definitions.add(readString(data));
                }
                Slang entry = new Slang(id, word, definitions, createdAt, updatedAt);
                if (byId.put(id, entry) != null) {
                    throw new IOException("Trùng id mục từ: " + id);
                }
                entries.add(entry);
            }

            int wordKeyCount = data.readInt();
            if (wordKeyCount < 0 || wordKeyCount > MAX_ENTRIES) {
                throw new IOException("Số khoá từ không hợp lệ: " + wordKeyCount);
            }
            Map<String, List<Slang>> wordIndex = new HashMap<>(wordKeyCount * 2);
            for (int i = 0; i < wordKeyCount; i++) {
                String key = readString(data);
                int count = data.readInt();
                if (count <= 0 || count > MAX_ENTRIES) {
                    throw new IOException("Danh sách từ không hợp lệ: " + count);
                }
                List<Slang> list = new ArrayList<>(count);
                for (int j = 0; j < count; j++) {
                    long id = data.readLong();
                    Slang entry = byId.get(id);
                    if (entry == null) {
                        throw new IOException("Khoá từ trỏ tới id không tồn tại: " + id);
                    }
                    list.add(entry);
                }
                wordIndex.put(key, list);
            }

            int trigramCount = data.readInt();
            if (trigramCount < 0 || trigramCount > 10_000_000) {
                throw new IOException("Số trigram không hợp lệ: " + trigramCount);
            }
            Map<String, int[]> postings = new HashMap<>(trigramCount * 2);
            for (int i = 0; i < trigramCount; i++) {
                String key = readString(data);
                int count = data.readInt();
                if (count <= 0 || count > entryCount) {
                    throw new IOException("Posting list không hợp lệ: " + count);
                }
                int[] values = new int[count];
                for (int j = 0; j < count; j++) {
                    int value = data.readInt();
                    if (value < 0 || value >= entryCount) {
                        throw new IOException("Chỉ số posting không hợp lệ: " + value);
                    }
                    values[j] = value;
                }
                postings.put(key, values);
            }
            if (data.read() != -1) {
                throw new IOException("Snapshot còn dữ liệu thừa");
            }
            return new DictionarySnapshot(entries, wordIndex, TrigramIndex.fromPostings(postings));
        } catch (EOFException ex) {
            throw new IOException("Snapshot bị cắt ngắn", ex);
        }
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length < 0 || length > MAX_STRING_BYTES) {
            throw new IOException("Độ dài chuỗi không hợp lệ: " + length);
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** CRC32 + kích thước file, đủ nhanh cho file vài chục MB. */
    public static long fingerprint(Path path) throws IOException {
        CRC32 crc = new CRC32();
        long size = Files.size(path);
        byte[] buffer = new byte[1 << 16];
        try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            int read;
            while ((read = in.read(buffer)) >= 0) {
                crc.update(buffer, 0, read);
            }
        }
        return (size << 32) ^ crc.getValue();
    }

    private static long elapsedMillis(long startNanos) {
        return Math.max(0, (System.nanoTime() - startNanos) / 1_000_000);
    }

    private static void moveAtomic(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static final class FileOut implements AutoCloseable {
        private final OutputStream file;
        private final DataOutputStream stream;

        FileOut(Path path) throws IOException {
            this.file = Files.newOutputStream(path, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            this.stream = new DataOutputStream(new BufferedOutputStream(file, 1 << 16));
        }

        DataOutputStream stream() {
            return stream;
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }

    private static final class FileIn implements AutoCloseable {
        private final InputStream file;
        private final DataInputStream stream;

        FileIn(Path path) throws IOException {
            this.file = Files.newInputStream(path);
            this.stream = new DataInputStream(new BufferedInputStream(file, 1 << 16));
        }

        DataInputStream stream() {
            return stream;
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }
}
