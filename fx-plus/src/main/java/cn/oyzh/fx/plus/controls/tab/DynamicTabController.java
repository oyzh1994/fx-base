package cn.oyzh.fx.plus.controls.tab;

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
public abstract class DynamicTabController implements Initializable {

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

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
