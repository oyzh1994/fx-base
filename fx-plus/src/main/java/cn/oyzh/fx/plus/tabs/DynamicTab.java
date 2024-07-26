package cn.oyzh.fx.plus.tabs;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.menu.CloseAllTabMenuItem;
import cn.oyzh.fx.plus.menu.CloseCurrTabMenuItem;
import cn.oyzh.fx.plus.menu.CloseLeftTabMenuItem;
import cn.oyzh.fx.plus.menu.CloseOtherTabMenuItem;
import cn.oyzh.fx.plus.menu.CloseRightTabMenuItem;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.paint.Paint;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态tab
 *
 * @author oyzh
 * @since 2023/11/03
 */
public abstract class DynamicTab extends FXTab {

    public DynamicTab() {
        // 加载内容
        this.loadContent();
        this.setClosable(true);
    }

    /**
     * controller
     */
    @Getter
    @Accessors(fluent = true, chain = false)
    protected DynamicTabController controller;

    /**
     * 加载内容
     */
    protected void loadContent() {
        String url = this.url();
        if (url != null) {
            FXMLLoaderExt loaderExt = new FXMLLoaderExt();
            Node content = loaderExt.load(url);
            this.setContent(content);
            this.controller = loaderExt.getController();
            this.controller.onTabInit(this);
            this.setOnClosed(e -> this.controller.onTabClose(this, e));
        }
    }

    /**
     * 获取tab内容地址
     *
     * @return tab内容地址
     */
    protected String url() {
        return null;
    }

    /**
     * 重载tab
     */
    public void reload() {
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
     * 获取标题
     *
     * @return 标题
     */
    public String getTitle() {
        return this.getText();
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        FXUtil.runWait(() -> {
            this.setText(title);
            this.setTipText(title);
            Node node = this.getGraphic();
            if (node instanceof SVGGlyph glyph) {
                glyph.setTipText(title);
            } else if (node instanceof SVGLabel label) {
                label.setTipText(title);
            }
        });
    }

    /**
     * 设置图标
     *
     * @param graphic 图标
     */
    protected void graphic(Node graphic) {
        FXUtil.runWait(() -> this.setGraphic(graphic));
    }

    /**
     * 设置颜色
     *
     * @param paint 颜色
     */
    protected void fill(Paint paint) {
        FXUtil.runWait(() -> {
            Node node = this.getGraphic();
            if (node instanceof SVGGlyph glyph) {
                glyph.setColor(paint);
            } else if (node instanceof SVGLabel label) {
                label.setColor(paint);
                label.setTextFill(paint);
            }
        });
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        CloseCurrTabMenuItem closeTab = new CloseCurrTabMenuItem(this::closeTab);
        CloseLeftTabMenuItem closeLeftTab = new CloseLeftTabMenuItem(this::closeLeftTab);
        CloseRightTabMenuItem closeRightTab = new CloseRightTabMenuItem(this::closeRightTab);
        CloseOtherTabMenuItem closeOtherTab = new CloseOtherTabMenuItem(this::closeOtherTab);
        CloseAllTabMenuItem closeAllTab = new CloseAllTabMenuItem(this::closeAllTab);
        items.add(closeTab);
        items.add(closeLeftTab);
        items.add(closeRightTab);
        items.add(closeOtherTab);
        items.add(closeAllTab);
        return items;
    }

    /**
     * 关闭左侧tab
     */
    public void closeLeftTab() {
        FXUtil.runLater(() -> {
            List<Tab> list = new ArrayList<>();
            for (Tab tab : this.tabs()) {
                if (tab == this) {
                    break;
                }
                list.add(tab);
            }
            this.tabs().removeAll(list);
        });
    }

    /**
     * 关闭右侧tab
     */
    public void closeRightTab() {
        FXUtil.runLater(() -> {
            List<Tab> list = new ArrayList<>();
            boolean start = false;
            for (Tab tab : this.tabs()) {
                if (tab == this) {
                    start = true;
                } else if (start) {
                    list.add(tab);
                }
            }
            this.tabs().removeAll(list);
        });
    }

    /**
     * 关闭全部tab
     */
    public void closeAllTab() {
        FXUtil.runLater(() -> this.tabs().clear());
    }

    /**
     * 关闭其他tab
     */
    public void closeOtherTab() {
        FXUtil.runLater(() -> this.tabs().removeIf(tab -> tab != this));
    }

    /**
     * 获取tab列表
     *
     * @return tab列表
     */
    protected ObservableList<Tab> tabs() {
        return this.getTabPane().getTabs();
    }

    @Override
    public void flushTitle() {
        super.flushTitle();
        if (StrUtil.isEmpty(this.getTitle())) {
            String title = this.getTabTitle();
            if (title != null) {
                this.setTitle(title);
            }
        }
    }

    protected String getTabTitle() {
        return null;
    }
}
