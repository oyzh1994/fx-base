package cn.oyzh.fx.plus.menu;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-06-25
 */
public class MenuItemManager {

    // /**
    //  * 菜单池
    //  */
    // private static final MenuPool MENU_POOL = new MenuPool();

    /**
     * 菜单项池
     */
    private static final MenuItemPool MENU_ITEM_POOL = new MenuItemPool();

    /**
     * 分割菜单项池
     */
    private static final SeparatorMenuItemPool SEPARATOR_MENU_ITEM_POOL = new SeparatorMenuItemPool();

    /**
     * 获取菜单
     *
     * @return 菜单
     */
    public static Menu getMenu(String text) {
        return getMenu(text, null);
    }

    /**
     * 获取菜单
     *
     * @return 菜单
     */
    public static Menu getMenu(String text, Node graphic) {
        // Menu menu = MENU_POOL.borrowObject();
        // menu.setText(text);
        // if (graphic != null) {
        //     menu.setGraphic(graphic);
        // }
        // return menu;
        return new FXMenu(graphic, text);
    }

    /**
     * 获取分割菜单项
     *
     * @return 分割菜单项
     */
    public static SeparatorMenuItem getSeparatorMenuItem() {
        return SEPARATOR_MENU_ITEM_POOL.borrowObject();
    }

    // /**
    //  * 获取菜单项
    //  *
    //  * @return 菜单项
    //  */
    // public static MenuItem getMenuItem() {
    //     return MENU_ITEM_POOL.borrowObject();
    // }

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

    // /**
    //  * 获取菜单项
    //  *
    //  * @param text    文字
    //  * @param graphic 图标
    //  * @param action  操作
    //  * @return 菜单
    //  */
    // public static MenuItem getMenuItem(String text, SVGGlyph graphic, Runnable action) {
    //     // 生成标签
    //     SVGLabel label = new SVGLabel(text, graphic);
    //     // 设置边距
    //     label.setPadding(Insets.EMPTY);
    //     // 计算宽度
    //     double w = FontUtil.stringWidth(text);
    //     if (graphic != null) {
    //         w += graphic.getWidth() + 25;
    //     }
    //     // 设置宽度
    //     // label.setMaxWidth(w);
    //     label.setMinWidth(w);
    //     label.setPrefWidth(w);
    //     MenuItem menuItem = MENU_ITEM_POOL.borrowObject();
    //     menuItem.setGraphic(graphic);
    //     menuItem.setText(text);
    //     menuItem.setOnAction(event -> action.run());
    //     return menuItem;
    // }

    /**
     * 获取菜单项
     *
     * @param text    文字
     * @param graphic 图标
     * @param action  操作
     * @return 菜单
     */
    public static MenuItem getMenuItem(String text, Node graphic, Runnable action) {
        MenuItem menuItem = MENU_ITEM_POOL.borrowObject();
        menuItem.setText(text);
        if (graphic != null) {
            menuItem.setGraphic(graphic);
        }
        if (action != null) {
            menuItem.setOnAction(event -> action.run());
        }
        return menuItem;
    }

    // /**
    //  * 获取菜单项
    //  *
    //  * @param text    文字
    //  * @param graphic 图标
    //  * @param action  操作
    //  * @return 菜单
    //  */
    // public static MenuItem getMenuItem(String text, Node graphic, EventHandler<ActionEvent> action) {
    //     MenuItem menuItem = MENU_ITEM_POOL.borrowObject();
    //     menuItem.setText(text);
    //     menuItem.setGraphic(graphic);
    //     menuItem.setOnAction(action);
    //     return menuItem;
    // }

    /**
     * 归还菜单项
     *
     * @param menuItem 菜单项
     */
    public static void returnMenuItem(MenuItem menuItem) {
        if (menuItem instanceof SeparatorMenuItem item) {
            SEPARATOR_MENU_ITEM_POOL.returnObject(item);
        // } else if (menuItem instanceof Menu menu) {
        //     MENU_POOL.returnObject(menu);
        } else if (menuItem instanceof FXMenuItem) {
            MENU_ITEM_POOL.returnObject(menuItem);
        }
    }

    // /**
    //  * 归还菜单项
    //  *
    //  * @param menuItem 菜单项
    //  */
    // public static void returnMenuItem(SeparatorMenuItem menuItem) {
    //     SEPARATOR_MENU_ITEM_POOL.returnObject(menuItem);
    // }

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
