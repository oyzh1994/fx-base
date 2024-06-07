package cn.oyzh.fx.plus.controls.tab;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.adapter.ContextMenuAdapter;
import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.FontWeight;
import lombok.NonNull;

import java.util.List;

/**
 * @author oyzh
 * @since 2022/1/20
 */
public class FlexTabPane extends TabPane implements ThemeAdapter, FontAdapter, ContextMenuAdapter, FlexAdapter, SelectAdapter<Tab> {

    {
        NodeManager.init(this);
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
        if (StrUtil.isNotBlank(className)) {
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
        if (StrUtil.isNotBlank(className) && tab != null) {
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
    public void selectedTabChanged(@NonNull ChangeListener<Tab> listener) {
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
    public void addTab(@NonNull Tab tab) {
        FXUtil.runWait(() -> this.getTabs().add(tab));
    }

    /**
     * 移除tab
     *
     * @param tab tab
     */
    public void removeTab(@NonNull Tab tab) {
        FXUtil.runLater(() -> this.getTabs().remove(tab));
    }

    /**
     * 移除tab
     *
     * @param tabs tabs
     */
    public void removeTab(List<? extends Tab> tabs) {
        if (CollUtil.isNotEmpty(tabs)) {
            FXUtil.runLater(() -> this.getTabs().removeAll(tabs));
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
    public void setStateManager(StateManager manager) {
        FlexAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return FlexAdapter.super.stateManager();
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

    @Override
    public void initNode() {

    }
}
