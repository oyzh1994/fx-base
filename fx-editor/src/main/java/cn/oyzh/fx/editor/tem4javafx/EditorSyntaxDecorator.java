package cn.oyzh.fx.editor.tem4javafx;

import cn.oyzh.common.util.StringUtil;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.jspecify.annotations.Nullable;
import tm4javafx.richtext.StatelessSyntaxDecorator;
import tm4javafx.richtext.StyleProvider;
import tm4javafx.richtext.StyledToken;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author oyzh
 * @since 2025-08-14
 */
public class EditorSyntaxDecorator extends StatelessSyntaxDecorator {

    private String highlightText;

    public EditorSyntaxDecorator() {
        this(null);
    }

    public EditorSyntaxDecorator(@Nullable StyleProvider styleProvider) {
        super(styleProvider);
    }

    public void setHighlightText(String highlightText) {
        this.highlightText = highlightText;
    }

    public String getHighlightText() {
        return highlightText;
    }

    @Override
    protected List<RichParagraph> createRichParagraphs(StyleProvider provider, String text) {
        if (StringUtil.isNotBlank(this.highlightText)) {
            String[] lines = text.split(LINE_SPLIT_PATTERN);
            List<RichParagraph> paragraphs = new ArrayList<>(lines.length);
            for (String line : lines) {
                List<StyledToken> tokens = new ArrayList<>();
                int fIndex = 0;
                while (true) {
                    int index = line.indexOf(highlightText, fIndex);
                    if (index == -1) {
                        tokens.add(new StyledToken(line.substring(fIndex), null));
                        break;
                    }
                    String str = line.substring(fIndex, index);
                    tokens.add(new StyledToken(str, null));
                    StyleAttributeMap style = StyleAttributeMap.of(StyleAttributeMap.TEXT_COLOR, HIGHLIGHT_COLOR);
                    tokens.add(new StyledToken(highlightText, style));
                    fIndex = index + highlightText.length();
                }
                RichParagraph.Builder paragraph = RichParagraph.builder();
                for (StyledToken token : tokens) {
                    applyStyles(paragraph, token);
                }
                paragraphs.add(paragraph.build());
            }
            return paragraphs;
        }
        return super.createRichParagraphs(provider, text);

    }

    /**
     * 高亮颜色
     */
    private static final Color HIGHLIGHT_COLOR = Color.rgb(255, 102, 0);

}
