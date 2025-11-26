package cn.oyzh.fx.plus.util;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Window;

import java.util.function.Consumer;

/**
 * TabPane工具类
 *
 * @author oyzh
 * @since 2025-11-18
 */
public class TabPaneUtil {

    /**
     * 获取窗口
     *
     * @param tab 标签
     * @return 结果
     */
    public static Window getWindow(Tab tab) {
        if (tab == null) {
            return null;
        }
        TabPane tabPane = tab.getTabPane();
        if (tabPane == null) {
            return null;
        }
        Scene scene = tabPane.getScene();
        if (scene == null) {
            return null;
        }
        return scene.getWindow();
    }

    /**
     * 窗口就绪事件
     *
     * @param tab      tab
     * @param callback 回调
     */
    public static void onWindowReady(Tab tab, Consumer<Window> callback) {
        // 监听窗口就绪
        if (getWindow(tab) != null) {
            callback.accept(getWindow(tab));
        } else {
            tab.tabPaneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (getWindow(tab) != null) {
                        callback.accept(getWindow(tab));
                    } else {
                        newValue.sceneProperty().addListener((observable1, oldValue1, newValue1) -> {
                            if (getWindow(tab) != null) {
                                callback.accept(getWindow(tab));
                            } else {
                                newValue1.windowProperty().addListener((observable2, oldValue2, newValue2) -> {
                                    if (newValue2 != null) {
                                        callback.accept(newValue2);
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
    }
}
