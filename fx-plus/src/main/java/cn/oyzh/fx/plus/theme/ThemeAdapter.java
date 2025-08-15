package cn.oyzh.fx.plus.theme;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Tab;
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

    String ENABLE_THEME_KEY = "enable:theme";

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
        if (this.isEnableTheme() && style != null) {
            if (this instanceof Canvas node) {
                this.handleStyle(node, style);
            } else if (NodeUtil.isSwingImport && this instanceof javafx.embed.swing.SwingNode node) {
                this.handleStyle(node, style);
            } else if (this instanceof Parent node) {
                this.handleStyle(node, style);
            } else if (this instanceof Popup node) {
                this.handleStyle(node.getContent(), style);
            } else if (this instanceof Tab node) {
                this.handleStyle(node.getContent(), style);
            } else if (this instanceof StageAdapter node) {
                this.handleStyle(node.root(), style);
            } else if (this instanceof Stage node) {
                this.handleStyle(node.getScene().getRoot(), style);
            }
        }
    }

    /**
     * 处理样式
     *
     * @param nodes 节点列表
     * @param style 主题风格
     */
    private void handleStyle(List<?> nodes, ThemeStyle style) {
        for (Object node : nodes) {
            if (node instanceof Parent parent) {
                style.handleStyle(parent);
            }
        }
    }

    /**
     * 处理样式
     *
     * @param node  节点
     * @param style 主题风格
     */
    private void handleStyle(Parent node, ThemeStyle style) {
        style.handleStyle(node);
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

    /**
     * 处理样式
     *
     * @param node  节点
     * @param style 主题风格
     */
    private void handleStyle(javafx.embed.swing.SwingNode node, ThemeStyle style) {
        style.handleStyle(node);
        cn.oyzh.fx.plus.swing.SwingUtil.applyTheme(node.getContent());
    }
}
