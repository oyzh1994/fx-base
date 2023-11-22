package cn.oyzh.fx.plus.tabs;

import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 动态tab
 *
 * @author oyzh
 * @since 2023/11/03
 */
public abstract class DynamicTab extends FXTab {

    public DynamicTab(){
        // 加载内容
        this.loadContent();
        this.setClosable(true);
    }

    /**
     * controller
     */
    @Getter
    @Accessors(fluent = true, chain = false)
    protected Object controller;

    /**
     * 加载内容
     */
    protected void loadContent() {
        String url = this.url();
        if (url != null) {
            FXMLLoaderExt loaderExt = new FXMLLoaderExt();
            Node content = loaderExt.load(url);
            content.setCache(true);
            content.setCacheHint(CacheHint.QUALITY);
            this.controller = loaderExt.getController();
            this.setContent(content);
            if (this.controller instanceof DynamicTabController tabContent) {
                tabContent.onTabInit();
                this.setOnClosed(tabContent::onTabClose);
            }
        }
    }

    /**
     * 地址
     *
     * @return tab的地址
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
     * 设置标题
     *
     * @param title 标题
     */
    protected void title(String title) {
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
                glyph.setColor((Color) paint);
            } else if (node instanceof SVGLabel label) {
                label.setColor((Color) paint);
                label.setTextFill(paint);
            }
        });
    }
}
