package cn.oyzh.fx.plus.controls;


import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.CacheHint;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.Setter;

/**
 * 基础文本域
 *
 * @author oyzh
 * @since 2023/08/15
 */
public class BaseTextField extends TextField implements FlexAdapter, TextAdapter, TipAdapter, StateAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setPickOnBounds(true);
        this.setCacheHint(CacheHint.QUALITY);
    }

    @Getter
    @Setter
    private boolean require;

    public BaseTextField() {
        super.setText("");
    }

    public BaseTextField(String text) {
        super.setText(text);
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
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
        this.setPromptText(tipText);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return this.getLength() == 0 || this.getText().isEmpty();
    }

    /**
     * 校验数据
     *
     * @return 结果
     */
    public boolean validate() {
        return true;
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
