package cn.oyzh.fx.editor.tm4javafx;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.jspecify.annotations.Nullable;
import tm4javafx.richtext.StatelessSyntaxDecorator;
import tm4javafx.richtext.StyleProvider;
import tm4javafx.richtext.StyledToken;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
    private String highlight;

    /**
     * 提示词
     */
    private Set<String> prompts;

    /**
     * 格式类型
     */
    private EditorFormatType formatType = EditorFormatType.RAW;

    public EditorSyntaxDecorator() {
        this(null);
    }

    public EditorSyntaxDecorator(@Nullable StyleProvider styleProvider) {
        super(styleProvider);
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public String getHighlight() {
        return highlight;
    }

    public Set<String> getPrompts() {
        return prompts;
    }

    public void setPrompts(Set<String> prompts) {
        this.prompts = prompts;
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
    private Color highlightColor = Editor.DEFAULT_HIGHLIGHT_COLOR;

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    /**
     * 高亮样式
     */
    private StyleAttributeMap highlightStyle;

    public StyleAttributeMap highlightStyle() {
        if (this.highlightStyle == null) {
            this.highlightStyle = StyleAttributeMap.of(StyleAttributeMap.TEXT_COLOR, highlightColor);
        }
        return this.highlightStyle;
    }

    /**
     * 提示词颜色
     */
    private Color promptsColor = Editor.DEFAULT_PROMPTS_COLOR;

    public void setPromptsColor(Color promptsColor) {
        this.promptsColor = promptsColor;
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

    @Override
    protected List<RichParagraph> createRichParagraphs(StyleProvider provider, String text) {
        // 处理提示词
        if (CollectionUtil.isNotEmpty(this.prompts)) {
            String[] lines = text.split(LINE_SPLIT_PATTERN);
            List<RichParagraph> paragraphs = new ArrayList<>(lines.length);
            for (String line : lines) {
                RichParagraph.Builder paragraph = RichParagraph.builder();
                List<EditorMachToken> machTokens = this.machPrompts(line);
                List<StyledToken> tokens = this.buildTokens(line, machTokens);
                for (StyledToken token : tokens) {
                    super.applyStyles(paragraph, token);
                }
                paragraphs.add(paragraph.build());
            }
            return paragraphs;
        }
        // 处理高亮
        if (StringUtil.isNotBlank(this.highlight)) {
            String[] lines = text.split(LINE_SPLIT_PATTERN);
            List<RichParagraph> paragraphs = new ArrayList<>(lines.length);
            for (String line : lines) {
                RichParagraph.Builder paragraph = RichParagraph.builder();
                List<EditorMachToken> machTokens = this.machHighlight(line);
                List<StyledToken> tokens = this.buildTokens(line, machTokens);
                for (StyledToken token : tokens) {
                    super.applyStyles(paragraph, token);
                }
                paragraphs.add(paragraph.build());
            }
            return paragraphs;
        }
        // 处理格式
        if (this.formatType == EditorFormatType.RAW || this.formatType == null) {
            String[] lines = text.split(LINE_SPLIT_PATTERN);
            List<RichParagraph> paragraphs = new ArrayList<>(lines.length);
            for (String line : lines) {
                RichParagraph.Builder paragraph = RichParagraph.builder();
                super.applyStyles(paragraph, new StyledToken(line, null));
                paragraphs.add(paragraph.build());
            }
            return paragraphs;
        }
        // 默认处理
        return super.createRichParagraphs(provider, text);
    }

    /**
     * 匹配高亮
     *
     * @param line 当前行
     * @return 编辑器匹配样式列表
     */
    private List<EditorMachToken> machHighlight(String line) {
        StyleAttributeMap style = this.highlightStyle();
        StyledToken token = new StyledToken(this.highlight, style);
        Pattern pattern = Pattern.compile(this.highlight, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        List<EditorMachToken> machTokens = new ArrayList<>();
        while (matcher.find()) {
            EditorMachToken machToken = new EditorMachToken(matcher.start(), matcher.end(), token);
            machTokens.add(machToken);
        }
        return machTokens;
    }

    /**
     * 匹配提示词
     *
     * @param line 当前行
     * @return 编辑器匹配样式列表
     */
    private List<EditorMachToken> machPrompts(String line) {
        StyleAttributeMap style = this.promptsStyle();
        Pattern pattern = Pattern.compile("\\b(" + String.join("|", this.prompts) + ")\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        List<EditorMachToken> machTokens = new ArrayList<>();
        while (matcher.find()) {
            StyledToken token = new StyledToken(matcher.group(), style);
            EditorMachToken machToken = new EditorMachToken(matcher.start(), matcher.end(), token);
            machTokens.add(machToken);
        }
        return machTokens;
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
        machTokens.sort(Comparator.comparingInt(EditorMachToken::getStart));
        // 搜索开始索引
        int fIndex = 0;
        List<StyledToken> tokens = new ArrayList<>();
        for (EditorMachToken machToken : machTokens) {
            int start = machToken.getStart();
            // 未匹配的样式，设置为null
            if (start > fIndex) {
                tokens.add(new StyledToken(line.substring(fIndex, start), null));
            }
            // 添加已匹配样式
            tokens.add(machToken.getToken());
            fIndex = machToken.getEnd();
        }
        // 对尾部进行处理
        if (fIndex < line.length()) {
            tokens.add(new StyledToken(line.substring(fIndex), null));
        }
        return tokens;
    }

    @Override
    public RichParagraph createRichParagraph(CodeTextModel model, int index) {
        return super.createRichParagraph(model, index);
    }
}
