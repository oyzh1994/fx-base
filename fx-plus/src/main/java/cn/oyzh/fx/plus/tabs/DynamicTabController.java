package cn.oyzh.fx.plus.tabs;

import cn.oyzh.fx.plus.event.EventListener;
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

    /**
     * tab初始化事件
     */
    public void onTabInit(DynamicTab tab) {
        this.setTab(tab);
        EventListener.super.register();
    }

    /**
     * tab关闭事件
     *
     * @param event 事件
     */
    public void onTabClose(DynamicTab tab, Event event) {
        EventListener.super.unregister();
    }

    @Override
    public void changeLocale(Locale locale) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {

    }
}
