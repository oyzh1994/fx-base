package cn.oyzh.fx.plus.controls.tab;

import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * @author oyzh
 * @since 2022/1/21
 */
public class FXTab extends Tab implements MenuItemAdapter, NodeGroup, NodeAdapter, ThemeAdapter, StateAdapter, TipAdapter {

    {
        NodeManager.init(this);
        this.setOnClosed(this::onTabClosed);
        this.setOnCloseRequest(this::onTabCloseRequest);
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

//    @Override
//    public void setTipText(String tipText) {
//        TipAdapter.super.tipText(tipText);
//    }
//
//    @Override
//    public String getTipText() {
//        return TipAdapter.super.tipText();
//    }

    // @Override
    // public void setStateManager(StateManager manager) {
    //     StateAdapter.super.stateManager(manager);
    // }
    //
    // @Override
    // public StateManager getStateManager() {
    //     return StateAdapter.super.stateManager();
    // }

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

    @Override
    public void initNode() {
        this.setClosable(false);
    }

    protected void onTabClosed(Event event) {

    }

    protected void onTabCloseRequest(Event event) {

    }

//    @Override
//    public void setGroupId(String groupId) {
//        NodeGroup.super.groupId(groupId);
//    }
//
//    @Override
//    public String getGroupId() {
//        return NodeGroup.super.groupId();
//    }

}
