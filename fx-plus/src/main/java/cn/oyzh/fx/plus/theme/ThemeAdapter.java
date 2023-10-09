package cn.oyzh.fx.plus.theme;

import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * 主题件适配器
 *
 * @author oyzh
 * @since 2023/05/11
 */
public interface ThemeAdapter {

    default void onThemeChanged(Theme theme) {
        if (theme != null) {
            switch (theme) {
                case DARK -> this.onDarkTheme();
                case LIGHT -> this.onLightTheme();
            }
        }
    }

    default void onDarkTheme() {
        if (this instanceof Node node) {
            node.getStyleClass().remove("light");
            node.getStyleClass().add("dark");
        } else if (this instanceof Tab tab) {
            tab.getStyleClass().remove("light");
            tab.getStyleClass().add("dark");
        }
    }

    default void onLightTheme() {
        if (this instanceof Node node) {
            node.getStyleClass().remove("dark");
            node.getStyleClass().add("light");
        } else if (this instanceof Tab tab) {
            tab.getStyleClass().remove("light");
            tab.getStyleClass().add("dark");
        }
    }

    //    default void onDarkTheme() {
//        if (this instanceof Region region) {
//            region.setBackground(ControlUtil.backgroundOfColor(Color.valueOf("#5B5B5B")));
//        }
//    }
//
//    default void onLightTheme() {
//        if (this instanceof Region region) {
//            region.setBackground(ControlUtil.backgroundOfColor(Color.WHITE));
//        }
//    }

}
