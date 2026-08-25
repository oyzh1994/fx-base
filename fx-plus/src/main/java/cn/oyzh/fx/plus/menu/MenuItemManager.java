package cn.oyzh.fx.plus.menu;

import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-06-25
 */
public class MenuItemManager {

    /**
     * 获取菜单
     *
     * @param text    文字
     * @param graphic 图标
     * @param action  操作
     * @return 菜单
     */
    public static Menu getMenu(String text, Node graphic, Runnable action) {
        return new FXMenu(graphic, text, action);
    }

    /**
     * 获取分割菜单项
     *
     * @return 分割菜单项
     */
    public static SeparatorMenuItem getSeparatorMenuItem() {
        return new SeparatorMenuItem();
    }

    /**
     * 获取菜单项
     *
     * @param text   文字
     * @param action 操作
     * @return 菜单
     */
    public static MenuItem getMenuItem(String text, Runnable action) {
        return getMenuItem(text, null, action);
    }

    /**
     * 获取菜单项
     *
     * @param text    文字
     * @param graphic 图标
     * @param action  操作
     * @return 菜单
     */
    public static MenuItem getMenuItem(String text, Node graphic, Runnable action) {
        MenuItem menuItem = new FXMenuItem();
        menuItem.setText(text);
        if (graphic != null) {
            menuItem.setGraphic(graphic);
        }
        if (action != null) {
            menuItem.setOnAction(event -> action.run());
        }
        return menuItem;
    }

    /**
     * 获取选择菜单项
     *
     * @param text    文字
     * @param action  操作
     * @param checked 是否选中
     * @return 菜单
     */
    public static CheckMenuItem getCheckMenuItem(String text, Runnable action, boolean checked) {
        return getCheckMenuItem(text, null, action, checked);
    }

    /**
     * 获取选择菜单项
     *
     * @param text    文字
     * @param graphic 图标
     * @param action  操作
     * @param checked 是否选中
     * @return 菜单
     */
    public static CheckMenuItem getCheckMenuItem(String text, Node graphic, Runnable action, boolean checked) {
        CheckMenuItem menuItem = new CheckMenuItem(text);
        if (graphic != null) {
            menuItem.setGraphic(graphic);
        }
        if (action != null) {
            menuItem.setOnAction(event -> {
                action.run();
            });
        }
        menuItem.setSelected(checked);
        return menuItem;
    }
}
