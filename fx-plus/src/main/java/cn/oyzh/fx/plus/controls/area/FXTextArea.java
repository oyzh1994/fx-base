package cn.oyzh.fx.plus.controls.area;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.plus.adapter.AreaAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.validator.BaseValidator;
import cn.oyzh.fx.plus.validator.Verifiable;
import javafx.scene.control.TextArea;
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
public class FXTextArea extends TextArea implements NodeAdapter, ThemeAdapter, AreaAdapter, Verifiable<BaseValidator>, TipAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 行分隔符
     */
    public static final String LINE_SEPARATOR = "\n";

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
        if (this.getPromptText() == null || this.getPromptText().isEmpty()) {
            this.setPromptText(tipText);
        }
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

    /**
     * 追加多行
     *
     * @param list 文本列表
     */
    public void appendLines(Collection<String> list) {
        if (CollUtil.isNotEmpty(list)) {
            String str = CollUtil.join(list, FXTextArea.LINE_SEPARATOR);
            this.appendText(str + FXTextArea.LINE_SEPARATOR);
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
            if (this.getLength() > 0 && !this.getText().endsWith(FXTextArea.LINE_SEPARATOR) && !text.startsWith(FXTextArea.LINE_SEPARATOR)) {
                text = FXTextArea.LINE_SEPARATOR + text;
            }
            if (!text.endsWith(FXTextArea.LINE_SEPARATOR)) {
                text = text + FXTextArea.LINE_SEPARATOR;
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

    @Override
    public void setFontSize(double fontSize) {
        AreaAdapter.super.fontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return AreaAdapter.super.fontSize();
    }

    @Override
    public String getFontFamily() {
        return AreaAdapter.super.fontFamily();
    }

    @Override
    public void setFontFamily(@NonNull String fontFamily) {
        AreaAdapter.super.fontFamily(fontFamily);
    }

    @Override
    public void setFontWeight(FontWeight fontWeight) {
        AreaAdapter.super.fontWeight(fontWeight);
    }

    @Override
    public FontWeight getFontWeight() {
        return AreaAdapter.super.fontWeight();
    }

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

    @Override
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }

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
}
