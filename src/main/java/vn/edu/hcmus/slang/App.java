package vn.edu.hcmus.slang;

import vn.edu.hcmus.slang.data.DictionaryStore;
import vn.edu.hcmus.slang.service.HistoryService;
import vn.edu.hcmus.slang.ui.MainFrame;
import vn.edu.hcmus.slang.ui.Theme;
import vn.edu.hcmus.slang.ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.nio.file.Path;

/** Điểm khởi động ứng dụng. */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        Theme.install();
        for (String arg : args) {
            if ("--smoke-test".equals(arg) || "--screenshots".equals(arg)) {
                ScreenshotMode.run();
                return;
            }
        }
        SwingUtilities.invokeLater(App::start);
    }

    private static void start() {
        JWindow splash = createSplash();
        splash.setVisible(true);
        new SwingWorker<BootData, Void>() {
            @Override
            protected BootData doInBackground() {
                try {
                    return bootstrap();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            protected void done() {
                splash.dispose();
                try {
                    BootData data = get();
                    MainFrame frame = new MainFrame(data);
                    frame.setVisible(true);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    cause.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "<html><div style='width:460px'>Không khởi động được ứng dụng:<br><b>"
                                    + cause.getMessage() + "</b><br><br>"
                                    + "Hãy kiểm tra file slang_clean.txt / slang.txt và quyền ghi thư mục dự án.</div></html>",
                            "Lỗi khởi động", JOptionPane.PLAIN_MESSAGE);
                    System.exit(1);
                }
            }
        }.execute();
    }

    /** Nạp dữ liệu và lịch sử; dùng chung cho chế độ bình thường và chụp ảnh. */
    public static BootData bootstrap() throws java.io.IOException {
        Path projectDir = AppPaths.resolveProjectDir();
        AppPaths.ensureOriginalData(projectDir);
        DictionaryStore store = new DictionaryStore(projectDir);
        DictionaryStore.LoadReport report = store.load();
        HistoryService history = HistoryService.load(store.getDataDir().resolve("history.txt"));
        System.out.println("[SlangDictionary] project=" + projectDir);
        System.out.println("[SlangDictionary] source=" + report.sourceFile().getFileName()
                + ", indexLoaded=" + report.indexLoaded()
                + ", loadMs=" + report.loadMillis()
                + ", buildMs=" + report.indexBuildMillis()
                + ", entries=" + report.dictionary().size());
        return new BootData(report.dictionary(), store, history, report);
    }

    private static JWindow createSplash() {
        JWindow window = new JWindow();
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(Theme.SIDEBAR);
        panel.setBorder(new EmptyBorder(30, 34, 26, 34));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("SLANG DICTIONARY");
        title.setFont(Theme.font(24, Font.BOLD));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Đang nạp dữ liệu và chỉ mục tìm kiếm…");
        subtitle.setFont(Theme.font(12, Font.PLAIN));
        subtitle.setForeground(new Color(0xA3A3A3));
        JLabel course = new JLabel("CSC13002 · Lập trình ứng dụng Java");
        course.setFont(Theme.font(10, Font.PLAIN));
        course.setForeground(new Color(0x737373));
        title.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        course.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        text.add(title);
        text.add(Box.createVerticalStrut(6));
        text.add(subtitle);
        text.add(Box.createVerticalStrut(3));
        text.add(course);

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        progress.setBorder(BorderFactory.createEmptyBorder());
        progress.setBackground(new Color(0x262626));
        progress.setForeground(Color.WHITE);
        progress.setPreferredSize(new Dimension(360, 4));

        panel.add(text, BorderLayout.CENTER);
        panel.add(progress, BorderLayout.SOUTH);
        window.setContentPane(panel);
        window.setSize(440, 190);
        window.setLocationRelativeTo(null);
        return window;
    }

    /** Chỉ dùng cho kiểm tra nhanh từ dòng lệnh. */
    public static String describe(BootData data) {
        return Ui.formatInt(data.dictionary().size()) + " mục từ";
    }
}
