package vn.edu.hcmus.slang;

import vn.edu.hcmus.slang.ui.MainFrame;
import vn.edu.hcmus.slang.ui.Theme;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Chế độ kiểm thử giao diện: mở cửa sổ, đi qua toàn bộ 11 màn hình và chụp
 * ảnh vào {@code report/screens}. Đây cũng là smoke-test cho toàn bộ UI.
 */
public final class ScreenshotMode {

    private ScreenshotMode() {
    }

    public static void run() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        Theme.install();
        try {
            BootData data = App.bootstrap();
            Path output = Paths.get("report", "screens").toAbsolutePath();
            Files.createDirectories(output);

            MainFrame[] holder = new MainFrame[1];
            SwingUtilities.invokeAndWait(() -> {
                MainFrame frame = new MainFrame(data);
                frame.setSize(1280, 840);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                holder[0] = frame;
            });
            Thread.sleep(900);
            MainFrame frame = holder[0];

            String[][] shots = {
                    {MainFrame.HOME, "01-home"},
                    {MainFrame.SEARCH, "02-search-word"},
                    {MainFrame.DEFINITION, "03-search-definition"},
                    {MainFrame.HISTORY, "04-history"},
                    {MainFrame.ADD, "05-add"},
                    {MainFrame.EDIT, "06-edit"},
                    {MainFrame.DELETE, "07-delete"},
                    {MainFrame.RESET, "08-reset"},
                    {MainFrame.RANDOM, "09-random"},
                    {MainFrame.QUIZ_WORD, "10-quiz-word"},
                    {MainFrame.QUIZ_DEFINITION, "11-quiz-definition"}
            };

            for (String[] shot : shots) {
                SwingUtilities.invokeAndWait(() -> {
                    frame.showPanel(shot[0]);
                    frame.prepareDemo();
                    frame.validate();
                    frame.repaint();
                });
                Thread.sleep(350);
                Path file = output.resolve(shot[1] + ".png");
                SwingUtilities.invokeAndWait(() -> capture(frame, file));
                System.out.println("[Screenshot] " + file);
            }

            // Ảnh bổ sung: gợi ý autocomplete (word ở panel 1, definition ở panel 2).
            SwingUtilities.invokeAndWait(() -> {
                frame.showPanel(MainFrame.SEARCH);
                frame.prepareSuggestionDemo();
                frame.validate();
                frame.repaint();
            });
            Thread.sleep(300);
            Path wordSuggestions = output.resolve("02b-search-word-suggestions.png");
            SwingUtilities.invokeAndWait(() -> capture(frame, wordSuggestions));
            System.out.println("[Screenshot] " + wordSuggestions);

            SwingUtilities.invokeAndWait(() -> {
                frame.showPanel(MainFrame.DEFINITION);
                frame.prepareSuggestionDemo();
                frame.validate();
                frame.repaint();
            });
            Thread.sleep(300);
            Path definitionSuggestions = output.resolve("03b-search-definition-suggestions.png");
            SwingUtilities.invokeAndWait(() -> capture(frame, definitionSuggestions));
            System.out.println("[Screenshot] " + definitionSuggestions);

            // Ảnh bổ sung: trạng thái đã trả lời sai (đáp án đúng viền đậm,
            // đáp án chọn sai viền nét đứt) — không dùng màu.
            SwingUtilities.invokeAndWait(() -> {
                frame.showPanel(MainFrame.QUIZ_WORD);
                frame.prepareDemo();
                frame.answerDemoQuizWrong();
                frame.validate();
                frame.repaint();
            });
            Thread.sleep(300);
            Path answeredWord = output.resolve("10b-quiz-word-answered.png");
            SwingUtilities.invokeAndWait(() -> capture(frame, answeredWord));
            System.out.println("[Screenshot] " + answeredWord);

            SwingUtilities.invokeAndWait(() -> {
                frame.showPanel(MainFrame.QUIZ_DEFINITION);
                frame.prepareDemo();
                frame.answerDemoQuizWrong();
                frame.validate();
                frame.repaint();
            });
            Thread.sleep(300);
            Path answeredDefinition = output.resolve("11b-quiz-definition-answered.png");
            SwingUtilities.invokeAndWait(() -> capture(frame, answeredDefinition));
            System.out.println("[Screenshot] " + answeredDefinition);

            SwingUtilities.invokeAndWait(frame::dispose);
            System.out.println("SMOKE OK: 11 màn hình, 15 ảnh đã mở/chụp thành công.");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.out.println("SMOKE FAILED: " + ex.getMessage());
            System.exit(2);
        }
    }

    private static void capture(MainFrame frame, Path file) {
        try {
            javax.swing.JRootPane root = frame.getRootPane();
            BufferedImage color = new BufferedImage(root.getWidth(), root.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = color.createGraphics();
            try {
                root.paint(graphics);
            } finally {
                graphics.dispose();
            }

            // Ảnh báo cáo chỉ dùng thang xám; loại bỏ fringe màu của ClearType.
            BufferedImage gray = new BufferedImage(color.getWidth(), color.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < color.getHeight(); y++) {
                for (int x = 0; x < color.getWidth(); x++) {
                    int rgb = color.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    int luminance = (r * 299 + g * 587 + b * 114) / 1000;
                    gray.setRGB(x, y, (luminance << 16) | (luminance << 8) | luminance);
                }
            }
            ImageIO.write(gray, "png", file.toFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
