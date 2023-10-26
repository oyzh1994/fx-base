package cn.oyzh.fx.plus.controls;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.plus.adapter.AreaAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.validator.BaseValidator;
import cn.oyzh.fx.plus.validator.Verifiable;
import javafx.scene.CacheHint;
import javafx.scene.control.TextArea;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

/**
 * 文本域
 *
 * @author oyzh
 * @since 2022/1/20
 */
@ToString
public class FlexTextArea extends TextArea implements AreaAdapter, FlexAdapter, Verifiable<BaseValidator>, TipAdapter, ThemeAdapter {

    {
        this.setCache(true);
        this.setWrapText(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
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
    public void setTipText(String tipTitle) {
        TipAdapter.super.tipText(tipTitle);
        this.setPromptText(tipTitle);
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

    @Getter
    private Boolean require;

    @Getter
    @Setter
    private BaseValidator validator = new BaseValidator(this);

    public void setRequire(Boolean require) {
        this.require = require;
        this.validator.addRequiredVerifier(require, Integer.MIN_VALUE);
    }

    @Override
    public void resize(double width, double height) {
        double computeWidth = this.computeWidth(width);
        double computeHeight = this.computeHeight(height);
        super.resize(computeWidth, computeHeight);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        this.resizeNode();
    }

    @Override
    public String getFlexWidth() {
        return FlexAdapter.super.flexWidth();
    }

    @Override
    public void setFlexWidth(String flexWidth) {
        FlexAdapter.super.flexWidth(flexWidth);
    }

    public String getFlexHeight() {
        return FlexAdapter.super.flexHeight();
    }

    @Override
    public void setFlexHeight(String flexHeight) {
        FlexAdapter.super.flexHeight(flexHeight);
    }

    @Override
    public String getFlexX() {
        return FlexAdapter.super.flexX();
    }

    @Override
    public void setFlexX(String flexX) {
        FlexAdapter.super.flexX(flexX);
    }

    @Override
    public String getFlexY() {
        return FlexAdapter.super.flexY();
    }

    @Override
    public void setFlexY(String flexY) {
        FlexAdapter.super.flexY(flexY);
    }

    @Override
    public double getRealWidth() {
        return FlexAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        FlexAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return FlexAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        FlexAdapter.super.realHeight(height);
    }

    @Override
    public void flushCaret() {
        this.positionCaret(this.getCaretPosition());
        this.requestFocus();
    }

    @Override
    public void setStateManager(StateManager manager) {
        FlexAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return FlexAdapter.super.stateManager();
    }
}
