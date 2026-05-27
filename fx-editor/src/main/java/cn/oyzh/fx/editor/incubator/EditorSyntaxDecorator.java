package cn.oyzh.fx.editor.incubator;

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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
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

    public EditorSyntaxDecorator() {
        this(null);
    }

    public EditorSyntaxDecorator(@Nullable StyleProvider styleProvider) {
        super(styleProvider);
    }

    /**
     * 维护自身的段落缓存，用于异步更新
     */
    private volatile List<RichParagraph> myParagraphs = List.of();

    /**
     * 样式版本号，用于丢弃过期的后台任务结果
     */
    private final AtomicInteger styleVersion = new AtomicInteger(0);

    /**
     * 后台语法高亮线程，单线程串行执行，避免provider状态冲突
     */
    private final ExecutorService styleExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "syntax-highlighter");
        t.setDaemon(true);
        return t;
    });

    private Pattern highlightPattern;

    public void setHighlight(String highlight) {
        this.highlight = highlight;
        this.initHighlightPattern();
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlightRegex(boolean highlightRegex) {
        this.highlightRegex = highlightRegex;
        this.initHighlightPattern();
    }

    public boolean isHighlightRegex() {
        return highlightRegex;
    }

    public void setHighlightWholeWord(boolean highlightWholeWord) {
        this.highlightWholeWord = highlightWholeWord;
        this.initHighlightPattern();
    }

    public boolean isHighlightWholeWord() {
        return highlightWholeWord;
    }

    public void setHighlightMatchCase(boolean highlightMatchCase) {
        this.highlightMatchCase = highlightMatchCase;
        this.initHighlightPattern();
    }

    public boolean isHighlightMatchCase() {
        return highlightMatchCase;
    }

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

    public Set<String> getPrompts() {
        return prompts;
    }

    private Pattern promptsPattern;

    public void setPrompts(Set<String> prompts) {
        this.prompts = prompts;
        if (CollectionUtil.isNotEmpty(prompts)) {
            try {
                String pattern = "\\b(" + String.join("|", this.prompts) + ")\\b";
                this.promptsPattern = Pattern.compile(Pattern.quote(pattern), Pattern.CASE_INSENSITIVE);
            } catch (Exception ignore) {
            }
        } else {
            this.promptsPattern = null;
        }
    }

    public EditorFormatType getFormatType() {
        return formatType;
    }

    public void setFormatType(EditorFormatType formatType) {
        this.formatType = formatType;
    }

    /**
     * 高亮颜色
     */
    private volatile Color highlightColor = Editor.DEFAULT_HIGHLIGHT_COLOR;

    public Color getHighlightColor() {
        return highlightColor;
    }

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

    public void setPromptsColor(Color promptsColor) {
        this.promptsColor = promptsColor;
        this.promptsStyle = null;
    }

    public Color getPromptsColor() {
        return promptsColor;
    }

    /**
     * 提示词样式
     */
    private StyleAttributeMap promptsStyle;

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

    public EditorSyntaxStrategy getSyntaxStrategy() {
        return syntaxStrategy;
    }

    public void setSyntaxStrategy(EditorSyntaxStrategy syntaxStrategy) {
        this.syntaxStrategy = syntaxStrategy;
    }

    /**
     * 异步着色策略
     */
    private EditorSyntaxAsyncStrategy syntaxAsyncStrategy = EditorSyntaxAsyncStrategy.AUTO;

    public EditorSyntaxAsyncStrategy getSyntaxAsyncStrategy() {
        return syntaxAsyncStrategy;
    }

    public void setSyntaxAsyncStrategy(EditorSyntaxAsyncStrategy syntaxAsyncStrategy) {
        this.syntaxAsyncStrategy = syntaxAsyncStrategy;
    }

    /**
     * 异步着色，最小行数阈值
     */
    private int syntaxAsyncMinThreshold = 1000;

    public int getSyntaxAsyncMinThreshold() {
        return syntaxAsyncMinThreshold;
    }

    public void setSyntaxAsyncMinThreshold(int syntaxAsyncMinThreshold) {
        this.syntaxAsyncMinThreshold = syntaxAsyncMinThreshold;
    }

    /**
     * 异步着色，自动行数阈值
     */
    private int syntaxAsyncAutoThreshold = 100_000;

    public int getSyntaxAsyncAutoThreshold() {
        return syntaxAsyncAutoThreshold;
    }

    public void setSyntaxAsyncAutoThreshold(int syntaxAsyncAutoThreshold) {
        this.syntaxAsyncAutoThreshold = syntaxAsyncAutoThreshold;
    }

    /**
     * 首次变更标志位
     */
    private boolean firstChange = true;

    @Override
    public void handleChange(@NonNull CodeTextModel model, @NonNull TextPos start, @NonNull TextPos end, int charsTop, int linesAdded, int charsBottom) {
        StyleProvider provider = this.getStyleProvider();
        if (provider == null) {
            this.myParagraphs = List.of();
            return;
        }

        String text = this.getPlainText(model);
        if (text.isEmpty()) {
            this.myParagraphs = List.of();
            return;
        }

        // 行号总数
        long lineCount = -1;
        // 异步着色标志位
        boolean asyncSyntax = false;
        // 异步着色判断
        if (this.syntaxStrategy == EditorSyntaxStrategy.AUTO) {
            lineCount = text.lines().count();
            // 大于最小阈值，则异步
            if (lineCount > this.syntaxAsyncMinThreshold) {
                asyncSyntax = true;
            }
        } else if (this.syntaxStrategy == EditorSyntaxStrategy.ASYNC) {
            asyncSyntax = true;
        }

        // 异步着色修正
        if (asyncSyntax) {
            // 首次异步
            if (this.syntaxAsyncStrategy == EditorSyntaxAsyncStrategy.FIRST) {
                if (!this.firstChange) {
                    asyncSyntax = false;
                }
            } else if (this.syntaxAsyncStrategy == EditorSyntaxAsyncStrategy.AUTO) {// 自动异步
                if (!this.firstChange) {
                    if (lineCount == -1) {
                        lineCount = text.lines().count();
                    }
                    // 小于自动阈值，则还是同步
                    if (lineCount <= this.syntaxAsyncAutoThreshold) {
                        asyncSyntax = false;
                    }
                }
            }
        }

        // 变更首次flag
        if (this.firstChange) {
            this.firstChange = false;
        }

        // 正常路径：构建语法着色的段落
        if (!asyncSyntax) {
            this.myParagraphs = this.buildRichParagraphs(text);
            this.refresh(model);
            return;
        }

        // 快速路径：构建无语法着色的段落
        this.myParagraphs = this.buildFastParagraphs(text);
        this.refresh(model);

        // 只有需要语法着色时才提交后台任务
        if (CollectionUtil.isNotEmpty(this.prompts)
                || this.formatType == EditorFormatType.RAW
                || this.formatType == null) {
            return;
        }
        // 后台线程：完整语法着色
        int currentVersion = this.styleVersion.incrementAndGet();
        // 提交异步线程
        this.styleExecutor.submit(() -> {
            if (this.styleVersion.get() != currentVersion) {
                return;
            }
            List<RichParagraph> styled;
            synchronized (provider) {
                if (this.styleVersion.get() != currentVersion) {
                    return;
                }
                provider.flush();
                styled = this.buildRichParagraphs(text);
            }
            if (styleVersion.get() != currentVersion) {
                return;
            }
            FXUtil.runLater(() -> {
                if (styleVersion.get() != currentVersion) {
                    return;
                }
                this.myParagraphs = styled;
                this.refresh(model);
            });
        });
    }

    @Override
    public RichParagraph createRichParagraph(@NonNull CodeTextModel model, int index) {
        if (this.myParagraphs.isEmpty() || index >= this.myParagraphs.size()) {
            return RichParagraph.builder().build();
        }
        return this.myParagraphs.get(index);
    }

    /**
     * 快速构建段落，跳过语法着色，仅应用提示词和高亮。
     *
     * @param text 文本
     */
    private List<RichParagraph> buildFastParagraphs(String text) {
        String[] lines = text.split(LINE_SPLIT_PATTERN);
        List<RichParagraph> paragraphs = new ArrayList<>(lines.length);
        for (String line : lines) {
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
            paragraphs.add(builder.build());
        }
        return paragraphs;
    }

    /**
     * token缓存
     * key: 行内容hashCode
     * value: 行样式token列表
     */
    private final Map<Integer, List<StyledToken>> tokenCache = new ConcurrentHashMap<>();

    /**
     * 完整构建段落，包含语法着色、提示词和高亮。
     * <p>
     * 此方法在后台线程中调用，可能耗时较长。
     *
     * @param text 文本
     */
    private List<RichParagraph> buildRichParagraphs(String text) {
        StyleProvider provider = this.getStyleProvider();
        String[] lines = text.split(LINE_SPLIT_PATTERN);
        List<RichParagraph> paragraphs = new ArrayList<>(lines.length);
        for (String line : lines) {
            if (Thread.interrupted()) {
                return paragraphs;
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
                int hashCode = line.hashCode();
                // 从缓存获取
                if (this.tokenCache.containsKey(hashCode)) {
                    tokens = this.tokenCache.get(hashCode);
                } else {// 执行生成
                    tokens = provider.tokenize(line);
                    this.tokenCache.put(hashCode, tokens);
                }
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
