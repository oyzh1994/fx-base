package cn.oyzh.fx.rich.richtextfx.control;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.CostUtil;
import cn.oyzh.common.util.MD5Util;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.i18n.I18nAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXColorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.fxmisc.richtext.CaretNode;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.util.UndoUtils;
import org.reactfx.value.Val;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

/**
 * @author oyzh
 * @since 2023/9/28
 */
public class BaseRichTextArea extends InlineCssTextArea implements I18nAdapter, NodeAdapter, ThemeAdapter, FontAdapter, TextAdapter, TipAdapter, StateAdapter {

    {
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
    public synchronized void replaceText(int start, int end, String text) {
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }
        if (end > this.getLength()) {
            end = this.getLength();
        }
        if (start > end) {
            start = end;
        }
        int finalStart = start;
        int finalEnd = end;
        FXUtil.runWait(() -> super.replaceText(finalStart, finalEnd, text));
    }

    @Override
    public String getText() {
        return super.getText();
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
        if (CollectionUtil.isNotEmpty(list)) {
            String str = CollectionUtil.join(list, "\n");
            this.appendText(str + "\n");
        }
    }

    /**
     * 追加行
     *
     * @param s 文本内容
     */
    public void appendLine(String s) {
        this.appendLine(s, true);
    }

    /**
     * 追加行
     *
     * @param s       文本内容
     * @param endLine 是否追加末尾行符号
     */
    public void appendLine(String s, boolean endLine) {
        if (s != null) {
            String text = this.getText();
            if (text != null && !text.isEmpty() && !text.endsWith("\n") && !s.startsWith("\n")) {
                s = "\n" + s;
            }
            if (endLine && !s.endsWith("\n")) {
                s = s + "\n";
            }
            this.appendText(s);
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
            return Math.max(super.getLength(), 0);
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

    // /**
    //  * 滚动到尾部
    //  */
    // public void scrollToEnd() {
    //     ExecutorUtil.start(() -> {
    //         int len = this.getLength() - 1;
    //         if (len > 0) {
    //             this.selectRange(len - 1, len);
    //             this.deselect();
    //             this.positionCaret(this.getLength());
    //         }
    //     }, 150);
    // }

    public void positionCaret(int caretPosition) {
        FXUtil.runWait(() -> this.displaceCaret(caretPosition));
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
     * 检查样式是否失效
     *
     * @return 结果
     */
    public boolean checkInvalidStyle() {
        String md5 = this.getProp("style:md5");
        String textMd5 = MD5Util.md5Hex(this.getText());
        if (!StringUtil.equals(md5, textMd5)) {
            this.setProp("style:md5", textMd5);
            return true;
        }
        return false;
    }

    /**
     * 初始化文字样式
     */
    public void initTextStyle() {
        if (!this.checkInvalidStyle()) {
            return;
        }
        CostUtil.record();
        this.clearTextStyle();
        // 初始化颜色
        if (this.isEnableTheme()) {
            Node placeholder = this.getPlaceholder();
            CaretNode caretNode = this.getCaretSelectionBind().getUnderlyingCaret();
            Color accentColor = ThemeManager.currentAccentColor();
            Color foregroundColor = ThemeManager.currentForegroundColor();
            String fgColor = FXColorUtil.getColorHex(foregroundColor);
            this.setStyle(0, this.getLength(), "-fx-fill:" + fgColor);
            caretNode.setStroke(accentColor);
            if (placeholder != null) {
                placeholder.setStyle("-fx-fill:" + fgColor);
            }
        }
        CostUtil.printCost();
    }

    /**
     * 设置样式
     *
     * @param style 富文本样式
     */
    public void setStyle(RichTextStyle style) {
        if (style != null) {
            FXUtil.runWait(() -> this.setStyle(style.start(), style.end(), style.style()));
        }
    }

    @Override
    public void setStyle(int from, int to, String style) {
        if (from < 0) {
            from = 0;
        }
        if (to < from) {
            to = from;
        }
        if (to > this.getLength()) {
            to = this.getLength();
        }
        super.setStyle(from, to, style);
    }

    /**
     * 设置样式
     *
     * @param styles 富文本样式
     */
    public void setStyles(List<RichTextStyle> styles) {
        if (CollectionUtil.isNotEmpty(styles)) {
            FXUtil.runWait(() -> {
                for (RichTextStyle style : styles) {
                    this.setStyle(style.start(), style.end(), style.style());
                }
            });
        }
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
//        if (this.isEnableTheme()) {
//            Node placeholder = this.getPlaceholder();
//            CaretNode caretNode = this.getCaretSelectionBind().getUnderlyingCaret();
//            if (style.isDarkMode()) {
//                this.setStyle(0, this.getLength(), "-fx-fill: #fff;");
//                caretNode.setStroke(Color.WHITE);
//                if (placeholder != null) {
//                    placeholder.setStyle("-fx-fill: #fff;");
//                }
//            } else {
//                this.setStyle(0, this.getLength(), "-fx-fill: #000");
//                caretNode.setStroke(Color.BLACK);
//                if (placeholder != null) {
//                    placeholder.setStyle("-fx-fill: #000;");
//                }
//            }
//        }
        // 移除样式缓存
        this.removeProp("style:md5");
        this.initTextStyle();
    }

    @Override
    public void initNode() {
        this.setWrapText(true);
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
        this.setAutoScrollOnDragDesired(true);
        this.setPadding(new Insets(5, 5, 5, 5));
        Color color = ThemeManager.currentAccentColor();
        BorderStroke stroke = new BorderStroke(color, BorderStrokeStyle.SOLID, null, new BorderWidths(1));
        this.setBorder(new Border(stroke));
        this.applyPlainUndoManager();
        this.getStyleClass().add("rich-text-area");
    }

    @Override
    public void requestFocus() {
        FXUtil.runWait(super::requestFocus, 1);
    }

    @Override
    public void changeLocale(Locale locale) {
        this.setLocale(locale);
    }

    /**
     * 获取选区行
     *
     * @return 选区行 键: 行号 值: 行内容
     */
    public Map<Integer, String> getSelectionLines() {
        IndexRange range = this.getSelection();
        if (range == null) {
            return Collections.emptyMap();
        }
        Map<Integer, String> map = new HashMap<>();
        AtomicInteger count = new AtomicInteger();
        AtomicInteger line = new AtomicInteger();
        this.getText().lines().forEach(str -> {
            int len = str.length();
            long lineStart = count.get();
            long lineEnd = count.get() + len + 1;
            if (NumberUtil.checkBound(lineStart, lineEnd, range.getStart(), range.getEnd())) {
                map.put(line.get(), str);
            }
            line.incrementAndGet();
            count.addAndGet(len + 1);
        });
        return map;
    }
}
