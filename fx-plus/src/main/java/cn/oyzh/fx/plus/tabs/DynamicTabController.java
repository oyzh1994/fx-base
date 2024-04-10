package cn.oyzh.fx.plus.tabs;

import cn.oyzh.fx.plus.event.EventListener;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;
import cn.oyzh.fx.plus.i18n.I18nAdapter;
import javafx.event.Event;
import javafx.fxml.Initializable;
import lombok.Getter;

import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 动态tab controller
 *
 * @author oyzh
 * @since 2023/11/3
 */
public abstract class DynamicTabController implements Initializable, EventListener, I18nAdapter {

    /**
     * 资源
     */
    @Getter
    protected ResourceBundle resources;

    /**
     * tab初始化事件
     */
    public void onTabInit(DynamicTab tab) {

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
    public void initialize(URL url, ResourceBundle resources) {
        EventListener.super.register();
        this.resources = resources;
    }

    @Override
    public String i18nString(String key) {
        try {
            if (this.i18nId() == null) {
                return this.resources.getString(key);
            }
            return this.resources.getString(this.i18nId() + "." + key);
        } catch (MissingResourceException ignored) {
        }
        return BaseResourceBundle.getBaseString("base." + key);
    }

    @Override
    public void changeLocale(Locale locale) {

    }

    @Override
    public String i18nId() {
        return I18nAdapter.super.i18nId();
    }
}
