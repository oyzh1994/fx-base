package cn.oyzh.fx.plus.tabs;

import cn.oyzh.fx.plus.event.EventListener;
import cn.oyzh.fx.plus.i18n.I18nAdapter;
import javafx.event.Event;
import javafx.fxml.Initializable;

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

    /**
     * tab初始化事件
     */
    public void onTabInit(DynamicTab tab) {
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
    public String i18nId() {
        return I18nAdapter.super.i18nId();
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {

    }
}
