package cn.oyzh.fx.plus.controls.tab;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.menu.ContextMenuAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author oyzh
 * @since 2022/1/20
 */
public class FXTabPane extends TabPane implements FlexAdapter, NodeGroup, ThemeAdapter, FontAdapter, ContextMenuAdapter, SelectAdapter<Tab> {

    {
        NodeManager.init(this);
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
     * 移除tab的class类
     *
     * @param className class名称
     */
    public void removeTabClass(String className) {
        if (StringUtil.isNotBlank(className)) {
            this.getTabs().forEach(t -> t.getStyleClass().remove(className));
        }
    }

    /**
     * 给选中tab添加class类
     *
     * @param className class名称
     */
    public void addTabClass(String className) {
        this.addTabClass(this.getSelectedItem(), className);
    }

    /**
     * 给tab添加class类
     *
     * @param tab       tab
     * @param className class名称
     */
    public void addTabClass(Tab tab, String className) {
        if (StringUtil.isNotBlank(className) && tab != null) {
            if (!tab.getStyleClass().contains("tab-active")) {
                tab.getStyleClass().add("tab-active");
            }
        }
    }

    /**
     * 重新选中节点
     */
    public void reselect() {
        Tab tab = this.getSelectedItem();
        this.clearSelection();
        this.select(tab);
    }

    /**
     * 选中内容变化事件
     *
     * @param listener 监听器
     */
    public void selectedTabChanged(ChangeListener<Tab> listener) {
        this.getSelectionModel().selectedItemProperty().addListener((observableValue, t, t1) -> {
            if (!this.isIgnoreChanged()) {
                listener.changed(observableValue, t, t1);
            }
        });
    }

    /**
     * 当前tab数量
     *
     * @return tab数量
     */
    protected int tabsSize() {
        return this.getTabs().size();
    }

    /**
     * 当前tab是否为空
     *
     * @return 结果
     */
    protected boolean tabsEmpty() {
        return this.getTabs().isEmpty();
    }

    /**
     * 添加tab
     *
     * @param tab tab
     */
    public void addTab(Tab tab) {
        FXUtil.runWait(() -> this.getTabs().add(tab));
    }

    /**
     * 设置tab
     *
     * @param tab tab
     */
    public void setTab(Tab tab) {
        FXUtil.runWait(() -> this.getTabs().setAll(tab));
    }

    /**
     * 设置tab
     *
     * @param tabs tab列表
     */
    public void setTab(Tab... tabs) {
        FXUtil.runWait(() -> this.getTabs().setAll(tabs));
    }

    /**
     * 设置tab
     *
     * @param index 索引
     * @param tab   tab
     */
    public void setTab(int index, Tab tab) {
        FXUtil.runWait(() -> this.getTabs().set(index, tab));
    }

    /**
     * 添加tab
     *
     * @param index 索引
     * @param tab   tab
     */
    public void addTab(int index, Tab tab) {
        FXUtil.runWait(() -> this.getTabs().add(index, tab));
    }

    /**
     * 设置tab
     *
     * @param tabs tab列表
     */
    public void setTab(Collection<Tab> tabs) {
        FXUtil.runWait(() -> this.getTabs().setAll(tabs));
    }

    /**
     * 移除tab
     *
     * @param tab tab
     */
    public void removeTab(Tab tab) {
        FXUtil.runLater(() -> this.getTabs().remove(tab));
    }

    /**
     * 移除tab
     *
     * @param tabIndex tab索引
     */
    public void removeTab(int tabIndex) {
        FXUtil.runLater(() -> this.getTabs().remove(tabIndex));
    }

    /**
     * 移除tab
     *
     * @param tabId tabId
     */
    public void removeTab(String tabId) {
        for (Tab tab : this.getTabs()) {
            if (StringUtil.equals(tabId, tab.getId())) {
                this.removeTab(tab);
                break;
            }
        }
    }

    /**
     * 移除tab
     *
     * @param tabIds tabId列表
     */
    public void removeTabs(String... tabIds) {
        List<Tab> tabs = new ArrayList<>(tabIds.length);
        for (Tab tab : this.getTabs()) {
            if (StringUtil.equalsAny(tab.getId(), tabIds)) {
                tabs.add(tab);
            }
        }
        this.removeTab(tabs);
    }

    /**
     * 选择tab
     *
     * @param tabId tabId
     */
    public void selectTab(String tabId) {
        for (Tab tab : this.getTabs()) {
            if (StringUtil.equals(tabId, tab.getId())) {
                this.select(tab);
                break;
            }
        }
    }

    /**
     * 移除tab
     *
     * @param tabs tabs
     */
    public void removeTab(List<? extends Tab> tabs) {
        if (CollectionUtil.isNotEmpty(tabs)) {
            FXUtil.runLater(() -> this.getTabs().removeAll(tabs));
        }
    }

    /**
     * tab选中事件
     *
     * @param tabId tabId
     * @param task  业务
     */
    public void onTabSelected(String tabId, Runnable task) {
        if (tabId != null && task != null) {
            this.selectedTabChanged((observable, oldValue, newValue) -> {
                if (newValue != null && StringUtil.equals(tabId, newValue.getId())) {
                    task.run();
                }
            });
        }
    }

    /**
     * 禁用其他tab
     *
     * @param current 当前tab
     */
    public void disableOtherTab(Tab current) {
        if (current != null) {
            FXUtil.runLater(() -> this.getTabs().forEach(t -> {
                if (t != current) {
                    t.setDisable(true);
                }
            }));
        }
    }

    /**
     * 启用所有tab
     */
    public void enableTabs() {
        FXUtil.runLater(() -> this.getTabs().forEach(t -> t.setDisable(false)));
    }

    public boolean isSelectedTab(String tabId) {
        if (tabId == null) {
            return false;
        }
        Tab tab = this.getSelectedItem();
        return tab != null && StringUtil.equals(tabId, tab.getId());
    }

    public void setTabHeight(double height) {
        this.setTabMaxHeight(height);
        this.setTabMinHeight(height);
    }

    public <T extends Tab> T getTab(int index) {
        if (index < this.getTabs().size()) {
            return (T) this.getTabs().get(index);
        }
        return null;
    }

    public int tabSize() {
        return this.getTabs().size();
    }

    public String getSelectTabId() {
        Tab tab = this.getSelectedItem();
        return tab == null ? null : tab.getId();
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public void resizeNode(Double width, Double height) {
        FlexAdapter.super.resizeNode(width, height);
        for (Tab tab : this.getTabs()) {
            if (tab.getContent() instanceof FlexAdapter flexNode) {
                flexNode.setRealWidth(FlexUtil.compute(flexNode.getFlexWidth(), width));
                flexNode.setRealHeight(FlexUtil.compute(flexNode.getFlexHeight(), height));
            } else {
                NodeUtil.setWidth(tab.getContent(), width);
                NodeUtil.setHeight(tab.getContent(), height);
            }
        }
    }

    @Override
    public void initNode() {
        FlexAdapter.super.initNode();
        this.setTabRealHeight(20);
    }

    public void setTabRealHeight(double tabHeight) {
        super.setTabMaxHeight(tabHeight);
        super.setTabMinHeight(tabHeight);
    }

    public double getTabRealHeight() {
        return Math.max(this.getTabMaxHeight(), this.getTabMinHeight());
    }
}
