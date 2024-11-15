package cn.oyzh.fx.plus.theme;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.List;

/**
 * 主题适配器
 *
 * @author oyzh
 * @since 2023/05/11
 */
public interface ThemeAdapter extends PropAdapter {

    String ENABLE_THEME_KEY = "_enable_theme";

    /**
     * 禁用主题
     */
    default void disableTheme() {
        this.setProp(ENABLE_THEME_KEY, false);
    }

    /**
     * 启用主题
     */
    default void enableTheme() {
        this.removeProp(ENABLE_THEME_KEY);
    }

    /**
     * 设置启用主题
     *
     * @param enableTheme 启用主题
     */
    default void setEnableTheme(boolean enableTheme) {
        this.setProp(ENABLE_THEME_KEY, enableTheme);
    }

    /**
     * 是否启用主题
     *
     * @return 结果
     */
    default boolean isEnableTheme() {
        Boolean b = this.getProp(ENABLE_THEME_KEY);
        return b == null || b;
    }

    /**
     * 更改主题
     *
     * @param style 主题风格
     */
    default void changeTheme(ThemeStyle style) {
        // if (this.isEnableTheme() && style != null) {
        //     switch (this) {
        //         case Parent node -> this.handleStyle(node, style);
        //         case StageAdapter wrapper -> this.handleStyle(wrapper.root(), style);
        //         case Stage stage -> this.handleStyle(stage.getScene().getRoot(), style);
        //         case Popup popup -> this.handleStyle(popup.getContent(), style);
        //         default -> {
        //         }
        //     }
        // }
    }

    /**
     * 处理样式
     *
     * @param nodes 节点列表
     * @param style 主题风格
     */
    private void handleStyle(List<Node> nodes, ThemeStyle style) {
        for (Node node : nodes) {
            style.handleStyle(node);
        }
    }

    /**
     * 处理样式
     *
     * @param node  节点
     * @param style 主题风格
     */
    private void handleStyle(Node node, ThemeStyle style) {
        style.handleStyle(node);
    }
}
