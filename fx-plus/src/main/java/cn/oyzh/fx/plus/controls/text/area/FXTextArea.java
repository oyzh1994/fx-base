package cn.oyzh.fx.plus.controls.text.area;

import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.LimitLenControl;
import cn.oyzh.fx.plus.LimitLineControl;
import cn.oyzh.fx.plus.adapter.AreaAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.operator.LimitOperator;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.validator.BaseValidator;
import cn.oyzh.fx.plus.validator.Verifiable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.FontWeight;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Collection;

/**
 * 文本域
 *
 * @author oyzh
 * @since 2022/1/20
 */
@Getter
public class FXTextArea extends TextArea implements LimitLineControl, LimitLenControl, NodeGroup, NodeAdapter, ThemeAdapter, AreaAdapter, Verifiable<BaseValidator>, TipAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 最大行数
     */
    @Getter
    private Long maxLine;

    /**
     * 最大长度
     */
    @Getter
    private Long maxLen;

    @Override
    public void setMaxLine(Long maxLine) {
        this.maxLine = maxLine;
        if (maxLine != null && this.getTextFormatter() == null) {
            this.setTextFormatter(new TextFormatter<>(new LimitOperator()));
        }
    }

    @Override
    public void setMaxLen(Long maxLen) {
        this.maxLen = maxLen;
        if (maxLen != null && this.getTextFormatter() == null) {
            this.setTextFormatter(new TextFormatter<>(new LimitOperator()));
        }
    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
        if (this.getPromptText() == null || this.getPromptText().isEmpty()) {
            this.setPromptText(tipText);
        }
    }

//    @Override
//    public String getTipText() {
//        return TipAdapter.super.tipText();
//    }

    /**
     * 追加多行
     *
     * @param list 文本列表
     */
    public void appendLines(Collection<String> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            String str = CollectionUtil.join(list, System.lineSeparator());
            this.appendText(str + System.lineSeparator());
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
            if (this.getLength() > 0 && !this.getText().endsWith(System.lineSeparator()) && !text.startsWith(System.lineSeparator())) {
                text = System.lineSeparator() + text;
            }
            if (!text.endsWith(System.lineSeparator())) {
                text = text + System.lineSeparator();
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
        ExecutorUtil.start(this::_scrollToEnd, 150);
    }

    public void _scrollToEnd() {
        int len = this.getLength() - 1;
        if (len > 0) {
            this.selectRange(len - 1, len);
            this.deselect();
            this.positionCaret(this.getLength());
        }
    }

//    @Override
//    public void setFontSize(double fontSize) {
//        AreaAdapter.super.fontSize(fontSize);
//    }
//
//    @Override
//    public double getFontSize() {
//        return AreaAdapter.super.fontSize();
//    }
//
//    @Override
//    public String getFontFamily() {
//        return AreaAdapter.super.fontFamily();
//    }
//
//    @Override
//    public void setFontFamily(@NonNull String fontFamily) {
//        AreaAdapter.super.fontFamily(fontFamily);
//    }
//
//    @Override
//    public void setFontWeight(FontWeight fontWeight) {
//        AreaAdapter.super.fontWeight(fontWeight);
//    }
//
//    @Override
//    public FontWeight getFontWeight() {
//        return AreaAdapter.super.fontWeight();
//    }

    private Boolean require;

    @Setter
    private BaseValidator validator = new BaseValidator(this);

    public void setRequire(Boolean require) {
        this.require = require;
        this.validator.addRequiredVerifier(require, Integer.MIN_VALUE);
    }

    @Override
    public void flushCaret() {
        this.positionCaret(this.getCaretPosition());
        this.requestFocus();
    }

    // @Override
    // public void setStateManager(StateManager manager) {
    //     StateAdapter.super.stateManager(manager);
    // }
    //
    // @Override
    // public StateManager getStateManager() {
    //     return StateAdapter.super.stateManager();
    // }

    @Override
    public void initNode() {
        this.setWrapText(true);
        this.setOnScroll(event -> {
            if (event.isControlDown()) {
                if (event.getDeltaY() > 0) {
                    this.fontSizeIncr();
                } else {
                    this.fontSizeDecr();
                }
            }
        });
    }

    @Override
    public void setTextExt(String text) {
        FXUtil.runWait(() -> this.setText(text));
    }

    @Override
    public void requestFocus() {
        TaskManager.startDelay(() -> FXUtil.runWait(super::requestFocus), 1);
    }
//
//    @Override
//    public void setGroupId(String groupId) {
//        NodeGroup.super.groupId(groupId);
//    }
//
//    @Override
//    public String getGroupId() {
//        return NodeGroup.super.groupId();
//    }

    public long lineCount() {
        return this.getText().lines().count();
    }
}
