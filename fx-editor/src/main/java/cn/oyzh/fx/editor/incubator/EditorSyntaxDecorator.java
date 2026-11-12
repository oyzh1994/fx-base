package cn.oyzh.fx.editor.incubator;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tm4javafx.richtext.StatelessSyntaxDecorator;
import tm4javafx.richtext.StyleProvider;
import tm4javafx.richtext.StyledToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编辑器语法装饰器，负责语法着色、提示词与搜索高亮
 *
 * @author oyzh
 * @since 2025-08-14
 */
public class EditorSyntaxDecorator extends StatelessSyntaxDecorator {

    /**
     * 高亮
     */
    private volatile String highlight;

    /**
     * 高亮，正则模式
     */
    private volatile boolean highlightRegex;

    /**
     * 高亮，全字匹配
     */
    private volatile boolean highlightWholeWord;

    /**
     * 高亮，匹配大小写
     */
    private volatile boolean highlightMatchCase;

    /**
     * 提示词
     */
    private volatile Set<String> prompts;

    /**
     * 格式类型
     */
    private volatile EditorFormatType formatType = EditorFormatType.RAW;

    /**
     * 构造语法装饰器
     */
    public EditorSyntaxDecorator() {
        this(null);
    }

    /**
     * 构造语法装饰器
     *
     * @param styleProvider 样式提供者
     */
    public EditorSyntaxDecorator(@Nullable StyleProvider styleProvider) {
        super(styleProvider);
    }

    /**
     * 维护自身的段落缓存，用于异步更新
     */
    private volatile List<RichParagraph> myParagraphs = List.of();

    /**
     * 当前文档文本长度（含换行符），用于选择同步或异步着色。
     */
    private long documentChars;

    /**
     * 样式版本号，用于丢弃过期的后台任务结果
     */
    private final AtomicInteger styleVersion = new AtomicInteger(0);

    /**
     * 后台语法高亮线程池。
     * <p>
     * 所有编辑器共享同一个线程池：每个编辑器单独建线程在消息列表等场景下会创建大量线程，
     * 且组件销毁后线程无法回收。线程池中的线程为守护线程，空闲一段时间会自动回收。
     */
    private static final ExecutorService STYLE_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(1, Math.min(2, Runtime.getRuntime().availableProcessors() - 1)), r -> {
        Thread t = new Thread(r, "editor-syntax-highlighter");
        t.setDaemon(true);
        return t;
    });

    /**
     * 着色任务防抖调度器。
     */
    private static final ScheduledExecutorService STYLE_SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "editor-syntax-scheduler");
        t.setDaemon(true);
        return t;
    });

    /**
     * 着色防抖时间，连续输入时只保留最后一次完整着色任务。
     */
    private static final long STYLE_DEBOUNCE_MILLIS = 80L;

    /**
     * 当前编辑器等待执行的着色任务。
     */
    private ScheduledFuture<?> pendingStyleTask;

    /**
     * 当前编辑器正在执行的着色任务。
     */
    private Future<?> runningStyleTask;

    /**
     * 高亮匹配模式
     */
    private Pattern highlightPattern;

    /**
     * 设置高亮。
     *
     * @param highlight 高亮
     */
    public void setHighlight(String highlight) {
        this.highlight = highlight;
        this.initHighlightPattern();
    }

    /**
     * 获取高亮。
     *
     * @return 高亮
     */
    public String getHighlight() {
        return highlight;
    }

    /**
     * 设置高亮正则。
     *
     * @param highlightRegex 高亮正则
     */
    public void setHighlightRegex(boolean highlightRegex) {
        this.highlightRegex = highlightRegex;
        this.initHighlightPattern();
    }

    /**
     * 是否高亮正则。
     *
     * @return 高亮正则
     */
    public boolean isHighlightRegex() {
        return highlightRegex;
    }

    /**
     * 设置高亮全词。
     *
     * @param highlightWholeWord 高亮全词
     */
    public void setHighlightWholeWord(boolean highlightWholeWord) {
        this.highlightWholeWord = highlightWholeWord;
        this.initHighlightPattern();
    }

    /**
     * 是否高亮全词。
     *
     * @return 高亮全词
     */
    public boolean isHighlightWholeWord() {
        return highlightWholeWord;
    }

    /**
     * 设置高亮匹配大小写。
     *
     * @param highlightMatchCase 高亮匹配大小写
     */
    public void setHighlightMatchCase(boolean highlightMatchCase) {
        this.highlightMatchCase = highlightMatchCase;
        this.initHighlightPattern();
    }

    /**
     * 是否高亮匹配大小写。
     *
     * @return 高亮匹配大小写
     */
    public boolean isHighlightMatchCase() {
        return highlightMatchCase;
    }

    /**
     * 初始化高亮匹配模式
     */
    private void initHighlightPattern() {
        if (StringUtil.isNotEmpty(this.highlight)) {
            try {
                this.highlightPattern = RegexUtil.createSearchPattern(this.highlight,
                        this.highlightMatchCase,
                        this.highlightWholeWord,
                        this.highlightRegex);
            } catch (Exception ignore) {
            }
        } else {
            this.highlightPattern = null;
        }
    }

    /**
     * 获取提示。
     *
     * @return 提示
     */
    public Set<String> getPrompts() {
        return prompts;
    }

    /**
     * 提示词匹配模式
     */
    private Pattern promptsPattern;

    /**
     * 设置提示词
     *
     * @param prompts 提示词集合
     */
    public void setPrompts(Set<String> prompts) {
        this.prompts = prompts;
        if (CollectionUtil.isNotEmpty(prompts)) {
            try {
                String pattern = "\\b(" + String.join("|", this.prompts) + ")\\b";
                this.promptsPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } catch (Exception ignore) {
            }
        } else {
            this.promptsPattern = null;
        }
    }

    /**
     * 获取格式类型。
     *
     * @return 格式类型
     */
    public EditorFormatType getFormatType() {
        return formatType;
    }

    /**
     * 设置格式类型。
     *
     * @param formatType 格式类型
     */
    public void setFormatType(EditorFormatType formatType) {
        this.formatType = formatType;
    }

    /**
     * 重新构建当前文本的样式并刷新视图。
     *
     * @param model 文本模型
     */
    public void refreshText(CodeTextModel model) {
        this.handleChange(model, TextPos.ZERO, model.getDocumentEnd(), 0, 0, 0);
    }

    /**
     * 高亮颜色
     */
    private volatile Color highlightColor = Editor.DEFAULT_HIGHLIGHT_COLOR;

    /**
     * 获取高亮颜色。
     *
     * @return 高亮颜色
     */
    public Color getHighlightColor() {
        return highlightColor;
    }

    /**
     * 设置高亮颜色。
     *
     * @param highlightColor 高亮颜色
     */
    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    //    /**
    //     * 高亮样式
    //     */
    //    private StyleAttributeMap highlightStyle;
    //
    //    public StyleAttributeMap highlightStyle() {
    //        if (this.highlightStyle == null) {
    //            this.highlightStyle = StyleAttributeMap.of(StyleAttributeMap.TEXT_COLOR, highlightColor);
    //        }
    //        return this.highlightStyle;
    //    }

    /**
     * 提示词颜色
     */
    private volatile Color promptsColor = Editor.DEFAULT_PROMPTS_COLOR;

    /**
     * 设置提示颜色。
     *
     * @param promptsColor 提示颜色
     */
    public void setPromptsColor(Color promptsColor) {
        this.promptsColor = promptsColor;
        this.promptsStyle = null;
    }

    /**
     * 获取提示颜色。
     *
     * @return 提示颜色
     */
    public Color getPromptsColor() {
        return promptsColor;
    }

    /**
     * 提示词样式
     */
    private StyleAttributeMap promptsStyle;

    /**
     * 获取提示词样式
     *
     * @return 提示词样式
     */
    public StyleAttributeMap promptsStyle() {
        if (this.promptsStyle == null) {
            this.promptsStyle = StyleAttributeMap.of(StyleAttributeMap.TEXT_COLOR, promptsColor);
        }
        return this.promptsStyle;
    }

    /**
     * 着色策略
     */
    private EditorSyntaxStrategy syntaxStrategy = EditorSyntaxStrategy.AUTO;

    /**
     * 获取语法策略。
     *
     * @return 语法策略
     */
    public EditorSyntaxStrategy getSyntaxStrategy() {
        return syntaxStrategy;
    }

    /**
     * 设置语法策略。
     *
     * @param syntaxStrategy 语法策略
     */
    public void setSyntaxStrategy(EditorSyntaxStrategy syntaxStrategy) {
        this.syntaxStrategy = syntaxStrategy;
    }

    /**
     * 异步着色策略
     */
    private EditorSyntaxAsyncStrategy syntaxAsyncStrategy = EditorSyntaxAsyncStrategy.AUTO;

    /**
     * 获取语法异步策略。
     *
     * @return 语法异步策略
     */
    public EditorSyntaxAsyncStrategy getSyntaxAsyncStrategy() {
        return syntaxAsyncStrategy;
    }

    /**
     * 设置语法异步策略。
     *
     * @param syntaxAsyncStrategy 语法异步策略
     */
    public void setSyntaxAsyncStrategy(EditorSyntaxAsyncStrategy syntaxAsyncStrategy) {
        this.syntaxAsyncStrategy = syntaxAsyncStrategy;
    }

    /**
     * 异步着色，最小行数阈值
     */
    private int syntaxAsyncMinThreshold = 500;

    /**
     * 获取语法异步最小阈值。
     *
     * @return 语法异步最小阈值
     */
    public int getSyntaxAsyncMinThreshold() {
        return syntaxAsyncMinThreshold;
    }

    /**
     * 设置语法异步最小阈值。
     *
     * @param syntaxAsyncMinThreshold 语法异步最小阈值
     */
    public void setSyntaxAsyncMinThreshold(int syntaxAsyncMinThreshold) {
        this.syntaxAsyncMinThreshold = syntaxAsyncMinThreshold;
    }

    /**
     * 异步着色，自动行数阈值
     */
    private int syntaxAsyncAutoThreshold = 50_000;

    /**
     * 获取语法异步自动阈值。
     *
     * @return 语法异步自动阈值
     */
    public int getSyntaxAsyncAutoThreshold() {
        return syntaxAsyncAutoThreshold;
    }

    /**
     * 设置语法异步自动阈值。
     *
     * @param syntaxAsyncAutoThreshold 语法异步自动阈值
     */
    public void setSyntaxAsyncAutoThreshold(int syntaxAsyncAutoThreshold) {
        this.syntaxAsyncAutoThreshold = syntaxAsyncAutoThreshold;
    }

    /**
     * 着色，最大行数阈值
     */
    private int syntaxMaxThreshold = 500_000;

    /**
     * 获取语法最大阈值。
     *
     * @return 语法最大阈值
     */
    public int getSyntaxMaxThreshold() {
        return syntaxMaxThreshold;
    }

    /**
     * 设置语法最大阈值。
     *
     * @param syntaxMaxThreshold 语法最大阈值
     */
    public void setSyntaxMaxThreshold(int syntaxMaxThreshold) {
        this.syntaxMaxThreshold = syntaxMaxThreshold;
    }

    /**
     * 首次变更标志位
     */
    private boolean firstChange = true;

    /**
     * 是否开启语法着色
     *
     * @param lineCount 行总数
     * @return 结果
     */
    private boolean isSyntax(long lineCount) {
        return lineCount <= this.syntaxMaxThreshold;
    }

    /**
     * 是否异步语法着色
     *
     * @param lineCount 行总数
     * @return 结果
     */
    private boolean isAsyncSyntax(long lineCount, long contentLength) {
        if (this.syntaxStrategy == EditorSyntaxStrategy.SYNC) {
            return false;
        }
        if (this.syntaxStrategy == EditorSyntaxStrategy.ASYNC) {
            return true;
        }

        boolean largeDocument = lineCount > this.syntaxAsyncMinThreshold
                || lineCount > this.syntaxAsyncAutoThreshold
                || contentLength > (long) this.syntaxAsyncMinThreshold * 1024L;
        if (!largeDocument) {
            return false;
        }
        if (this.syntaxAsyncStrategy == EditorSyntaxAsyncStrategy.FIRST) {
            boolean firstChange = this.firstChange;
            this.firstChange = false;
            return firstChange;
        }
        return true;
    }

    @Override
    public void handleChange(@NonNull CodeTextModel model, @NonNull TextPos start, @NonNull TextPos end, int charsTop, int linesAdded, int charsBottom) {
        // 版本号自增，使正在执行中的后台着色任务(基于旧文本)失效，避免其覆盖当前结果
        int currentVersion = this.styleVersion.incrementAndGet();
        StyleProvider provider = this.getStyleProvider();
        if (provider == null) {
            this.myParagraphs = List.of();
            return;
        }

        if (model.size() == 0) {
            this.myParagraphs = List.of();
            this.documentChars = 0;
            return;
        }

        boolean wholeDocument = TextPos.ZERO.equals(start) && end.equals(model.getDocumentEnd()) && linesAdded == 0;
        if (wholeDocument) {
            this.myParagraphs = this.buildFastParagraphs(model);
            this.documentChars = this.calculateDocumentChars(model);
        } else {
            this.updateChangedParagraphs(model, start, end, linesAdded);
        }

        // 行号总数
        long lineCount = model.size();
        // 是否语法着色
        boolean syntax = this.isSyntax(lineCount);

        if (!syntax
                || CollectionUtil.isNotEmpty(this.prompts)
                || this.formatType == EditorFormatType.RAW
                || this.formatType == null) {
            if (wholeDocument) {
                this.refresh(model);
            }
            return;
        }

        boolean asyncSyntax = this.isAsyncSyntax(lineCount, this.documentChars);
        if (!asyncSyntax) {
            String text = this.getPlainText(model);
            synchronized (provider) {
                this.myParagraphs = this.buildRichParagraphs(text, currentVersion);
            }
            this.documentChars = text.length();
            if (wholeDocument) {
                this.refresh(model);
            }
            return;
        }

        this.scheduleRichParagraphs(model, currentVersion);
    }

    @Override
    public RichParagraph createRichParagraph(@NonNull CodeTextModel model, int index) {
        if (this.myParagraphs.isEmpty() || index >= this.myParagraphs.size()) {
            return RichParagraph.builder().build();
        }
        return this.myParagraphs.get(index);
    }

    /**
     * 仅重建本次编辑影响的段落，避免每次输入都扫描整篇文档。
     */
    private void updateChangedParagraphs(CodeTextModel model, TextPos start, TextPos end, int linesAdded) {
        List<RichParagraph> previous = this.myParagraphs;
        int modelSize = model.size();
        int startIx = Math.max(0, Math.min(start.index(), modelSize));
        int oldEndIx = Math.max(startIx, Math.min(end.index(), Math.max(previous.size() - 1, 0)));
        int oldCount = previous.isEmpty() ? 0 : oldEndIx - startIx + 1;
        int newCount = Math.max(1, Math.min(linesAdded + 1, modelSize - startIx));

        int expectedPreviousSize = modelSize - newCount + oldCount;
        if (previous.size() != expectedPreviousSize && !previous.isEmpty()) {
            this.myParagraphs = this.buildFastParagraphs(model);
            this.documentChars = this.calculateDocumentChars(model);
            return;
        }

        long removedChars = 0;
        int removeStart = Math.min(startIx, previous.size());
        int removeEnd = Math.min(removeStart + oldCount, previous.size());
        for (int i = removeStart; i < removeEnd; i++) {
            removedChars += previous.get(i).getPlainText().length();
        }

        List<RichParagraph> replacement = new ArrayList<>(newCount);
        long addedChars = 0;
        for (int i = 0; i < newCount; i++) {
            int paragraphIndex = startIx + i;
            String line = model.getPlainText(paragraphIndex);
            replacement.add(this.buildFastParagraph(line));
            addedChars += line.length();
        }

        List<RichParagraph> updated = previous instanceof ArrayList ? previous : new ArrayList<>(previous);
        updated.subList(removeStart, removeEnd).clear();
        updated.addAll(removeStart, replacement);
        this.myParagraphs = updated;
        int lineEndingLength = model.getLineEnding().getText().length();
        long lineEndingDelta = (long) (newCount - oldCount) * lineEndingLength;
        this.documentChars = Math.max(0, this.documentChars - removedChars + addedChars + lineEndingDelta);
    }

    /**
     * 计算文档文本长度，含换行符。
     */
    private long calculateDocumentChars(CodeTextModel model) {
        long chars = 0;
        int lineEndingLength = model.getLineEnding().getText().length();
        for (int i = 0; i < model.size(); i++) {
            if (i > 0) {
                chars += lineEndingLength;
            }
            chars += model.getParagraphLength(i);
        }
        return chars;
    }

    /**
     * 防抖提交完整语法着色，连续输入时取消旧任务。
     */
    private void scheduleRichParagraphs(CodeTextModel model, int currentVersion) {
        if (this.pendingStyleTask != null) {
            this.pendingStyleTask.cancel(false);
        }
        if (this.runningStyleTask != null) {
            this.runningStyleTask.cancel(true);
        }
        this.pendingStyleTask = STYLE_SCHEDULER.schedule(() -> FXUtil.runLater(() -> {
            if (this.styleVersion.get() != currentVersion) {
                return;
            }
            String text = this.getPlainText(model);
            int paragraphCount = model.size();
            if (this.styleVersion.get() != currentVersion) {
                return;
            }
            this.runningStyleTask = STYLE_EXECUTOR.submit(() -> {
                Thread.interrupted();
                StyleProvider provider = this.getStyleProvider();
                if (provider == null || this.styleVersion.get() != currentVersion) {
                    return;
                }
                List<RichParagraph> styled;
                synchronized (provider) {
                    if (this.styleVersion.get() != currentVersion) {
                        return;
                    }
                    styled = this.buildRichParagraphs(text, currentVersion);
                }
                if (this.styleVersion.get() != currentVersion || styled.size() != paragraphCount) {
                    return;
                }
                FXUtil.runLater(() -> {
                    if (this.styleVersion.get() != currentVersion) {
                        return;
                    }
                    this.myParagraphs = styled;
                    this.documentChars = text.length();
                    this.refresh(model);
                });
            });
        }), STYLE_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS);
    }

    /**
     * 取消当前编辑器尚未完成的着色任务。
     */
    public void dispose() {
        this.styleVersion.incrementAndGet();
        if (this.pendingStyleTask != null) {
            this.pendingStyleTask.cancel(false);
            this.pendingStyleTask = null;
        }
        if (this.runningStyleTask != null) {
            this.runningStyleTask.cancel(true);
            this.runningStyleTask = null;
        }
    }

    /**
     * 快速构建模型中的全部段落，不执行语法分词。
     */
    private List<RichParagraph> buildFastParagraphs(CodeTextModel model) {
        List<RichParagraph> paragraphs = new ArrayList<>(model.size());
        for (int i = 0; i < model.size(); i++) {
            paragraphs.add(this.buildFastParagraph(model.getPlainText(i)));
        }
        return paragraphs;
    }

    /**
     * 快速构建单个段落，仅应用提示词和搜索高亮。
     */
    private RichParagraph buildFastParagraph(String line) {
        RichParagraph.Builder builder = RichParagraph.builder();
        if (CollectionUtil.isNotEmpty(this.prompts)) {
            List<EditorMachToken> machTokens = this.machPrompts(line);
            List<StyledToken> tokens = this.buildTokens(line, machTokens);
            for (StyledToken token : tokens) {
                super.applyStyles(builder, token);
            }
        } else {
            super.applyStyles(builder, new StyledToken(line, null));
        }
        if (StringUtil.isNotEmpty(this.highlight)) {
            List<EditorMachToken> machTokens = this.machHighlight(line);
            for (EditorMachToken token : machTokens) {
                builder.addHighlight(token.start(), token.length(), this.highlightColor);
            }
        }
        return builder.build();
    }

    ///**
    // * token缓存
    // * key: 行内容hash
    // * value: 行样式token列表
    // */
    //private final Map<String, List<StyledToken>> tokenCache = new ConcurrentHashMap<>();

    /**
     * 完整构建段落，包含语法着色、提示词和高亮。
     * <p>
     * 此方法在后台线程中调用，可能耗时较长。
     *
     * @param text 文本
     * @param version 着色版本号
     */
    private List<RichParagraph> buildRichParagraphs(String text, int version) {
        StyleProvider provider = this.getStyleProvider();
        if (provider == null) {
            JulLog.warn("StyleProvider is null");
            return Collections.emptyList();
        }
        // 每次整篇着色前必须重置分词状态。
        // StyleProvider 会在多次调用之间保留上一行的分词状态(prevState)，若上一次着色时
        // 文档末尾处于未闭合的多行结构(如 SQL/JS 中未闭合的单引号、双引号、块注释)，
        // 残留状态会让本次着色从字符串/注释规则开始，表现为整篇文本都变成同一种颜色。
        // 本方法始终是整篇重新着色，故此处重置状态是安全的。
        provider.flush();
        String[] lines = text.split(LINE_SPLIT_PATTERN, -1);
        List<RichParagraph> paragraphs = new ArrayList<>(lines.length);
        for (String line : lines) {
            if (Thread.currentThread().isInterrupted() || this.styleVersion.get() != version) {
                return Collections.emptyList();
            }
            RichParagraph.Builder builder = RichParagraph.builder();
            if (CollectionUtil.isNotEmpty(this.prompts)) {
                List<EditorMachToken> machTokens = this.machPrompts(line);
                List<StyledToken> tokens = this.buildTokens(line, machTokens);
                for (StyledToken token : tokens) {
                    super.applyStyles(builder, token);
                }
            } else if (this.formatType == EditorFormatType.RAW || this.formatType == null) {
                super.applyStyles(builder, new StyledToken(line, null));
            } else {
                List<StyledToken> tokens;
                //String hash = MD5Util.md5Hex(line);
                //// 从缓存获取
                //if (this.tokenCache.containsKey(hash)) {
                //    if (line.contains("image2")) {
                //        System.out.println(hash + " " + line);
                //    }
                //    tokens = this.tokenCache.get(hash);
                //} else {// 执行生成
                    tokens = provider.tokenize(line);
                    //this.tokenCache.put(hash, tokens);
                //}
                // 应用样式
                for (StyledToken token : tokens) {
                    super.applyStyles(builder, token);
                }
            }
            if (StringUtil.isNotEmpty(this.highlight)) {
                List<EditorMachToken> machTokens = this.machHighlight(line);
                for (EditorMachToken token : machTokens) {
                    builder.addHighlight(token.start(), token.length(), this.highlightColor);
                }
            }
            paragraphs.add(builder.build());
        }
        return paragraphs;
    }

    /**
     * 匹配高亮
     *
     * @param line 当前行
     * @return 编辑器匹配样式列表
     */
    private List<EditorMachToken> machHighlight(String line) {
        //        StyleAttributeMap style = this.highlightStyle();
        //        StyledToken token = new StyledToken(this.highlight, style);
        //        Pattern pattern = Pattern.compile(this.highlight, Pattern.CASE_INSENSITIVE);
        if (this.highlightPattern != null) {
            Matcher matcher = this.highlightPattern.matcher(line);
            List<EditorMachToken> machTokens = new ArrayList<>();
            while (matcher.find()) {
                //            EditorMachToken machToken = new EditorMachToken(matcher.start(), matcher.end(), token);
                EditorMachToken machToken = new EditorMachToken(matcher.start(), matcher.end(), null);
                machTokens.add(machToken);
            }
            return machTokens;
        }
        return Collections.emptyList();
    }

    /**
     * 匹配提示词
     *
     * @param line 当前行
     * @return 编辑器匹配样式列表
     */
    private List<EditorMachToken> machPrompts(String line) {
        if (this.promptsPattern != null) {
            StyleAttributeMap style = this.promptsStyle();
            //            Pattern pattern = Pattern.compile("\\b(" + String.join("|", this.prompts) + ")\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = this.promptsPattern.matcher(line);
            List<EditorMachToken> machTokens = new ArrayList<>();
            while (matcher.find()) {
                StyledToken token = new StyledToken(matcher.group(), style);
                EditorMachToken machToken = new EditorMachToken(matcher.start(), matcher.end(), token);
                machTokens.add(machToken);
            }
            return machTokens;
        }
        return Collections.emptyList();
    }

    /**
     * 构建样式token
     *
     * @param line       当前行
     * @param machTokens 匹配样式列表
     * @return 样式token列表
     */
    private List<StyledToken> buildTokens(String line, List<EditorMachToken> machTokens) {
        // 进行排序
        machTokens.sort(Comparator.comparingInt(EditorMachToken::start));
        // 搜索开始索引
        int fIndex = 0;
        List<StyledToken> tokens = new ArrayList<>();
        for (EditorMachToken machToken : machTokens) {
            int start = machToken.start();
            // 未匹配的样式，设置为null
            if (start > fIndex) {
                tokens.add(new StyledToken(line.substring(fIndex, start), null));
            }
            // 添加已匹配样式
            tokens.add(machToken.token());
            fIndex = machToken.end();
        }
        // 对尾部进行处理
        if (fIndex < line.length()) {
            tokens.add(new StyledToken(line.substring(fIndex), null));
        }
        return tokens;
    }
}
