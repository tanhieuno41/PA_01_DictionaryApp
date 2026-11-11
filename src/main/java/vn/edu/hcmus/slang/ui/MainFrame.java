package vn.edu.hcmus.slang.ui;

import vn.edu.hcmus.slang.BootData;
import vn.edu.hcmus.slang.data.DictionaryStore;
import vn.edu.hcmus.slang.data.SlangDictionary;
import vn.edu.hcmus.slang.model.QuizQuestion;
import vn.edu.hcmus.slang.service.HistoryService;
import vn.edu.hcmus.slang.ui.component.Chip;
import vn.edu.hcmus.slang.ui.component.NavButton;
import vn.edu.hcmus.slang.ui.component.RoundedPanel;
import vn.edu.hcmus.slang.ui.panel.AddPanel;
import vn.edu.hcmus.slang.ui.panel.DefinitionSearchPanel;
import vn.edu.hcmus.slang.ui.panel.DeletePanel;
import vn.edu.hcmus.slang.ui.panel.EditPanel;
import vn.edu.hcmus.slang.ui.panel.HistoryPanel;
import vn.edu.hcmus.slang.ui.panel.HomePanel;
import vn.edu.hcmus.slang.ui.panel.QuizPanel;
import vn.edu.hcmus.slang.ui.panel.RandomPanel;
import vn.edu.hcmus.slang.ui.panel.ResetPanel;
import vn.edu.hcmus.slang.ui.panel.SearchWordPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/** Cửa sổ chính: sidebar điều hướng, menu, nội dung và thanh trạng thái. */
public final class MainFrame extends JFrame implements AppHost {

    public static final String HOME = "home";
    public static final String SEARCH = "search";
    public static final String DEFINITION = "definition";
    public static final String HISTORY = "history";
    public static final String ADD = "add";
    public static final String EDIT = "edit";
    public static final String DELETE = "delete";
    public static final String RESET = "reset";
    public static final String RANDOM = "random";
    public static final String QUIZ_WORD = "quiz-word";
    public static final String QUIZ_DEFINITION = "quiz-def";

    public static final String[] NAV_ORDER = {
            HOME, SEARCH, DEFINITION, HISTORY, ADD, EDIT, DELETE, RESET, RANDOM, QUIZ_WORD, QUIZ_DEFINITION
    };

    private final BootData boot;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JPanel> screens = new LinkedHashMap<>();
    private final Map<String, Refreshable> refreshables = new LinkedHashMap<>();
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();
    private final JLabel sideCountLabel = Ui.label("", 13, Font.BOLD, Color.WHITE);
    private final JLabel statusLabel = Ui.label("Sẵn sàng", 11, Font.PLAIN, Theme.MUTED);
    private final JLabel statsLabel = Ui.label("", 11, Font.PLAIN, Theme.FAINT);
    private String currentKey = HOME;
    private Timer statusTimer;

    public MainFrame(BootData boot) {
        super("Slang Dictionary · CSC13002");
        this.boot = boot;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
        setJMenuBar(buildMenuBar());

        buildScreens();
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);

        showPanel(HOME);
        updateStats();
        setStatus(boot.loadReport().message());
    }

    private void buildScreens() {
        content.setBackground(Theme.BG);
        addScreen(HOME, new HomePanel(this));
        addScreen(SEARCH, new SearchWordPanel(this));
        addScreen(DEFINITION, new DefinitionSearchPanel(this));
        addScreen(HISTORY, new HistoryPanel(this));
        addScreen(ADD, new AddPanel(this));
        addScreen(EDIT, new EditPanel(this));
        addScreen(DELETE, new DeletePanel(this));
        addScreen(RESET, new ResetPanel(this));
        addScreen(RANDOM, new RandomPanel(this));
        addScreen(QUIZ_WORD, new QuizPanel(this, QuizQuestion.Mode.WORD_TO_DEFINITION));
        addScreen(QUIZ_DEFINITION, new QuizPanel(this, QuizQuestion.Mode.DEFINITION_TO_WORD));
    }

    private void addScreen(String key, JPanel panel) {
        screens.put(key, panel);
        if (panel instanceof Refreshable refreshable) {
            refreshables.put(key, refreshable);
        }
        content.add(panel, key);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 16));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(266, 0));
        sidebar.setBorder(new EmptyBorder(22, 16, 16, 16));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.add(new LogoBadge(), BorderLayout.WEST);

        JPanel brandText = Ui.vbox();
        JLabel name = Ui.label("SLANG", 21, Font.BOLD, Color.WHITE);
        JLabel sub = Ui.label("DICTIONARY", 9, Font.BOLD, new Color(0xD4D4D4));
        JLabel course = Ui.label("CSC13002 · Lập trình ứng dụng Java", 9, Font.PLAIN, new Color(0xA3A3A3));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        course.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandText.add(name);
        brandText.add(sub);
        brandText.add(Box.createVerticalStrut(5));
        brandText.add(course);
        brand.add(brandText, BorderLayout.CENTER);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = Ui.vbox();
        nav.setBorder(new EmptyBorder(2, 0, 0, 0));
        addSection(nav, "TÌM KIẾM");
        addNav(nav, SEARCH, "Tra cứu slang word");
        addNav(nav, DEFINITION, "Tìm theo định nghĩa");
        addNav(nav, HISTORY, "Lịch sử tra cứu");
        addSection(nav, "QUẢN LÝ");
        addNav(nav, ADD, "Thêm slang word");
        addNav(nav, EDIT, "Chỉnh sửa slang word");
        addNav(nav, DELETE, "Xóa slang word");
        addNav(nav, RESET, "Khôi phục dữ liệu chuẩn");
        addSection(nav, "KHÁM PHÁ");
        addNav(nav, RANDOM, "Slang word mỗi ngày");
        addNav(nav, QUIZ_WORD, "Đố vui: đoán định nghĩa");
        addNav(nav, QUIZ_DEFINITION, "Đố vui: đoán slang word");

        JScrollPane scroll = new JScrollPane(nav);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebar.add(scroll, BorderLayout.CENTER);
        sidebar.add(buildSidebarFooter(), BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildSidebarFooter() {
        RoundedPanel card = new RoundedPanel(new Color(0x1A1A1A), 12);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(11, 12, 11, 12));
        JLabel title = Ui.label("DỮ LIỆU CỤC BỘ", 9, Font.BOLD, new Color(0xBDBDBD));
        sideCountLabel.setForeground(Color.WHITE);
        JLabel note = Ui.label("Lưu tự động · không cần mạng", 9, Font.PLAIN, new Color(0xBDBDBD));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sideCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sideCountLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(note);
        return card;
    }

    private void addSection(JPanel nav, String text) {
        JLabel label = Ui.label(text, 9, Font.BOLD, new Color(0xBDBDBD));
        label.setBorder(new EmptyBorder(16, 8, 5, 8));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(label);
    }

    private void addNav(JPanel nav, String key, String text) {
        NavButton button = new NavButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> showPanel(key));
        nav.add(button);
        nav.add(Box.createVerticalStrut(2));
        navButtons.put(key, button);
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.CARD);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(8, 18, 8, 18)));
        bar.setPreferredSize(new Dimension(0, 36));
        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(statsLabel, BorderLayout.EAST);
        return bar;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(Theme.CARD);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(2, 8, 2, 8)));

        JMenu system = menu("Hệ thống");
        JMenuItem home = item("Trang tổng quan", HOME, KeyEvent.VK_H);
        JMenuItem exit = new JMenuItem("Thoát");
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exit.addActionListener(e -> dispose());
        system.add(home);
        system.addSeparator();
        system.add(exit);

        JMenu functions = menu("Chức năng");
        functions.add(item("1. Tra cứu slang word", SEARCH, KeyEvent.VK_1));
        functions.add(item("2. Tìm theo định nghĩa", DEFINITION, KeyEvent.VK_2));
        functions.add(item("3. Lịch sử tra cứu", HISTORY, KeyEvent.VK_3));
        functions.addSeparator();
        functions.add(item("4. Thêm slang word", ADD, KeyEvent.VK_4));
        functions.add(item("5. Chỉnh sửa slang word", EDIT, KeyEvent.VK_5));
        functions.add(item("6. Xóa slang word", DELETE, KeyEvent.VK_6));
        functions.add(item("7. Khôi phục dữ liệu chuẩn", RESET, KeyEvent.VK_7));
        functions.addSeparator();
        functions.add(item("8. Slang word ngẫu nhiên", RANDOM, KeyEvent.VK_8));
        functions.add(item("9. Đố vui: đoán định nghĩa", QUIZ_WORD, KeyEvent.VK_9));
        functions.add(item("10. Đố vui: đoán slang word", QUIZ_DEFINITION, KeyEvent.VK_0));

        JMenu help = menu("Trợ giúp");
        JMenuItem about = new JMenuItem("Giới thiệu");
        about.addActionListener(e -> showAbout());
        JMenuItem guide = new JMenuItem("Hướng dẫn nhanh");
        guide.addActionListener(e -> showGuide());
        JMenuItem data = new JMenuItem("Thông tin dữ liệu");
        data.addActionListener(e -> showDataInfo());
        help.add(about);
        help.add(guide);
        help.add(data);

        bar.add(system);
        bar.add(functions);
        bar.add(help);
        return bar;
    }

    private JMenu menu(String text) {
        JMenu menu = new JMenu(text);
        menu.setFont(Theme.font(13, Font.PLAIN));
        menu.setForeground(Theme.TEXT);
        menu.setBorder(new EmptyBorder(4, 8, 4, 8));
        return menu;
    }

    private JMenuItem item(String text, String target, int keyCode) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(Theme.font(13, Font.PLAIN));
        item.setAccelerator(KeyStroke.getKeyStroke(keyCode, InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(e -> showPanel(target));
        return item;
    }

    private void showAbout() {
        String message = "<html><div style='width:480px'>"
                + "<h2 style='margin:0'>Slang Dictionary</h2>"
                + "<p style='color:#525252'>Đồ án #1 – Slang Word · CSC13002 – Lập trình ứng dụng Java</p>"
                + "<p>Ứng dụng minh hoạ <b>Java IO</b>, <b>OOP</b>, <b>Generic</b> và <b>Collections</b> "
                + "thông qua một từ điển slang word.</p>"
                + "<p><b>Cấu trúc dữ liệu:</b> HashMap cho tra cứu từ khoá O(1), chỉ mục nghịch đảo "
                + "trigram cho tìm theo định nghĩa, snapshot nhị phân để không đánh chỉ mục lại.</p>"
                + "<p style='color:#525252'>Dữ liệu hiện tại: " + Ui.formatInt(dictionary().size())
                + " mục từ · " + Ui.formatInt(dictionary().definitionCount()) + " định nghĩa.</p>"
                + "</div></html>";
        Dialogs.info(this, "Giới thiệu", message);
    }

    private void showGuide() {
        String message = "<html><div style='width:500px'>"
                + "<b>Tra cứu</b><br>Dùng ô tìm kiếm ở chức năng 1 hoặc 2. Nhấn Enter để tìm nhanh.<br><br>"
                + "<b>Quản lý dữ liệu</b><br>Thêm / sửa / xóa đều được lưu ngay vào <code>data/slang_modified.txt</code> "
                + "và ảnh chụp chỉ mục <code>data/dictionary.bin</code>.<br><br>"
                + "<b>Trùng từ</b><br>Khi thêm một từ đã tồn tại, chọn Ghi đè hoặc Thêm bản trùng.<br><br>"
                + "<b>Đố vui</b><br>Mỗi câu có 4 đáp án. Đáp án đúng có viền đậm, sai có viền nét đứt; điểm được giữ trong phiên.<br><br>"
                + "<b>Phím tắt</b><br>Ctrl+1 … Ctrl+0 mở nhanh 10 chức năng."
                + "</div></html>";
        Dialogs.info(this, "Hướng dẫn nhanh", message);
    }

    private void showDataInfo() {
        DictionaryStore store = store();
        String message = "<html><div style='width:560px'>"
                + "<b>File gốc (raw):</b> " + store.getRawFile() + "<br>"
                + "<b>File chuẩn hoá:</b> " + store.getCleanFile() + "<br>"
                + "<b>File đang dùng:</b> " + boot.loadReport().sourceFile() + "<br>"
                + "<b>Snapshot chỉ mục:</b> " + store.getSnapshotFile() + "<br>"
                + "<b>Lịch sử:</b> " + store.getDataDir().resolve("history.txt") + "<br><br>"
                + "<b>Trạng thái nạp:</b> " + boot.loadReport().message() + "<br>"
                + "<b>Số mục từ:</b> " + Ui.formatInt(dictionary().size()) + "<br>"
                + "<b>Số định nghĩa:</b> " + Ui.formatInt(dictionary().definitionCount())
                + "</div></html>";
        Dialogs.info(this, "Thông tin dữ liệu", message);
    }

    public void showPanel(String key) {
        if (!screens.containsKey(key)) {
            return;
        }
        currentKey = key;
        cardLayout.show(content, key);
        navButtons.forEach((name, button) -> button.setActive(name.equals(key)));
        Refreshable refreshable = refreshables.get(key);
        if (refreshable != null) {
            refreshable.onShown();
        }
        updateStats();
    }

    /** Chuẩn bị dữ liệu mẫu cho ảnh chụp kiểm thử. */
    public void prepareDemo() {
        JPanel panel = screens.get(currentKey);
        if (panel instanceof DemoScreen demo) {
            demo.prepareDemo();
        }
    }

    /** Chọn một đáp án sai cho ảnh minh hoạ trạng thái quiz. */
    public void answerDemoQuizWrong() {
        JPanel panel = screens.get(currentKey);
        if (panel instanceof QuizPanel quiz) {
            quiz.demoAnswerWrong();
        }
    }

    /** Hiện gợi ý autocomplete cho ảnh chụp (không chạy tìm kiếm). */
    public void prepareSuggestionDemo() {
        JPanel panel = screens.get(currentKey);
        if (panel instanceof SearchWordPanel search) {
            search.prepareSuggestionDemo();
        } else if (panel instanceof DefinitionSearchPanel definition) {
            definition.prepareSuggestionDemo();
        }
    }

    @Override
    public SlangDictionary dictionary() {
        return boot.dictionary();
    }

    @Override
    public DictionaryStore store() {
        return boot.store();
    }

    @Override
    public HistoryService history() {
        return boot.history();
    }

    @Override
    public DictionaryStore.LoadReport loadReport() {
        return boot.loadReport();
    }

    @Override
    public void navigate(String key) {
        showPanel(key);
    }

    @Override
    public void setStatus(String message) {
        statusLabel.setText(message == null || message.isBlank() ? "Sẵn sàng" : message);
        statusLabel.setForeground(Theme.SUCCESS);
        if (statusTimer != null) {
            statusTimer.stop();
        }
        statusTimer = new Timer(5000, e -> {
            statusLabel.setText("Sẵn sàng");
            statusLabel.setForeground(Theme.MUTED);
        });
        statusTimer.setRepeats(false);
        statusTimer.start();
    }

    @Override
    public void notifyDataChanged() {
        updateStats();
        Refreshable current = refreshables.get(currentKey);
        if (current != null) {
            current.onDataChanged();
        }
        if (!HOME.equals(currentKey)) {
            Refreshable home = refreshables.get(HOME);
            if (home != null) {
                home.onDataChanged();
            }
        }
    }

    private void updateStats() {
        int entries = dictionary().size();
        int definitions = dictionary().definitionCount();
        int unique = dictionary().uniqueWordCount();
        sideCountLabel.setText(Ui.formatInt(entries) + " mục từ");
        statsLabel.setText(Ui.formatInt(entries) + " mục · "
                + Ui.formatInt(unique) + " từ duy nhất · "
                + Ui.formatInt(definitions) + " định nghĩa · "
                + (boot.loadReport().indexLoaded() ? "chỉ mục từ đĩa" : "chỉ mục xây mới"));
    }

    private static java.awt.Image createAppIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, new Color(0x111111), 64, 64, new Color(0x4A4A4A)));
            g.fillRoundRect(0, 0, 64, 64, 16, 16);
            g.setColor(Color.WHITE);
            g.setFont(Theme.font(36, Font.BOLD));
            java.awt.FontMetrics metrics = g.getFontMetrics();
            String text = "S";
            g.drawString(text, (64 - metrics.stringWidth(text)) / 2,
                    (64 + metrics.getAscent() - metrics.getDescent()) / 2);
        } finally {
            g.dispose();
        }
        return image;
    }

    /** Logo nhỏ vẽ bằng Java2D. */
    private static final class LogoBadge extends JPanel {

        LogoBadge() {
            setOpaque(false);
            setPreferredSize(new Dimension(44, 44));
            setMaximumSize(new Dimension(44, 44));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new GradientPaint(0, 0, new Color(0x111111), getWidth(), getHeight(),
                        new Color(0x4A4A4A)));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
                g.setColor(Color.WHITE);
                g.setFont(Theme.font(22, Font.BOLD));
                java.awt.FontMetrics metrics = g.getFontMetrics();
                g.drawString("S", (getWidth() - metrics.stringWidth("S")) / 2,
                        (getHeight() + metrics.getAscent() - metrics.getDescent()) / 2);
            } finally {
                g.dispose();
            }
        }
    }
}
