package cn.oyzh.fx.plus.theme;

import cn.oyzh.fx.plus.FXStyle;
import cn.oyzh.fx.plus.util.ColorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
     * @param locale 地区
     * @return 主题描述
     */
    String getDesc(Locale locale);

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
     *
     * @return 强调色16进制值
     */
    default String getAccentColorHex() {
        return ColorUtil.getColorHex(this.getAccentColor());
    }

    /**
     * 获取背景色
     *
     * @return 背景色
     */
    Color getForegroundColor();

    /**
     * 获取背景色16进制值
     *
     * @return 背景色16进制值
     */
    default String getForegroundColorHex() {
        return ColorUtil.getColorHex(this.getForegroundColor());
    }

    /**
     * 获取前景色
     *
     * @return 前景色
     */
    Color getBackgroundColor();

    /**
     * 获取背景色16进制值
     *
     * @return 背景色16进制值
     */
    default String getBackgroundColorHex() {
        return ColorUtil.getColorHex(this.getBackgroundColor());
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

    /**
     * 计算相关度
     *
     * @param style 主题
     * @return 相关度
     */
    default double corr(ThemeStyle style) {
        if ((this.isDarkMode() && !style.isDarkMode()) || (!this.isDarkMode() && style.isDarkMode())) {
            return -1;
        }
        // 前景色
        double d1 = ThemeUtil.calcCorr(this.getForegroundColor(), style.getForegroundColor());
        // 背景色
        double d2 = ThemeUtil.calcCorr(this.getBackgroundColor(), style.getBackgroundColor());
        // 强调色
        double d3 = ThemeUtil.calcCorr(this.getAccentColor(), style.getAccentColor());
        // 返回相关度
        return d1 * 5.5 + d2 * 2.5 + d3 * 2;
    }

}
