package cn.oyzh.fx.editor.test;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.RegexHelper;
import cn.oyzh.common.util.ResourceUtil;
import javafx.scene.control.Button;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.*;
import org.fxmisc.richtext.model.*;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class CorrectSyntaxHighlighter {
    private final VirtualizedScrollPane<InlineCssTextArea> virtualScrollPane;
    private final InlineCssTextArea textArea;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ScheduledFuture<?> parseDelayFuture;

    // 样式常量（复用）
    // private static final String NORMAL = "normal";
    // private static final String JSON_KEY = "json-key";
    // private static final String JSON_STRING = "json-string";
    // private static final String JSON_NUMBER = "json-number";
    private static final String ERROR = "-fx-fill: #c2c2c2;";

    /**
     * 键样式
     */
    public static final String NORMAL = "-fx-fill: #1232AC;";

    /**
     * 值样式
     */
    public static final String JSON_STRING = "-fx-fill: #95261F;";

    /**
     * 基础样式
     */
    public static final String JSON_KEY = "-fx-fill: #800000;";

    /**
     * 注释样式
     */
    public static final String JSON_NUMBER = "-fx-fill: #95261F;";

    public CorrectSyntaxHighlighter() {
        // 初始化虚拟滚动文本区域
        textArea = new InlineCssTextArea();
        virtualScrollPane = new VirtualizedScrollPane<>(textArea);

        // 性能优化配置
        textArea.setWrapText(false);
        textArea.setCache(true);
        textArea.setCacheHint(javafx.scene.CacheHint.SPEED);

        // 文本变化监听（延迟解析）
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            if (parseDelayFuture != null && !parseDelayFuture.isDone()) {
                parseDelayFuture.cancel(false);
            }
            // 延迟300ms执行，避免输入抖动
            parseDelayFuture = Executors.newSingleThreadScheduledExecutor().schedule(
                    this::analyzeAndUpdateStyles, 300, TimeUnit.MILLISECONDS
            );
        });
    }

    public VirtualizedScrollPane<InlineCssTextArea> getScrollPane() {
        return virtualScrollPane;
    }

    private void analyzeAndUpdateStyles() {
        String text = textArea.getText();
        if (text.isEmpty()) return;

        try {
            // 计算样式（使用正确的合并逻辑）
            // StyleSpans<String> styleSpans = computeStyleSpans(text);
            // StyleSpans<String> styleSpans = computeStyleSpans1(text);
            //
            // // 主线程更新样式
            // Platform.runLater(() -> {
            //
            //
            //     textArea.setStyleSpans(0, styleSpans);
            // });
            Platform.runLater(() -> {
                computeStyleSpans2(text);
            });
        } catch (Throwable e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                StyleSpansBuilder<String> builder = new StyleSpansBuilder<>();
                builder.add(ERROR, text.length());
                textArea.setStyleSpans(0, builder.create());
            });
        }
    }

    // 正确的样式计算与合并逻辑
    private StyleSpans<String> computeStyleSpans(String text) {
        StyleSpansBuilder<String> builder = new StyleSpansBuilder<>();
        Pattern pattern = Pattern.compile("(\"[^\"]+\")\\s*:|\"[^\"]+\"|\\d+|([{}])");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        int lastIdx = 0;
        String lastStyle = NORMAL.toString();
        int lastLength = 0;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // 处理上次匹配到当前匹配之间的文本
            if (start > lastIdx) {
                // 检查是否可以与上一个样式合并
                String currentStyle = NORMAL.toString();
                if (currentStyle.equals(lastStyle)) {
                    lastLength += start - lastIdx;
                } else {
                    // 无法合并，添加之前的样式
                    if (lastLength > 0) {
                        builder.add(lastStyle, lastLength);
                    }
                    lastStyle = currentStyle;
                    lastLength = start - lastIdx;
                }
            }

            // 处理当前匹配到的文本
            String currentStyle = getStyleForMatch(matcher);
            if (currentStyle.equals(lastStyle)) {
                lastLength += end - start;
            } else {
                if (lastLength > 0) {
                    builder.add(lastStyle, lastLength);
                }
                lastStyle = currentStyle;
                lastLength = end - start;
            }

            lastIdx = end;
        }

        // 添加剩余文本
        if (lastIdx < text.length()) {
            String currentStyle = NORMAL;
            if (currentStyle.equals(lastStyle)) {
                lastLength += text.length() - lastIdx;
            } else {
                if (lastLength > 0) {
                    builder.add(lastStyle, lastLength);
                }
                lastStyle = currentStyle;
                lastLength = text.length() - lastIdx;
            }
        }

        // 添加最后一个样式片段
        if (lastLength > 0) {
            builder.add(lastStyle, lastLength);
        }

        return builder.create();
    }

    private StyleSpans<String> computeStyleSpans1(String text) {
        StyleSpansBuilder<String> builder = new StyleSpansBuilder<>();
        Pattern pattern = RegexHelper.jsonValuePattern();
        java.util.regex.Matcher matcher = pattern.matcher(text);
        // Pattern pattern = Pattern.compile("(\"[^\"]+\")\\s*:|\"[^\"]+\"|\\d+|([{}])");
        // java.util.regex.Matcher matcher = pattern.matcher(text);
        int lastIndex = -1;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String group = matcher.group();
            System.out.println("1---------------" + start);
            System.out.println("2---------------" + end);
            System.out.println("4---------------" + group);
            builder.add(JSON_STRING, end - start);
            if (lastIndex != -1) {
                builder.add(ERROR, start - lastIndex);
            }
            lastIndex = end;
            // builder.add(new StyleSpan<>(JSON_STRING, start, group.length()));
        }
        Pattern pattern1 = RegexHelper.jsonSymbolPattern();
        java.util.regex.Matcher matcher1 = pattern1.matcher(text);
        while (matcher1.find()) {
            int start = matcher1.start();
            int end = matcher1.end();
            String group = matcher1.group();
            System.out.println("1---------------" + start);
            System.out.println("2---------------" + end);
            System.out.println("4---------------" + group);
            // builder.add(ERROR, 4);
            // builder.add(new StyleSpan<>(JSON_NUMBER, start, group.length()));
        }
        System.out.println("2---------------");
        return builder.create();
    }

    private void computeStyleSpans2(String text) {
        Pattern pattern = RegexHelper.jsonValuePattern();
        java.util.regex.Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            textArea.setStyle(start, end, JSON_STRING);
            System.out.println("-----------------1");
        }
        Pattern pattern1 = RegexHelper.jsonSymbolPattern();
        java.util.regex.Matcher matcher1 = pattern1.matcher(text);
        while (matcher1.find()) {
            int start = matcher1.start();
            int end = matcher1.end();
            textArea.setStyle(start, end, JSON_NUMBER);
            System.out.println("-----------------2");
        }
    }

    private String getStyleForMatch(java.util.regex.Matcher matcher) {
        if (matcher.group(1) != null) return JSON_KEY;
        if (matcher.group(2) != null) return NORMAL;
        if (matcher.group().startsWith("\"")) return JSON_STRING;
        if (matcher.group().matches("\\d+")) return JSON_NUMBER;
        return NORMAL;
    }

    public void shutdown() {
        executor.shutdown();
        if (parseDelayFuture != null) parseDelayFuture.cancel(false);
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(App.class);
    }

    public static class App extends javafx.application.Application {
        @Override
        public void start(javafx.stage.Stage stage) {
            InputStream stream = ResourceUtil.getResourceAsStream("test.json");
            byte[] bytes = IOUtil.readBytes(stream);
            CorrectSyntaxHighlighter highlighter = new CorrectSyntaxHighlighter();
            highlighter.getScrollPane().getContent().replaceText(new String(bytes));
            // highlighter.analyzeAndUpdateStyles();
            stage.setScene(new javafx.scene.Scene(highlighter.getScrollPane(), 800, 600));
            stage.setTitle("优化后的RichtextFX语法高亮");
            stage.show();
        }
    }
}
