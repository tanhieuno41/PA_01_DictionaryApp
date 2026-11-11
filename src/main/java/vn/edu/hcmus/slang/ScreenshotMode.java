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

            SwingUtilities.invokeAndWait(frame::dispose);
            System.out.println("SMOKE OK: 11 màn hình đã mở và chụp ảnh thành công.");
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
            BufferedImage image = new BufferedImage(root.getWidth(), root.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            try {
                root.paint(graphics);
            } finally {
                graphics.dispose();
            }
            ImageIO.write(image, "png", file.toFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
