package cn.oyzh.fx.plus.theme;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.stage.StageWrapper;
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
     * 设置启用主题
     *
     * @param enableTheme 启用主题
     */
    default void setEnableTheme(boolean enableTheme) {
        this.setProp("_enableTheme", enableTheme);
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
     * @param style 主题风格
     */
    default void changeTheme(ThemeStyle style) {
        if (this.isEnableTheme() && style != null) {
            switch (this) {
                case Parent node -> this.handleStyle(node, style);
                case StageWrapper wrapper -> this.handleStyle(wrapper.root(), style);
                case Stage stage -> this.handleStyle(stage.getScene().getRoot(), style);
                case Popup popup -> this.handleStyle(popup.getContent(), style);
                default -> {
                }
            }
        }
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
