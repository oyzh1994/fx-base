package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.adapter.MouseAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FXButton extends Button implements ThemeAdapter, MouseAdapter, TipAdapter, StateAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
        this.setCursor(Cursor.HAND);
        this.setPickOnBounds(true);
        this.setMnemonicParsing(false);
        this.setFocusTraversable(false);
    }

    public FXButton() {
        super();
    }

    public FXButton(String text) {
        super(text);
    }

    @Override
    public void setOnMousePrimaryClicked(EventHandler<? super MouseEvent> handler) {
        MouseAdapter.super.setOnMousePrimaryClicked(handler);
    }

    @Override
    public EventHandler<? super MouseEvent> getOnMousePrimaryClicked() {
        return MouseAdapter.super.getOnMousePrimaryClicked();
    }

    @Override
    public void setOnMouseSecondClicked(EventHandler<? super MouseEvent> handler) {
        MouseAdapter.super.setOnMouseSecondClicked(handler);
    }

    @Override
    public EventHandler<? super MouseEvent> getOnMouseSecondClicked() {
        return MouseAdapter.super.getOnMouseSecondClicked();
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
}
