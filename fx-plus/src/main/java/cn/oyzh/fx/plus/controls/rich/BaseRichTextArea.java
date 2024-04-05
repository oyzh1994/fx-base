package cn.oyzh.fx.plus.controls.rich;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.NonNull;
import org.fxmisc.richtext.CaretNode;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.util.UndoUtils;
import org.reactfx.value.Val;

import java.util.Collection;
import java.util.function.IntFunction;

/**
 * @author oyzh
 * @since 2023/9/28
 */
public class BaseRichTextArea extends InlineCssTextArea implements ThemeAdapter, FontAdapter, TextAdapter, TipAdapter, StateAdapter {

    {
//        this.setCache(true);
//        this.setCacheShape(true);
//        this.setCacheHint(CacheHint.QUALITY);
        this.setWrapText(true);
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
        this.setAutoScrollOnDragDesired(true);
        this.setPadding(new Insets(5, 5, 5, 5));
        BorderStroke stroke = new BorderStroke(Color.GREY, BorderStrokeStyle.SOLID, null, new BorderWidths(1));
        this.setBorder(new Border(stroke));
        this.getStyleClass().add("rich-text-area");
        this.applyPlainUndoManager();
//        this.changeTheme(ThemeManager.currentTheme());
        NodeManager.init(this);
    }

    /**
     * 应用丰富操作管理器
     */
    public void applyRichUndoManager() {
        this.setUndoManager(UndoUtils.richTextUndoManager(this));
    }

    /**
     * 应用文本操作管理器
     */
    public void applyPlainUndoManager() {
        this.setUndoManager(UndoUtils.plainTextUndoManager(this));
    }

    /**
     * 行号函数
     */
    private IntFunction<Node> lineFunc;

    /**
     * 设置是否显示行号
     *
     * @param showLine 是否显示行号
     */
    public void setShowLine(boolean showLine) {
        this.setProp("showLine", showLine);
        if (showLine) {
            this.showLineNum();
        } else {
            this.hideLineNum();
        }
    }

    /**
     * 获取设置是否显示行号
     */
    public boolean isShowLine() {
        return this.getProp("showLine");
    }

    /**
     * 显示行号
     */
    public void showLineNum() {
        if (this.getParagraphGraphicFactory() == null) {
            if (lineFunc == null) {
                lineFunc = LineNumberFactory.get(this);
            }
            FXUtil.runWait(() -> this.setParagraphGraphicFactory(lineFunc));
        }
    }

    /**
     * 隐藏行号
     */
    public void hideLineNum() {
        if (this.getParagraphGraphicFactory() != null) {
            this.lineFunc = null;
            FXUtil.runWait(() -> this.setParagraphGraphicFactory(null));
        }
    }

    /**
     * 设置内容
     *
     * @param text 内容
     */
    public void setText(String text) {
        if (text != null) {
            FXUtil.runWait(() -> this.replaceText(text));
        }
    }

    @Override
    public String getText() {
        return super.getText();
    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
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

    public String getPromptText() {
        Node node = super.getPlaceholder();
        if (node instanceof Labeled l) {
            return l.getText();
        }
        return null;
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

    @Override
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }

    @Override
    public void replaceSelection(String replacement) {
        FXUtil.runWait(() -> super.replaceSelection(replacement));
    }

    @Override
    public void selectRange(int anchor, int caretPosition) {
        FXUtil.runWait(() -> super.selectRange(anchor, caretPosition));
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        if (this.isEnableTheme()) {
            Node placeholder = this.getPlaceholder();
            CaretNode caretNode = this.getCaretSelectionBind().getUnderlyingCaret();
            if (style.isDarkMode()) {
                this.setStyle(0, this.getLength(), "-fx-fill: #fff;");
                caretNode.setStroke(Color.WHITE);
                if (placeholder != null) {
                    placeholder.setStyle("-fx-fill: #fff;");
                }
            } else {
                this.setStyle(0, this.getLength(), "-fx-fill: #000");
                caretNode.setStroke(Color.BLACK);
                if (placeholder != null) {
                    placeholder.setStyle("-fx-fill: #000;");
                }
            }
        }
    }
}
