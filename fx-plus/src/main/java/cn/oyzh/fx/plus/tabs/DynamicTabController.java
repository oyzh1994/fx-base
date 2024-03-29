package cn.oyzh.fx.plus.tabs;

import cn.oyzh.fx.plus.event.EventListener;
import javafx.event.Event;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 动态tab controller
 *
 * @author oyzh
 * @since 2023/11/3
 */
public abstract class DynamicTabController implements Initializable, EventListener {

    /**
     * tab初始化事件
     */
    public void onTabInit() {

    }

    /**
     * tab关闭事件
     *
     * @param event 事件
     */
    public void onTabClose(Event event) {
        EventListener.super.unregister();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        EventListener.super.register();
    }
}
