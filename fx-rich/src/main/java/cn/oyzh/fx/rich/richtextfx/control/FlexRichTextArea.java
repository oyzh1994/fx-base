package cn.oyzh.fx.rich.richtextfx.control;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.AreaAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author oyzh
 * @since 2023/9/28
 */
public class FlexRichTextArea extends BaseRichTextArea implements FlexAdapter, AreaAdapter {

    /**
     * 高亮文本
     */
    @Getter
    private String highlightText;

    /**
     * 设置高亮文本
     *
     * @param highlightText 高亮文本
     */
    public void setHighlightText(String highlightText) {
        this.highlightText = highlightText;
        this.initTextStyle();
    }

    /**
     * 高亮正则模式
     *
     * @return 高亮正则模式
     */
    protected Pattern highlightPattern() {
        return Pattern.compile(this.highlightText);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public void flushCaret() {
        this.positionCaret(this.getCaretPosition());
        this.requestFocus();
    }

    @Override
    public void setFontSize(double fontSize) {
        NodeUtil.replaceStyle(this, "-fx-font-size", fontSize + "px");
    }

    @Override
    public void initTextStyle() {
        super.initTextStyle();
        // 高亮
        if (StringUtil.isNotBlank(this.highlightText)) {
            String text = this.getText();
            Matcher matcher = this.highlightPattern().matcher(text);
            List<RichTextStyle> styles = new ArrayList<>();
            while (matcher.find()) {
                styles.add(new RichTextStyle(matcher.start(), matcher.end(), "-fx-fill: #FF6600;"));
            }
            this.setStyles(styles);
        }
        // 内容提示
        if (this.contentPrompts != null) {
            String text = this.getText();
            Matcher matcher1 = this.contentPrompts.matcher(text);
            List<RichTextStyle> styles = new ArrayList<>();
            while (matcher1.find()) {
                styles.add(new RichTextStyle(matcher1.start(), matcher1.end(), "-fx-fill: #008B45;"));
            }
            this.setStyles(styles);
            this.forgetHistory();
        }
    }

    /**
     * 基础内容正则模式
     */
    protected Pattern contentPrompts;

    /**
     * 设置内容提示词
     *
     * @param prompts 内容提示词列表
     */
    public void setContentPrompts(Set<String> prompts) {
        if (prompts == null || prompts.isEmpty()) {
            this.contentPrompts = null;
        } else {
            StringBuilder regex = new StringBuilder("\\b(");
            for (String s : prompts) {
                regex.append(s).append("|");
            }
            regex.append(")\\b");
            this.contentPrompts = Pattern.compile(regex.toString().replaceFirst("\\|\\)", ")"), Pattern.CASE_INSENSITIVE);
        }
        this.initTextStyle();
    }

    /**
     * 初始化内容提示词
     */
    public void initContentPrompts() {
    }
}
