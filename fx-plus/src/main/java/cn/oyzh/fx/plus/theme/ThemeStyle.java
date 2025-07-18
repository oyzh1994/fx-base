package cn.oyzh.fx.plus.theme;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.ReflectUtil;
import cn.oyzh.fx.plus.FXStyle;
import cn.oyzh.fx.plus.util.FXColorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

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
        return FXColorUtil.getColorHex(this.getAccentColor());
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
        return FXColorUtil.getColorHex(this.getForegroundColor());
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
        return FXColorUtil.getColorHex(this.getBackgroundColor());
    }

    /**
     * 获取样式文件
     *
     * @return 样式文件
     */
    String getUserAgentStylesheet();

    /**
     * 获取压缩样式文件
     *
     * @return 压缩样式文件
     */
    String getCompressedUserAgentStylesheet();

    /**
     * 重应用css尾缀
     */
    String REAPPLY_CSS_SUFFIX = ":reapply:css";

    /**
     * 处理样式
     *
     * @param node 节点
     */
    default void handleStyle(Parent node) {
        if (node != null) {
            TaskManager.startDelay(this.hashCode() + REAPPLY_CSS_SUFFIX, () -> FXUtil.runLater(() -> {
                try {
                    // 更新fx-base样式文件
                    node.getStylesheets().remove(FXStyle.FX_BASE);
                    node.getStylesheets().add(FXStyle.FX_BASE);
                    // 重新应用样式
//                    node.reapplyCss();
                    ReflectUtil.invoke(node, "reapplyCss");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }), 50);
        }
        // FXUtil.runLater(() -> {
        //     // if (node instanceof Parent parent) {
        //         try {
        //             node.getStylesheets().removeAll(FXStyle.FX_BASE, ThemeManager.currentUserAgentStylesheet());
        //             // node.getStylesheets().addAll(this.getUserAgentStylesheet(), FXStyle.FX_BASE);
        //             node.getStylesheets().addAll(ThemeManager.currentUserAgentStylesheet(), FXStyle.FX_BASE);
        //             // parent.getStylesheets().removeAll(FXStyle.FX_BASE, ThemeManager.currentCompressedUserAgentStylesheet());
        //             // parent.getStylesheets().addAll(this.getCompressedUserAgentStylesheet(), FXStyle.FX_BASE);
        //             // List<String> removes = new ArrayList<>();
        //             // removes.add(FXStyle.FX_BASE);
        //             // removes.add(ThemeManager.currentTheme().getCompressedUserAgentStylesheet());
        //             // parent.getStylesheets().removeAll(removes);
        //             // parent.getStylesheets().addAll(this.getCompressedUserAgentStylesheet(), FXStyle.FX_BASE);
        //         } catch (Exception ex) {
        //             ex.printStackTrace();
        //         }
        //     // }
        // });
    }

    /**
     * 处理样式
     *
     * @param node 节点
     */
    default void handleStyle(Node node) {
        if (node != null) {
            TaskManager.startDelay(this.hashCode() + REAPPLY_CSS_SUFFIX, () -> FXUtil.runLater(() -> {
                try {
                    ReflectUtil.invoke(node, "reapplyCss");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }), 50);
        }
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
