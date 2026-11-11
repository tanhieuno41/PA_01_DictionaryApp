package vn.edu.hcmus.slang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/** Xác định thư mục làm việc và bảo đảm luôn có file dữ liệu. */
public final class AppPaths {

    public static final String ORIGINAL_FILE_NAME = "slang.txt";
    public static final String CLEAN_FILE_NAME = "slang_clean.txt";
    public static final String DATA_DIR_NAME = "data";

    private AppPaths() {
    }

    public static Path resolveProjectDir() {
        Path current = Paths.get("").toAbsolutePath();
        if (hasDataFile(current)) {
            return current;
        }
        Path codeDir = codeSourceDirectory();
        if (codeDir != null) {
            if (hasDataFile(codeDir)) {
                return codeDir;
            }
            Path parent = codeDir.getParent();
            if (parent != null && hasDataFile(parent)) {
                return parent;
            }
        }
        return current;
    }

    private static boolean hasDataFile(Path directory) {
        return Files.isRegularFile(directory.resolve(ORIGINAL_FILE_NAME))
                || Files.isRegularFile(directory.resolve(CLEAN_FILE_NAME));
    }

    public static Path dataDir(Path projectDir) {
        return projectDir.resolve(DATA_DIR_NAME);
    }

    /**
     * Bảo đảm có file dữ liệu. Bản gốc {@code slang.txt} được giữ nguyên để
     * đối chiếu; bản đã chuẩn hoá {@code slang_clean.txt} là dữ liệu ứng dụng
     * ưu tiên sử dụng. Nếu thiếu file, giải nén bản đi kèm jar; nếu không có
     * resource thì tạo dữ liệu mẫu nhỏ.
     */
    public static Path ensureOriginalData(Path projectDir) throws IOException {
        Files.createDirectories(projectDir);
        Path original = projectDir.resolve(ORIGINAL_FILE_NAME);
        Path clean = projectDir.resolve(CLEAN_FILE_NAME);

        if (!Files.isRegularFile(original)) {
            if (!extractResource(ORIGINAL_FILE_NAME, original)) {
                Files.writeString(original, sampleData(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        if (!Files.isRegularFile(clean)) {
            extractResource(CLEAN_FILE_NAME, clean);
        }
        return original;
    }

    private static boolean extractResource(String name, Path target) throws IOException {
        try (InputStream resource = AppPaths.class.getResourceAsStream("/data/" + name)) {
            if (resource == null) {
                return false;
            }
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temp = Files.createTempFile(parent, "data", ".tmp");
            Files.copy(resource, temp, StandardCopyOption.REPLACE_EXISTING);
            moveAtomic(temp, target);
            return true;
        }
    }

    private static Path codeSourceDirectory() {
        try {
            URI uri = AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path path = Paths.get(uri);
            return Files.isDirectory(path) ? path : path.getParent();
        } catch (Exception ex) {
            return null;
        }
    }

    private static void moveAtomic(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String sampleData() {
        return """
                Slang`Meaning
                BOMB`Something really good|Something really bad
                BRB`Be right back
                BTW`By the way
                FOMO`Fear of missing out
                GOAT`Greatest of all time
                IDK`I don't know
                LOL`Laughing out loud
                ROFL`Rolling on the floor laughing
                TBH`To be honest
                YOLO`You only live once
                """;
    }
}
