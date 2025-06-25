package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.font.FontUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-06-25
 */
public class MenuItemManager {

    /**
     * 菜单项池
     */
    private static final MenuItemPool POOL = new MenuItemPool();

    /**
     * 分割菜单项池
     */
    private static final SeparatorMenuItemPool SEPARATOR_POOL = new SeparatorMenuItemPool();

    /**
     * 获取分割菜单项
     *
     * @return 菜单
     */
    public static SeparatorMenuItem getSeparatorMenuItem() {
        return SEPARATOR_POOL.borrowObject();
    }

    /**
     * 获取菜单项
     *
     * @return 菜单
     */
    public static MenuItem getMenuItem() {
        return POOL.borrowObject();
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
    public static MenuItem getMenuItem(String text, SVGGlyph graphic, Runnable action) {
        // 生成标签
        SVGLabel label = new SVGLabel(text, graphic);
        // 设置边距
        label.setPadding(Insets.EMPTY);
        // 计算宽度
        double w = FontUtil.stringWidth(text);
        if (graphic != null) {
            w += graphic.getWidth() + 25;
        }
        // 设置宽度
        label.setMaxWidth(w);
        label.setMinWidth(w);
        label.setPrefWidth(w);
        MenuItem menuItem = POOL.borrowObject();
        menuItem.setGraphic(label);
        menuItem.setOnAction(event -> action.run());
        return menuItem;
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
        return getMenuItem(text, graphic, event -> action.run());
    }

    /**
     * 获取菜单项
     *
     * @param text    文字
     * @param graphic 图标
     * @param action  操作
     * @return 菜单
     */
    public static MenuItem getMenuItem(String text, Node graphic, EventHandler<ActionEvent> action) {
        MenuItem menuItem = POOL.borrowObject();
        menuItem.setText(text);
        menuItem.setGraphic(graphic);
        menuItem.setOnAction(action);
        return menuItem;
    }

    /**
     * 归还菜单项
     *
     * @param menuItem 菜单项
     */
    public static void returnMenuItem(MenuItem menuItem) {
        if (menuItem instanceof FXMenuItem) {
            POOL.returnObject(menuItem);
        }
    }

    /**
     * 归还菜单项
     *
     * @param menuItem 菜单项
     */
    public static void returnMenuItem(SeparatorMenuItem menuItem) {
        SEPARATOR_POOL.returnObject(menuItem);
    }


    /**
     * 归还菜单项
     *
     * @param menuItems 菜单项
     */
    public static void returnMenuItem(List<? extends MenuItem> menuItems) {
        for (MenuItem menuItem : menuItems) {
            returnMenuItem(menuItem);
        }
    }
}
