package cn.oyzh.fx.editor.incubator;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.gui.text.field.HighlightTextField;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.theme.ThemeUtil;
import com.alibaba.fastjson2.JSONObject;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * 编辑器工具类
 *
 * @author oyzh
 * @since 2025-09-24
 */
public class EditorUtil {

    //    /**
    //     * 安装输入法支持
    //     * TODO: 由于官方已经支持了输入法，此方法废弃
    //     *
    //     * @param editor 编辑器
    //     */
    //    @Deprecated
    //    public static void setupIMESupport(Editor editor) {
    //        // 上一次未提交位置记录
    //        AtomicReference<Integer> lastComposedEnd = new AtomicReference<>();
    //        AtomicReference<Integer> lastComposedStart = new AtomicReference<>();
    //        // 输入法文本变更事件
    //        editor.addEventFilter(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, event -> {
    //            // 删除选中内容
    //            if (editor.isSelectedText()) {
    //                SelectionSegment segment = editor.getSelection();
    //                editor.deleteText(segment.getMin(), segment.getMax());
    //            }
    //            // 删除未提交内容
    //            Integer end = lastComposedEnd.get();
    //            Integer start = lastComposedStart.get();
    //            if (start != null && end != null) {
    //                editor.deleteText(start, end);
    //            }
    //            TextPos textPos = editor.getCaretPosition();
    //            int caretPosition = editor.caretPosition();
    //            // 如果已经提交的文本不为空，则插入到当前位置
    //            String committed = event.getCommitted();
    //            ObservableList<InputMethodTextRun> composed = event.getComposed();
    //            if (StringUtil.isNotEmpty(committed)) {
    //                StyledInput input = StyledInput.of(committed);
    //                // 插入内容
    //                editor.insertText(textPos, input);
    //                int caretEnd = caretPosition + committed.length();
    //                // 移动光标到插入位置后
    //                editor.positionCaret(caretEnd);
    //                // 清除未提交内容位置
    //                lastComposedEnd.set(null);
    //                lastComposedStart.set(null);
    //            } else if (CollectionUtil.isNotEmpty(event.getComposed())) {
    //                StringBuilder builder = new StringBuilder();
    //                for (InputMethodTextRun run : composed) {
    //                    builder.append(run.getText());
    //                }
    //                StyledInput input = StyledInput.of(builder.toString());
    //                // 插入内容
    //                editor.insertText(textPos, input);
    //                int caretEnd = caretPosition + builder.length();
    //                // 移动光标到插入位置后
    //                editor.positionCaret(caretEnd);
    //                // 记录未提交内容位置
    //                lastComposedEnd.set(caretEnd);
    //                lastComposedStart.set(caretPosition);
    //            }
    //            event.consume();
    //        });
    //        // 输入法请求
    //        editor.setInputMethodRequests(new InputMethodRequests() {
    //            @Override
    //            public Point2D getTextLocation(int offset) {
    //                Optional<Bounds> bounds = editor.getCaretBounds();
    //                // 返回光标位置，以便输入法窗口可以正确显示
    //                return bounds.map(value -> new Point2D(value.getMinX(), value.getMinY())).orElseGet(() -> new Point2D(0, 0));
    //            }
    //
    //            @Override
    //            public int getLocationOffset(int x, int y) {
    //                return 0;
    //            }
    //
    //            @Override
    //            public void cancelLatestCommittedText() {
    //            }
    //
    //            @Override
    //            public String getSelectedText() {
    //                return editor.getSelectedText();
    //            }
    //        });
    //    }

    /**
     * 绑定高亮组件
     *
     * @param editor 编辑器
     * @param field  高亮文本组件
     */
    public static void bindHighlight(Editor editor, HighlightTextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            editor.setHighlight(newValue);
        });
        field.regexPropery().addListener((observable, oldValue, newValue) -> {
            editor.setHighlightRegex(newValue);
        });
        field.wholeWordPropery().addListener((observable, oldValue, newValue) -> {
            editor.setHighlightWholeWord(newValue);
        });
        field.matchCasePropery().addListener((observable, oldValue, newValue) -> {
            editor.setHighlightMacthCase(newValue);
        });
    }

    //    /**
    //     * 高亮搜索索引名称
    //     */
    //    private final static String HIGHLIGHT_SEARCH_INDEX = "_highlight_search_index";

    //    /**
    //     * 清除高亮搜索索引
    //     *
    //     * @param editor 编辑器
    //     */
    //    public static void clearHighlightSearchIndex(Editor editor) {
    //        editor.setProp(HIGHLIGHT_SEARCH_INDEX, 0);
    //    }

    /**
     * 搜索下一个高亮
     *
     * @param editor 编辑器
     * @param field  高亮文本组件
     */
    public static void searchNextHighlight(Editor editor, HighlightTextField field) {
        try {
            //            searchIndex = editor.hasProp(HIGHLIGHT_SEARCH_INDEX) ? editor.getProp(HIGHLIGHT_SEARCH_INDEX) : 0;
            String filterText = field.getText();
            if (StringUtil.isBlank(filterText)) {
                return;
            }
            int searchIndex = editor.caretPosition();
            String text = editor.getText();
            if (searchIndex >= text.length()) {
                searchIndex = 0;
            }
            // 搜索高亮
            TextUtil.MatchText matchText = searchHighlight(text, field, searchIndex);
            // 无效则返回
            if (matchText == TextUtil.MatchText.INVALID) {
                //                searchIndex = 0;
                return;
            }
            // 如果未找到，可能是到尾了，再从头执行一次搜索
            if (matchText == TextUtil.MatchText.NOT_FOUND) {
                searchIndex = 0;
                matchText = searchHighlight(text, field, searchIndex);
                if (matchText == TextUtil.MatchText.NOT_FOUND) {
                    return;
                }
            }
            //            searchIndex = matchText.index() + matchText.text().length();
            editor.selectRange(matchText.index(), matchText.index() + matchText.text().length());
        } catch (Exception ex) {
            ex.printStackTrace();
            //        } finally {
            //            editor.setProp(HIGHLIGHT_SEARCH_INDEX, searchIndex);
        }
    }

    /**
     * 搜索高亮
     *
     * @param text        文本
     * @param field       高亮文本组件
     * @param searchIndex 搜索起始索引
     * @return 匹配文本
     */
    private static TextUtil.MatchText searchHighlight(String text, HighlightTextField field, int searchIndex) {
        String filterText = field.getText();
        if (StringUtil.isBlank(filterText)) {
            return TextUtil.MatchText.INVALID;
        }
        return TextUtil.findText(text,
                filterText,
                searchIndex,
                field.isMatchCase(),
                field.isWholeWord(),
                field.isRegex());
    }

    /**
     * 主题资源目录
     */
    private static final String THEME_DIR = "/tm4javafx/themes/";

    /**
     * 主题资源目录（jar内路径）
     */
    private static final String THEME_DIR_IN_JAR = "tm4javafx/themes/";

    /**
     * 暗色兜底主题
     */
    private static final String FALLBACK_DARK_THEME = "dark-plus.json";

    /**
     * 亮色兜底主题
     */
    private static final String FALLBACK_LIGHT_THEME = "light-plus.json";

    /**
     * 主题资源解析缓存
     */
    private static final Map<String, String> THEME_RESOURCE_CACHE = new ConcurrentHashMap<>();

    /**
     * 主题候选列表
     */
    private static volatile List<ThemeCandidate> themeCandidates;

    /**
     * 解析编辑器主题资源。
     * <p>
     * 根据主题的明暗模式严格筛选候选主题，再按前景色、背景色、强调色的相似度选择最接近的主题。
     *
     * @param style 主题风格
     * @return 主题资源路径
     */
    public static String resolveThemeResource(ThemeStyle style) {
        if (style == null) {
            boolean darkMode = ThemeManager.isDarkMode();
            return THEME_DIR + (darkMode ? FALLBACK_DARK_THEME : FALLBACK_LIGHT_THEME);
        }
        boolean darkMode = style.isDarkMode();
        String cacheKey = style.getName() + "#" + darkMode;
        String cached = THEME_RESOURCE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        ThemeCandidate best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (ThemeCandidate candidate : themeCandidates()) {
            // 严格区分暗色模式和亮色模式，禁止跨模式匹配
            if (candidate.dark() != darkMode) {
                continue;
            }
            double score = similarity(style, candidate);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        String resource = THEME_DIR + (best == null
                ? (darkMode ? FALLBACK_DARK_THEME : FALLBACK_LIGHT_THEME)
                : best.resource());
        THEME_RESOURCE_CACHE.put(cacheKey, resource);
        return resource;
    }

    /**
     * 计算主题与候选主题的相似度
     *
     * @param style     主题风格
     * @param candidate 候选主题
     * @return 相似度，越大越接近
     */
    private static double similarity(ThemeStyle style, ThemeCandidate candidate) {
        double score = 0;
        double weight = 0;
        // 背景色权重最高，编辑器主体观感主要由背景色决定
        double background = ThemeUtil.calcCorr(style.getBackgroundColor(), candidate.background());
        if (background >= 0) {
            score += background * 6;
            weight += 6;
        }
        double foreground = ThemeUtil.calcCorr(style.getForegroundColor(), candidate.foreground());
        if (foreground >= 0) {
            score += foreground * 3;
            weight += 3;
        }
        double accent = ThemeUtil.calcCorr(style.getAccentColor(), candidate.accent());
        if (accent >= 0) {
            score += accent;
            weight += 1;
        }
        if (weight == 0) {
            return -1;
        }
        // 名称相似度用于同色系主题之间的微调
        return score / weight + nameSimilarity(style.getName(), candidate.name()) * 0.08;
    }

    /**
     * 计算名称相似度
     *
     * @param name1 名称1
     * @param name2 名称2
     * @return 相似度
     */
    private static double nameSimilarity(String name1, String name2) {
        Set<String> tokens1 = nameTokens(name1);
        Set<String> tokens2 = nameTokens(name2);
        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0;
        }
        int common = 0;
        for (String token : tokens1) {
            if (tokens2.contains(token)) {
                common++;
            }
        }
        return common / (double) Math.min(tokens1.size(), tokens2.size());
    }

    /**
     * 提取名称中的有效词元
     *
     * @param name 名称
     * @return 词元集合
     */
    private static Set<String> nameTokens(String name) {
        Set<String> tokens = new HashSet<>();
        if (StringUtil.isBlank(name)) {
            return tokens;
        }
        for (String token : name.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (!token.isEmpty() && !"dark".equals(token) && !"light".equals(token) && !"theme".equals(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * 获取主题候选列表
     *
     * @return 主题候选列表
     */
    private static List<ThemeCandidate> themeCandidates() {
        List<ThemeCandidate> candidates = themeCandidates;
        if (candidates == null) {
            synchronized (EditorUtil.class) {
                candidates = themeCandidates;
                if (candidates == null) {
                    candidates = loadThemeCandidates();
                    themeCandidates = candidates;
                }
            }
        }
        return candidates;
    }

    /**
     * 加载主题候选列表
     *
     * @return 主题候选列表
     */
    private static List<ThemeCandidate> loadThemeCandidates() {
        List<ThemeCandidate> candidates = new ArrayList<>();
        for (String file : listThemeFiles()) {
            try (InputStream stream = EditorUtil.class.getResourceAsStream(THEME_DIR + file)) {
                if (stream == null) {
                    continue;
                }
                String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                var json = JSONUtil.parseObject(text);
                String type = json.getString("type");
                boolean dark = "dark".equalsIgnoreCase(type);
                if (!dark && !"light".equalsIgnoreCase(type)) {
                    continue;
                }
                var colors = json.getJSONObject("colors");
                if (colors == null) {
                    continue;
                }
                Color background = parseThemeColor(colors.getString("editor.background"));
                if (background == null) {
                    continue;
                }
                Color foreground = firstThemeColor(colors, "editor.foreground", "foreground", "terminal.foreground");
                Color accent = firstThemeColor(colors,
                        "activityBarBadge.background",
                        "textLink.foreground",
                        "button.background",
                        "editorCursor.foreground",
                        "focusBorder",
                        "statusBarItem.remoteBackground");
                candidates.add(new ThemeCandidate(file, json.getString("name"), dark, background, foreground, accent));
            } catch (Exception ex) {
                JulLog.warn("load theme candidate failed: " + file, ex);
            }
        }
        return candidates;
    }

    /**
     * 获取主题文件列表，同时兼容目录和jar两种运行方式
     *
     * @return 主题文件列表
     */
    private static List<String> listThemeFiles() {
        List<String> files = new ArrayList<>();
        try {
            URL directory = EditorUtil.class.getResource(THEME_DIR);
            // 打包为jar时目录entry可能不存在，用兜底主题文件反推jar
            URL sample = EditorUtil.class.getResource(THEME_DIR + FALLBACK_DARK_THEME);
            URL url = directory != null ? directory : sample;
            if (url == null) {
                return files;
            }
            if ("file".equals(url.getProtocol()) && directory != null) {
                try (Stream<Path> stream = Files.list(Paths.get(directory.toURI()))) {
                    stream.filter(Files::isRegularFile)
                            .map(path -> path.getFileName().toString())
                            .filter(name -> name.endsWith(".json"))
                            .forEach(files::add);
                }
            } else if ("jar".equals(url.getProtocol())) {
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                try (JarFile jarFile = connection.getJarFile()) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        String entry = entries.nextElement().getName();
                        if (entry.startsWith(THEME_DIR_IN_JAR)
                                && entry.endsWith(".json")
                                && entry.indexOf('/', THEME_DIR_IN_JAR.length()) < 0) {
                            files.add(entry.substring(THEME_DIR_IN_JAR.length()));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            JulLog.warn("list theme files failed", ex);
        }
        Collections.sort(files);
        return files;
    }

    /**
     * 获取第一个可解析的颜色
     *
     * @param colors 颜色配置
     * @param keys   键
     * @return 颜色
     */
    private static Color firstThemeColor(JSONObject colors, String... keys) {
        for (String key : keys) {
            Color color = parseThemeColor(colors.getString(key));
            if (color != null) {
                return color;
            }
        }
        return null;
    }

    /**
     * 解析主题颜色，忽略透明通道
     *
     * @param value 颜色值
     * @return 颜色
     */
    private static Color parseThemeColor(String value) {
        if (StringUtil.isBlank(value)) {
            return null;
        }
        String color = value.trim();
        if (!color.startsWith("#")) {
            try {
                return Color.web(color);
            } catch (Exception ex) {
                return null;
            }
        }
        String digits = color.substring(1);
        // #rgb / #rgba 展开为 #rrggbb
        if (digits.length() == 3 || digits.length() == 4) {
            StringBuilder expanded = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                expanded.append(digits.charAt(i)).append(digits.charAt(i));
            }
            digits = expanded.toString();
        }
        // 忽略 alpha 通道
        if (digits.length() == 8) {
            digits = digits.substring(0, 6);
        }
        if (digits.length() != 6) {
            return null;
        }
        try {
            return Color.web("#" + digits);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 主题候选
     *
     * @param resource   资源名称
     * @param name       主题名称
     * @param dark       是否暗色主题
     * @param background 背景色
     * @param foreground 前景色
     * @param accent     强调色
     */
    private record ThemeCandidate(String resource, String name, boolean dark, Color background, Color foreground,
                                  Color accent) {
    }
}
