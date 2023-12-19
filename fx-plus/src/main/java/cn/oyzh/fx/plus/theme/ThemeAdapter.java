package cn.oyzh.fx.plus.theme;

import cn.oyzh.fx.plus.FXStyle;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.extra.AtlantaFX;
import cn.oyzh.fx.plus.stage.StageWrapper;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * 主题适配器
 *
 * @author oyzh
 * @since 2023/05/11
 */
public interface ThemeAdapter extends PropAdapter {

    /**
     * 禁用主题
     */
    default void disableTheme() {
        this.setProp("_enableTheme", false);
    }

    /**
     * 启用主题
     */
    default void enableTheme() {
        this.removeProp("_enableTheme");
    }

    /**
     * 是否启用主题
     *
     * @return 结果
     */
    default boolean isEnableTheme() {
        Boolean b = this.getProp("_enableTheme");
        return b == null || b;
    }

    /**
     * 更改主题
     *
     * @param theme 主题
     */
    default void changeTheme(Theme theme) {
        if (this.isEnableTheme()) {
            if (theme != null) {
                switch (theme) {
                    case DRACULA -> this.switchDraculaTheme();
                    case NORD_DARK -> this.switchNordDarkTheme();
                    case NORD_LIGHT -> this.switchNordLightTheme();
                    case PRIMER_DARK -> this.switchPrimerDarkTheme();
                    case PRIMER_LIGHT -> this.switchPrimerLightTheme();
                    case CUPERTINO_DARK -> this.switchCupertinoDarkTheme();
                    case CUPERTINO_LIGHT -> this.switchCupertinoLightTheme();
                }
                // 页面
                if (this instanceof StageWrapper wrapper) {
                    this.changeTheme(wrapper.root(), theme);
                }
            }
        }
    }

    /**
     * 更改主题
     *
     * @param root  根节点
     * @param theme 主题
     */
    default void changeTheme(Node root, Theme theme) {
        if (root instanceof ThemeAdapter adapter) {
            adapter.changeTheme(theme);
        }
        if (root instanceof Parent parent) {
            for (Node node : parent.getChildrenUnmodifiable()) {
                this.changeTheme(node, theme);
            }
        }
    }

    default void switchDraculaTheme() {
        if (this instanceof StageWrapper wrapper) {
            ObservableList<String> stylesheets = wrapper.root().getStylesheets();
            this.handleStyle(stylesheets, Theme.DRACULA);
        } else if (this instanceof Parent parent) {
            ObservableList<String> stylesheets = parent.getStylesheets();
            this.handleStyle(stylesheets, Theme.DRACULA);
        }
    }

    default void switchNordDarkTheme() {
        if (this instanceof StageWrapper wrapper) {
            ObservableList<String> stylesheets = wrapper.root().getStylesheets();
            this.handleStyle(stylesheets, Theme.NORD_DARK);
        } else if (this instanceof Parent parent) {
            ObservableList<String> stylesheets = parent.getStylesheets();
            this.handleStyle(stylesheets, Theme.NORD_DARK);
        }
    }

    default void switchNordLightTheme() {
        if (this instanceof StageWrapper wrapper) {
            ObservableList<String> stylesheets = wrapper.root().getStylesheets();
            this.handleStyle(stylesheets, Theme.NORD_LIGHT);
        } else if (this instanceof Parent parent) {
            ObservableList<String> stylesheets = parent.getStylesheets();
            this.handleStyle(stylesheets, Theme.NORD_LIGHT);
        }
    }

    default void switchPrimerDarkTheme() {
        if (this instanceof StageWrapper wrapper) {
            ObservableList<String> stylesheets = wrapper.root().getStylesheets();
            this.handleStyle(stylesheets, Theme.PRIMER_DARK);
        } else if (this instanceof Parent parent) {
            ObservableList<String> stylesheets = parent.getStylesheets();
            this.handleStyle(stylesheets, Theme.PRIMER_DARK);
        }
    }

    default void switchPrimerLightTheme() {
        if (this instanceof StageWrapper wrapper) {
            ObservableList<String> stylesheets = wrapper.root().getStylesheets();
            this.handleStyle(stylesheets, Theme.PRIMER_LIGHT);
        } else if (this instanceof Parent parent) {
            ObservableList<String> stylesheets = parent.getStylesheets();
            this.handleStyle(stylesheets, Theme.PRIMER_LIGHT);
        }
    }

    default void switchCupertinoDarkTheme() {
        if (this instanceof StageWrapper wrapper) {
            ObservableList<String> stylesheets = wrapper.root().getStylesheets();
            this.handleStyle(stylesheets, Theme.CUPERTINO_DARK);
        } else if (this instanceof Parent parent) {
            ObservableList<String> stylesheets = parent.getStylesheets();
            this.handleStyle(stylesheets, Theme.CUPERTINO_DARK);
        }
    }

    default void switchCupertinoLightTheme() {
        if (this instanceof StageWrapper wrapper) {
            ObservableList<String> stylesheets = wrapper.root().getStylesheets();
            this.handleStyle(stylesheets, Theme.CUPERTINO_LIGHT);
        } else if (this instanceof Parent parent) {
            ObservableList<String> stylesheets = parent.getStylesheets();
            this.handleStyle(stylesheets, Theme.CUPERTINO_LIGHT);
        }
    }

    /**
     * 处理样式
     *
     * @param stylesheets 样式列表
     * @param theme       主题
     */
    private void handleStyle(ObservableList<String> stylesheets, Theme theme) {
        if (stylesheets != null) {
            String style = switch (theme) {
                case PRIMER_LIGHT -> AtlantaFX.PRIMER_LIGHT;
                case PRIMER_DARK -> AtlantaFX.PRIMER_DARK;
                case NORD_LIGHT -> AtlantaFX.NORD_LIGHT;
                case NORD_DARK -> AtlantaFX.NORD_DARK;
                case CUPERTINO_LIGHT -> AtlantaFX.CUPERTINO_LIGHT;
                case CUPERTINO_DARK -> AtlantaFX.CUPERTINO_DARK;
                case DRACULA -> AtlantaFX.DRACULA;
            };
            if (!stylesheets.contains(style)) {
                if (!stylesheets.isEmpty()) {
                    stylesheets.remove(AtlantaFX.DRACULA);
                    stylesheets.remove(AtlantaFX.NORD_DARK);
                    stylesheets.remove(AtlantaFX.NORD_LIGHT);
                    stylesheets.remove(AtlantaFX.PRIMER_DARK);
                    stylesheets.remove(AtlantaFX.PRIMER_LIGHT);
                    stylesheets.remove(AtlantaFX.CUPERTINO_DARK);
                    stylesheets.remove(AtlantaFX.CUPERTINO_LIGHT);
                    stylesheets.remove(FXStyle.FX_BASE);
                    // stylesheets.remove(FXStyle.FX_DARK);
                    // stylesheets.remove(FXStyle.FX_LIGHT);
                }
                stylesheets.addAll(style, FXStyle.FX_BASE);
                // if (theme.isDarkMode()) {
                //     stylesheets.addAll(style, FXStyle.FX_BASE, FXStyle.FX_DARK);
                // } else {
                //     stylesheets.addAll(style, FXStyle.FX_BASE, FXStyle.FX_LIGHT);
                // }
            }
        }
    }
}
