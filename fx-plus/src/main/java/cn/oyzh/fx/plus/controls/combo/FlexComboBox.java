package cn.oyzh.fx.plus.controls.combo;

import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.validator.BaseValidator;
import cn.oyzh.fx.plus.validator.Verifiable;
import javafx.beans.value.ChangeListener;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Collection;

/**
 * @author oyzh
 * @since 2022/2/7
 */
public class FlexComboBox<T> extends ComboBox<T> implements ThemeAdapter, FlexAdapter, Verifiable<BaseValidator>, SelectAdapter<T>, TipAdapter, StateAdapter, FontAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
        this.setPickOnBounds(true);
        this.setCursor(Cursor.HAND);
        this.setFocusTraversable(false);
    }

    @Getter
    private Boolean require;

    @Getter
    @Setter
    private BaseValidator validator = new BaseValidator(this);

    @Override
    public void setTipText(String tipTitle) {
        TipAdapter.super.tipText(tipTitle);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

    public void setRequire(Boolean require) {
        this.require = require;
        this.validator.addRequiredVerifier(require, Integer.MIN_VALUE);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
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
    public void setInitIndex(int initIndex) {
        SelectAdapter.super.setInitIndex(initIndex);
    }

    @Override
    public int getInitIndex() {
        return SelectAdapter.super.getInitIndex();
    }

    /**
     * 选中内容变化事件
     *
     * @param listener 监听器
     */
    public void selectedItemChanged(@NonNull ChangeListener<T> listener) {
        this.getSelectionModel().selectedItemProperty().addListener((observableValue, t, t1) -> {
            if (!this.isIgnoreChanged()) {
                listener.changed(observableValue, t, t1);
            }
        });
    }

    /**
     * 添加数据
     *
     * @param item 数据
     */
    public void addItem(@NonNull T item) {
        FXUtil.runWait(() -> this.getItems().add(item));
    }

    /**
     * 是否包含数据
     *
     * @param item 数据
     */
    public boolean containsItem(@NonNull T item) {
        return this.getItems().contains(item);
    }

    /**
     * 添加多个数据
     *
     * @param collection 数据集合
     */
    public void addItems(@NonNull Collection<T> collection) {
        FXUtil.runWait(() -> this.getItems().addAll(collection));
    }

    /**
     * 添加多个数据
     *
     * @param items 数据数组
     */
    public void addItems(@NonNull T[] items) {
        FXUtil.runWait(() -> this.getItems().addAll(items));
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
    public void setStateManager(StateManager manager) {
        FlexAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return FlexAdapter.super.stateManager();
    }
}
