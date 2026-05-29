package cn.oyzh.fx.plus.controls.tab;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Tab;

import java.util.List;

/**
 * @author oyzh
 * @since 2022/1/21
 */
public class FXTab extends Tab implements FontAdapter, MenuItemAdapter, NodeGroup, NodeAdapter, ThemeAdapter, StateAdapter, TipAdapter, Destroyable {

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
     * 获取tab列表
     *
     * @return tab列表
     */
    public ObservableList<Tab> tabs() {
        return this.getTabPane().getTabs();
    }

    /**
     * 关闭当前tab
     */
    public void closeTab() {
        this.closeTabs(List.of(this));
    }

    /**
     * 关闭多个tab
     *
     * @param tabs tab列表
     */
    public void closeTabs(List<Tab> tabs) {
        FXUtil.runWait(() -> {
            for (Tab tab : tabs) {
                if (tab.isClosable()) {
                    this.tabs().remove(tab);
                    // 手动触发关闭事件
                    Event.fireEvent(tab, new Event(Tab.CLOSED_EVENT));
                }
            }
        });
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
     * 刷新tab
     */
    public void flush() {
        FXUtil.runWait(() -> {
            this.flushGraphic();
            this.flushGraphicColor();
            this.flushTitle();
        });
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
//        List<? extends MenuItem> items = this.getMenuItems();
//        if (CollectionUtil.isNotEmpty(items)) {
//            ContextMenu contextMenu = ContextMenuManager.createNewContextMenu(items);
//            ContextMenuManager.setContextMenu(this, contextMenu);
//        }
        NodeAdapter.super.initNode();
    }

    /**
     * tab关闭事件
     *
     * @param event 事件
     */
    protected void onTabClosed(Event event) {
    }

    protected void onTabCloseRequest(Event event) {

    }

    public void setAppendText(String appendText) {
        if (StringUtil.isEmpty(appendText)) {
            return;
        }
        String text;
        if (this.hasProp("appendText")) {
            text = this.getProp("text");
        } else {
            text = this.getText();
        }
        if (StringUtil.isEmpty(text)) {
            this.setText(appendText);
        } else {
            this.setText(text + appendText);
        }
        this.setProp("text", text);
        this.setProp("appendText", appendText);
    }

    public String getAppendText() {
        return this.getProp("appendText");
    }

    @Override
    public void destroy() {
        this.setContent(null);
        this.setContextMenu(null);
        NodeDestroyUtil.destroyObject(this);
    }
}
