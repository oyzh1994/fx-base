package cn.oyzh.fx.plus.controls.tab;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * @author oyzh
 * @since 2022/1/21
 */
public class FXTab extends Tab implements ThemeAdapter, StateAdapter, TipAdapter {

    {
        this.setClosable(false);
        this.changeTheme(ThemeManager.currentTheme());
    }

    public FXTab() {
        super();
    }

    public FXTab(String text) {
        super(text);
    }

    public FXTab(String text, Node content) {
        super(text, content);
    }

    @Override
    public void disable() {
        StateAdapter.super.disable();
        if (this.getGraphic() != null) {
            this.getGraphic().setDisable(true);
        }
    }

    @Override
    public void enable() {
        StateAdapter.super.enable();
        if (this.getGraphic() != null) {
            this.getGraphic().setDisable(false);
        }
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

    /**
     * 关闭当前tab
     */
    public void closeTab() {
        if (this.isClosable()) {
            FXUtil.runLater(() -> this.getTabPane().getTabs().remove(this));
        }
    }

    /**
     * 选中当前tab
     */
    public void selectTab() {
        if (!this.isSelected()) {
            FXUtil.runLater(() -> this.getTabPane().getSelectionModel().select(this));
        }
    }

    /**
     * 刷新tab标题
     */
    public void flushTitle() {
    }

    /**
     * 刷新tab图标
     */
    public void flushGraphic() {
    }

    /**
     * 刷新tab图标颜色
     */
    public void flushGraphicColor() {
    }
}
