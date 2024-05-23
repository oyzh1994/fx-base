package cn.oyzh.fx.plus.controls.toggle;

import atlantafx.base.controls.ToggleSwitch;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import javafx.beans.value.ChangeListener;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.text.FontWeight;
import lombok.Getter;
import lombok.NonNull;

/**
 * 切换开关组件
 *
 * @author oyzh
 * @since 2023/12/19
 */
public class FXToggleSwitch extends ToggleSwitch implements TipAdapter, StateAdapter, FontAdapter {

    /**
     * 选中时显示的文字
     */
    @Getter
    private String selectedText;

    /**
     * 未选中时显示的文字
     */
    @Getter
    private String unselectedText;

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCursor(Cursor.HAND);
        this.setCacheHint(CacheHint.QUALITY);
        // 选中变化事件
        this.selectedChanged((observable, oldValue, t1) -> {
            if (t1) {
                // 初始化选中按钮
                this.setText(this.selectedText);
            } else {
                this.setText(this.unselectedText);
            }
        });
    }

    /**
     * 设置选中文字
     *
     * @param selectedText 选中文字
     */
    public void setSelectedText(String selectedText) {
        this.selectedText = selectedText;
        if (this.isSelected()) {
            this.setText(this.selectedText);
        }
    }

    /**
     * 设置未选中文字
     *
     * @param unselectedText 未选中文字
     */
    public void setUnselectedText(String unselectedText) {
        this.unselectedText = unselectedText;
        if (!this.isSelected()) {
            this.setText(this.unselectedText);
        }
    }

    /**
     * 选中变化事件
     *
     * @param listener 监听器
     */
    public void selectedChanged(@NonNull ChangeListener<Boolean> listener) {
        this.selectedProperty().addListener(listener);
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
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
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
    public void setFontWeight(FontWeight fontWeight) {
        FontAdapter.super.fontWeight(fontWeight);
    }

    @Override
    public FontWeight getFontWeight() {
        return FontAdapter.super.fontWeight();
    }
}
