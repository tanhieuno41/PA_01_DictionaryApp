package vn.edu.hcmus.slang.service;

import vn.edu.hcmus.slang.model.HistoryEntry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Lịch sử tra cứu, được lưu bền vào {@code data/history.txt}.
 * Mục mới nhất nằm ở đầu danh sách.
 */
public final class HistoryService {

    private static final int MAX_ENTRIES = 200;
    private static final String SEPARATOR = "\t";

    private final Path file;
    private final Deque<HistoryEntry> entries = new ArrayDeque<>();

    private HistoryService(Path file) {
        this.file = file;
    }

    public static HistoryService load(Path file) {
        HistoryService service = new HistoryService(file);
        service.read();
        return service;
    }

    public synchronized void record(HistoryEntry.SearchType type, String keyword, int resultCount) {
        String value = keyword == null ? "" : keyword.trim();
        if (value.isEmpty()) {
            return;
        }
        if (value.length() > 300) {
            value = value.substring(0, 300);
        }
        entries.addFirst(new HistoryEntry(System.currentTimeMillis(), type, value, resultCount));
        while (entries.size() > MAX_ENTRIES) {
            entries.removeLast();
        }
        saveQuietly();
    }

    public synchronized List<HistoryEntry> entries() {
        return List.copyOf(entries);
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized void clear() {
        entries.clear();
        saveQuietly();
    }

    private void read() {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    line = line.replace("\uFEFF", "");
                    first = false;
                }
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(SEPARATOR, 4);
                if (parts.length != 4) {
                    continue;
                }
                try {
                    long timestamp = Long.parseLong(parts[0]);
                    HistoryEntry.SearchType type = HistoryEntry.SearchType.valueOf(parts[1]);
                    int count = Integer.parseInt(parts[2]);
                    String keyword = unescape(parts[3]);
                    if (!keyword.isBlank()) {
                        entries.addLast(new HistoryEntry(timestamp, type, keyword, count));
                    }
                } catch (RuntimeException ex) {
                    // Bỏ qua dòng hỏng.
                }
            }
            while (entries.size() > MAX_ENTRIES) {
                entries.removeLast();
            }
        } catch (IOException ex) {
            System.err.println("[HistoryService] Không đọc được lịch sử: " + ex.getMessage());
        }
    }

    private void saveQuietly() {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                for (HistoryEntry entry : entries) {
                    writer.write(Long.toString(entry.timestamp()));
                    writer.write(SEPARATOR);
                    writer.write(entry.type().name());
                    writer.write(SEPARATOR);
                    writer.write(Integer.toString(entry.resultCount()));
                    writer.write(SEPARATOR);
                    writer.write(escape(entry.keyword()));
                    writer.newLine();
                }
            }
        } catch (IOException ex) {
            System.err.println("[HistoryService] Không ghi được lịch sử: " + ex.getMessage());
        }
    }

    private static String escape(String value) {
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '\t' -> out.append("\\t");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    private static String unescape(String value) {
        StringBuilder out = new StringBuilder(value.length());
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escaped) {
                switch (c) {
                    case 't' -> out.append('\t');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    default -> out.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                out.append(c);
            }
        }
        if (escaped) {
            out.append('\\');
        }
        return out.toString();
    }

    /** Chỉ dùng cho kiểm thử. */
    public static List<HistoryEntry> readForTest(Path file) throws IOException {
        HistoryService service = load(file);
        return new ArrayList<>(service.entries());
    }

    public static String formatTimestamp(long timestamp) {
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(java.time.ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(timestamp));
    }
}
