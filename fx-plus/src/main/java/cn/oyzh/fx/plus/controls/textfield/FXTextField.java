package cn.oyzh.fx.plus.controls.textfield;


import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
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
public class FXTextField extends TextField implements  TextAdapter, TipAdapter, StateAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setPickOnBounds(true);
        this.setCacheHint(CacheHint.QUALITY);
    }

    @Getter
    @Setter
    private boolean require;

    public FXTextField() {
        super.setText("");
    }

    public FXTextField(String text) {
        super.setText(text);
    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
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
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }
}
