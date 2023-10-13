package cn.oyzh.fx.plus.rich;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.NonNull;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.reactfx.value.Val;

import java.util.Collection;
import java.util.function.IntFunction;

/**
 * @author oyzh
 * @since 2023/9/28
 */
public class BaseRichTextArea extends InlineCssTextArea implements FontAdapter, TextAdapter, TipAdapter, StateAdapter {

    {
        this.setCache(true);
        this.setWrapText(true);
        this.setCacheShape(true);
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
        this.setCacheHint(CacheHint.QUALITY);
        this.setAutoScrollOnDragDesired(true);
        this.setPadding(new Insets(2, 5, 2, 5));
        BorderStroke stroke = new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, null, new BorderWidths(1));
        this.setBorder(new Border(stroke));
    }

    /**
     * 行号函数
     */
    private IntFunction<Node> lineFunc;

    /**
     * 显示行号
     */
    public void showLineNum() {
        if (lineFunc == null) {
            lineFunc = LineNumberFactory.get(this);
        }
        FXUtil.runWait(() -> this.setParagraphGraphicFactory(lineFunc));
    }

    /**
     * 隐藏行号
     */
    public void hideLineNum() {
        FXUtil.runWait(() -> this.setParagraphGraphicFactory(null));
    }

    public void setText(String text) {
        FXUtil.runWait(() -> this.replaceText(text));
        this.forgetHistory();
    }

    @Override
    public String getText() {
        return super.getText();
    }

    @Override
    public void setTipText(String tipTitle) {
        TipAdapter.super.tipText(tipTitle);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

    @Override
    public void setFontSize(double fontSize) {
        FontAdapter.super.fontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return FontAdapter.super.fontSize();
    }

    @Override
    public void setFontFamily(@NonNull String fontFamily) {
        FontAdapter.super.fontFamily(fontFamily);
    }

    @Override
    public String getFontFamily() {
        return FontAdapter.super.fontFamily();
    }

    public void setPromptText(String prompt) {
        FXUtil.runLater(() -> this.setPlaceholder(new Text(prompt)));
    }

    public Val<Boolean> undoableProperty() {
        return this.undoAvailableProperty();
    }

    public Val<Boolean> redoableProperty() {
        return this.redoAvailableProperty();
    }

    /**
     * 追加多行
     *
     * @param list 文本列表
     */
    public void appendLines(Collection<String> list) {
        if (CollUtil.isNotEmpty(list)) {
            String str = CollUtil.join(list, "\n");
            this.appendText(str + "\n");
        }
    }

    /**
     * 追加行
     *
     * @param s 文本内容
     */
    public void appendLine(String s) {
        if (s != null) {
            String text = s;
            if (this.getLength() > 0 && !this.getText().endsWith("\n") && !text.startsWith("\n")) {
                text = "\n" + text;
            }
            if (!text.endsWith("\n")) {
                text = text + "\n";
            }
            this.appendText(text);
        }
    }

    @Override
    public void deleteText(int start, int end) {
        FXUtil.runWait(() -> super.deleteText(start, end));
    }

    @Override
    public void appendText(String s) {
        if (s != null) {
            FXUtil.runWait(() -> super.appendText(s));
        }
    }

    @Override
    public void clear() {
        FXUtil.runWait(super::clear);
    }

    @Override
    public int getLength() {
        String text = super.getText();
        if (text == null) {
            return super.getLength();
        }
        return text.length();
    }

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return this.getText().isEmpty();
    }

    /**
     * 滚动到尾部
     */
    public void scrollToEnd() {
        ExecutorUtil.start(() -> {
            int len = this.getLength() - 1;
            if (len > 0) {
                this.selectRange(len - 1, len);
                this.deselect();
                this.positionCaret(this.getLength());
            }
        }, 150);
    }

    public void positionCaret(int caretPosition) {
        this.displaceCaret(caretPosition);
    }

    /**
     * 清除文字样式
     */
    public void clearTextStyle() {
        FXUtil.runWait(() -> this.setStyle(0, this.getLength(), ""));
        this.forgetHistory();
    }

    /**
     * 忘记历史记录
     */
    public void forgetHistory() {
        this.getUndoManager().forgetHistory();
    }

    /**
     * 初始化文字样式
     */
    public void initTextStyle() {
    }

    /**
     * 设置样式
     *
     * @param style 富文本样式
     */
    public void setStyle(RichTextStyle style) {
        if (style != null) {
            this.setStyle(style.start(), style.end(), style.style());
        }
    }
}
