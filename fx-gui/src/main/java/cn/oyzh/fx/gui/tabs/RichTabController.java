package cn.oyzh.fx.gui.tabs;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.event.EventListener;
import cn.oyzh.event.EventUtil;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.i18n.I18nAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Window;

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
public abstract class RichTabController implements EventListener, I18nAdapter, Initializable {

    private Reference<FXTab> tabReference;

    /**
     * 设置tab
     *
     * @param tab tab
     */
    protected void setTab(FXTab tab) {
        this.tabReference = new WeakReference<>(tab);
        // 初始化一次样式
        tab.changeTheme(ThemeManager.currentTheme());
    }

    /**
     * 获取tab
     *
     * @return RichTab
     */
    public FXTab getTab() {
        return this.tabReference != null ? this.tabReference.get() : null;
    }

    /**
     * 获取TabPane
     *
     * @return TabPane
     */
    public TabPane getTabPane() {
        FXTab tab = this.getTab();
        if (tab == null) {
            return null;
        }
        return tab.getTabPane();
    }

    /**
     * 获取窗口
     *
     * @return Window
     */
    public Window getWindow() {
        TabPane tabPane = this.getTabPane();
        if (tabPane == null) {
            return null;
        }
        Scene scene = tabPane.getScene();
        return scene == null ? null : scene.getWindow();
    }

    public void closeTab() {
        FXTab tab = this.getTab();
        if (tab != null) {
            tab.closeTab();
        } else {
            JulLog.warn("tab is null");
        }
    }

    public void disableTab() {
        FXTab tab = this.getTab();
        if (tab != null) {
            tab.disable();
        }
    }

    public void enableTab() {
        FXTab tab = this.getTab();
        if (tab != null) {
            tab.enable();
        }
    }

    public void flushTab() {
        FXTab tab = this.getTab();
        if (tab != null) {
            tab.flush();
        }
    }

    public void flushTabGraphic() {
        FXTab tab = this.getTab();
        if (tab != null) {
            tab.flushGraphic();
        }
    }

    public void flushTabGraphicColor() {
        FXTab tab = this.getTab();
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
     *
     * @param tab tab
     */
    public void onTabInit(FXTab tab) {
        this.setTab(tab);
        this.bindListeners();
        EventListener.super.register();
    }

    /**
     * tab关闭事件
     *
     * @param event 事件
     */
    public void onTabClosed(Event event) {
        EventListener.super.unregister();
        FXTab tab = this.getTab();
        if (tab != null && !tab.hasProp("tab:close:flag")) {
            tab.setProp("tab:close:flag", true);
            EventUtil.post(new TabClosedEvent(tab));
        }
    }

    /**
     * tab请求关闭事件
     *
     * @param event 事件
     */
    public void onTabCloseRequest(Event event) {
    }

    @Override
    public void changeLocale(Locale locale) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {

    }
}
