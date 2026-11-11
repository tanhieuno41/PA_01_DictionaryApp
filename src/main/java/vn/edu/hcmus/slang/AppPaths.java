package vn.edu.hcmus.slang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/** Xác định thư mục làm việc và bảo đảm luôn có file dữ liệu gốc. */
public final class AppPaths {

    public static final String ORIGINAL_FILE_NAME = "slang.txt";
    public static final String DATA_DIR_NAME = "data";

    private AppPaths() {
    }

    public static Path resolveProjectDir() {
        Path current = Paths.get("").toAbsolutePath();
        if (Files.isRegularFile(current.resolve(ORIGINAL_FILE_NAME))) {
            return current;
        }
        Path codeDir = codeSourceDirectory();
        if (codeDir != null) {
            if (Files.isRegularFile(codeDir.resolve(ORIGINAL_FILE_NAME))) {
                return codeDir;
            }
            Path parent = codeDir.getParent();
            if (parent != null && Files.isRegularFile(parent.resolve(ORIGINAL_FILE_NAME))) {
                return parent;
            }
        }
        return current;
    }

    public static Path dataDir(Path projectDir) {
        return projectDir.resolve(DATA_DIR_NAME);
    }

    /**
     * Nếu thiếu slang.txt (ví dụ chạy jar ở thư mục khác), giải nén bản dữ
     * liệu đi kèm trong jar. Nếu resource cũng không có, tạo dữ liệu mẫu nhỏ.
     */
    public static Path ensureOriginalData(Path projectDir) throws IOException {
        Files.createDirectories(projectDir);
        Path original = projectDir.resolve(ORIGINAL_FILE_NAME);
        if (Files.isRegularFile(original)) {
            return original;
        }
        try (InputStream resource = AppPaths.class.getResourceAsStream("/data/" + ORIGINAL_FILE_NAME)) {
            if (resource != null) {
                Path temp = Files.createTempFile(projectDir, "slang", ".tmp");
                Files.copy(resource, temp, StandardCopyOption.REPLACE_EXISTING);
                moveAtomic(temp, original);
                return original;
            }
        }
        Files.writeString(original, sampleData(), java.nio.charset.StandardCharsets.UTF_8);
        return original;
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
