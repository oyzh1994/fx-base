package cn.oyzh.fx.plus.controls.tab;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeDisposeUtil;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * @author oyzh
 * @since 2022/1/21
 */
public class FXTab extends Tab implements MenuItemAdapter, NodeGroup, NodeAdapter, ThemeAdapter, StateAdapter, TipAdapter {

    {
        NodeManager.init(this);
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

    /**
     * 关闭当前tab
     */
    public void closeTab() {
        if (this.isClosable()) {
            TabPane tabPane = this.getTabPane();
            if (tabPane != null) {
                FXUtil.runLater(() -> tabPane.getTabs().remove(this));
                // 手动触发关闭事件
                Event.fireEvent(this, new Event(Tab.CLOSED_EVENT));
            }
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
        this.setOnClosed(this::onTabClosed);
        this.setOnCloseRequest(this::onTabCloseRequest);
//        this.addEventFilter(Tab.CLOSED_EVENT, this::onTabClosed);
//        this.addEventFilter(Tab.TAB_CLOSE_REQUEST_EVENT, this::onTabCloseRequest);
    }

    /**
     * tab关闭事件
     *
     * @param event 事件
     */
    protected void onTabClosed(Event event) {
        NodeDisposeUtil.dispose(this);
    }

    protected void onTabCloseRequest(Event event) {

    }

    public void setAppendText(String appendText) {
        if (StringUtil.isEmpty(appendText)) {
            return;
        }
        String text = this.getText();
        if (StringUtil.isEmpty(text)) {
            this.setText(appendText);
        } else {
            this.setText(text + appendText);
        }
        this.setProp("appendText", appendText);
    }

    public String getAppendText() {
        return this.getProp("appendText");
    }
}
