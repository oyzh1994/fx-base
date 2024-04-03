package cn.oyzh.fx.plus.theme;

import cn.oyzh.fx.plus.FXStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * 主题风格
 *
 * @author oyzh
 * @since 2024/4/3
 */
public interface ThemeStyle {

    /**
     * 获取名称
     *
     * @return 主题名称
     */
    String getName();

    /**
     * 获取描述
     *
     * @return 主题描述
     */
    String getDesc();

    /**
     * 是否暗黑模式
     *
     * @return 结果
     */
    default boolean isDarkMode() {
        return false;
    }

    /**
     * 获取强调色
     *
     * @return 强调色
     */
    Color getAccentColor();

    /**
     * 获取强调色16进制值
     * @return 强调色16进制值
     */
    default String getAccentColorHex() {
        return ThemeUtil.getColorHex(this.getAccentColor());
    }

    /**
     * 获取背景色
     *
     * @return 背景色
     */
    Color getForegroundColor();

    /**
     * 获取背景色16进制值
     * @return 背景色16进制值
     */
    default String getForegroundColorHex() {
        return ThemeUtil.getColorHex(this.getForegroundColor());
    }

    /**
     * 获取前景色
     *
     * @return 前景色
     */
    Color getBackgroundColor();


    /**
     * 获取背景色16进制值
     * @return 背景色16进制值
     */
    default String getBackgroundColorHex() {
        return ThemeUtil.getColorHex(this.getBackgroundColor());
    }

    /**
     * 获取样式文件
     *
     * @return 样式文件
     */
    String getUserAgentStylesheet();

    /**
     * 处理样式
     *
     * @param node 节点
     */
    default void handleStyle(Node node) {
        FXUtil.runWait(() -> {
            if (node instanceof Parent parent) {
                try {
                    List<String> removes = new ArrayList<>();
                    removes.add(FXStyle.FX_BASE);
                    removes.add(ThemeManager.currentTheme().getUserAgentStylesheet());
                    parent.getStylesheets().removeAll(removes);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    parent.getStylesheets().addAll(this.getUserAgentStylesheet(), FXStyle.FX_BASE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

}
