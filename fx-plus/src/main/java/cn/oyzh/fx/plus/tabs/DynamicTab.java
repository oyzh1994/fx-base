package cn.oyzh.fx.plus.tabs;

import cn.oyzh.fx.plus.controls.FXTab;
import cn.oyzh.fx.plus.ext.FXMLLoaderExt;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 动态tab
 *
 * @author oyzh
 * @since 2023/11/03
 */
public abstract class DynamicTab extends FXTab {

    {
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
     * 刷新tab
     */
    public void flush() {
        FXUtil.runWait(() -> {
            this.flushGraphic();
            this.flushGraphicColor();
            this.flushTitle();
        });
    }
}
