package cn.oyzh.fx.plus.util;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.FXConst;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * @author oyzh
 * @since 2024-09-19
 */
@UtilityClass
public class TooltipUtil {

    /**
     * 初始化提示组件
     *
     * @param text 提示
     * @return 组件
     */
    public static Tooltip initTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        initStyle(tooltip);
        return tooltip;
    }

    /**
     * 初始化提示组件
     *
     * @param tooltip 提示组件
     */
    public static void initStyle(Tooltip tooltip) {
        String style = tooltip.getStyle() +
                ";-fx-background-color: #000000;" +
                "-fx-background-radius: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-font-size: 10;";
        tooltip.setStyle(style);
        tooltip.setShowDelay(Duration.millis(500));
        tooltip.setOpacity(0.75);
    }

    /**
     * 获取提示组件
     *
     * @param target 组件
     * @return 提示组件
     */
    public static Tooltip getTooltip(EventTarget target) {
        Tooltip tooltip = null;
        if (target instanceof Node node) {
            tooltip = (Tooltip) node.getProperties().get(FXConst.TOOLTIP_PROP_KEY);
        } else if (target instanceof Tab tab) {
            tooltip = (Tooltip) tab.getProperties().get(FXConst.TOOLTIP_PROP_KEY);
        }
        return tooltip;
    }

    /**
     * 设置提示标题
     *
     * @param target 组件
     * @param text   提示文本
     */
    public static void setTipText(@NonNull EventTarget target, String text) {
        if (StrUtil.isNotBlank(text)) {
            if (target instanceof Node node) {
                node.setOnMouseEntered(event -> Tooltip.install(node, initTooltip(text)));
                node.setOnMouseExited(event -> {
                    Tooltip tooltip = getTooltip(node);
                    if (tooltip != null) {
                        Tooltip.uninstall(node, tooltip);
                    }
                });
            } else if (target instanceof Tab tab) {
                tab.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        tab.setTooltip(initTooltip(text));
                    } else {
                        tab.setTooltip(null);
                    }
                });
            }
        } else {
            if (target instanceof Node node) {
                Tooltip tooltip = getTooltip(node);
                if (tooltip != null) {
                    Tooltip.uninstall(node, tooltip);
                }
            } else if (target instanceof Tab tab) {
                tab.setTooltip(null);
            }
        }
    }

    /**
     * 获取提示文本
     *
     * @param target 组件
     * @return 提示文本
     */
    public static String getTipText(EventTarget target) {
        Tooltip tooltip = getTooltip(target);
        if (tooltip != null) {
            return tooltip.getText();
        }
        return null;
    }

}
