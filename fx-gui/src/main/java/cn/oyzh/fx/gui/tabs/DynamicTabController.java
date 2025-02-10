package cn.oyzh.fx.gui.tabs;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.event.EventListener;
import cn.oyzh.event.EventUtil;
import cn.oyzh.fx.plus.i18n.I18nAdapter;
import javafx.event.Event;
import javafx.fxml.Initializable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 动态tab controller
 *
 * @author oyzh
 * @since 2023/11/3
 */
public abstract class DynamicTabController implements EventListener, I18nAdapter, Initializable {

    private Reference<DynamicTab> tabReference;

    protected void setTab(DynamicTab tab) {
        this.tabReference = new WeakReference<>(tab);
    }

    public DynamicTab getTab() {
        return this.tabReference != null ? this.tabReference.get() : null;
    }

    public void closeTab() {
        DynamicTab tab = this.getTab();
        if (tab != null) {
            tab.closeTab();
        } else {
            JulLog.warn("tab is null");
        }
    }

    public void disableTab() {
        DynamicTab tab = this.getTab();
        if (tab != null) {
            tab.disable();
        }
    }

    public void enableTab() {
        DynamicTab tab = this.getTab();
        if (tab != null) {
            tab.enable();
        }
    }

    public void flushTab() {
        DynamicTab tab = this.getTab();
        if (tab != null) {
            tab.flush();
        }
    }

    public void flushTabGraphic() {
        DynamicTab tab = this.getTab();
        if (tab != null) {
            tab.flushGraphic();
        }
    }

    public void flushTabGraphicColor() {
        DynamicTab tab = this.getTab();
        if (tab != null) {
            tab.flushGraphicColor();
        }
    }

    /**
     * 绑定监听器
     */
    protected void bindListeners() {

    }

    /**
     * tab初始化事件
     */
    public void onTabInit(DynamicTab tab) {
        this.setTab(tab);
        this.bindListeners();
        EventListener.super.register();
    }

    /**
     * tab关闭事件
     *
     * @param event 事件
     */
    public void onTabClose(DynamicTab tab, Event event) {
        EventListener.super.unregister();
        if (!tab.hasProp("tab:close:flag")) {
            tab.setProp("tab:close:flag", true);
            EventUtil.post(new TabClosedEvent(tab));
        }
    }

    /**
     * tab请求关闭事件
     *
     * @param event 事件
     */
    public void onCloseRequest(DynamicTab tab, Event event) {
    }

    @Override
    public void changeLocale(Locale locale) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {

    }
}
