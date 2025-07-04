package cn.oyzh.fx.plus.tooltip;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.FXConst;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * @author oyzh
 * @since 2024-09-19
 */
public class TooltipUtil {

    // /**
    //  * 缓存
    //  */
    // private final static TimedCache<String, Tooltip> CACHE = CacheUtil.newTimedCache(60 * 1000L);

    /**
     * 池
     */
    private final static TooltipPool POOL = new TooltipPool();

    /**
     * 属性键
     */
    private final static String PROP_KEY = "_tip_handler";

    // /**
    //  * 初始化提示组件
    //  *
    //  * @param text 提示
    //  * @return 组件
    //  */
    // public static Tooltip initTooltip(String text) {
    //     String hash = MD5Util.md5Hex(text);
    //     if (CACHE.containsKey(hash)) {
    //         return CACHE.get(hash);
    //     }
    //     Tooltip tooltip = new Tooltip(text);
    //     initStyle(tooltip);
    //     CACHE.put(hash, tooltip);
    //     return tooltip;
    // }

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
    public static void setTipText(EventTarget target, String text) {
        uninstall(target);
        if (StringUtil.isNotBlank(text)) {
            if (target instanceof Node node) {
                if (node.getProperties().containsKey(PROP_KEY)) {
                    EventHandler<MouseEvent> tipHandler = (EventHandler<MouseEvent>) node.getProperties().get(PROP_KEY);
                    node.removeEventFilter(MouseEvent.ANY, tipHandler);
                }
                EventHandler<MouseEvent> handler = event -> {
                    if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                        Tooltip tooltip = getTooltip(node);
                        if (tooltip == null) {
                            tooltip = POOL.borrowObject();
                            tooltip.setText(text);
                            Tooltip.install(node, tooltip);
                        }
                    } else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
                        Tooltip tooltip = getTooltip(node);
                        if (tooltip != null) {
                            Tooltip.uninstall(node, tooltip);
                            POOL.returnObject(tooltip);
                        }
                    }
                };
                node.addEventFilter(MouseEvent.ANY, handler);
                node.getProperties().put(PROP_KEY, handler);
            } else if (target instanceof Tab tab) {
                if (tab.getProperties().containsKey(PROP_KEY)) {
                    ChangeListener<Boolean> listener = (ChangeListener<Boolean>) tab.getProperties().get(PROP_KEY);
                    tab.selectedProperty().removeListener(listener);
                }
                ChangeListener<Boolean> listener = (observable, oldValue, newValue) -> {
                    if (newValue) {
                        Tooltip tooltip = POOL.borrowObject();
                        tooltip.setText(text);
                        tab.setTooltip(tooltip);
                    } else {
                        POOL.returnObject(tab.getTooltip());
                        tab.setTooltip(null);
                    }
                };
                tab.selectedProperty().addListener(listener);
                tab.getProperties().put(PROP_KEY, listener);
            }
        }
    }

    /**
     * 卸载提示
     *
     * @param target 提示
     */
    public static void uninstall(EventTarget target) {
        if (target instanceof Node node) {
            Tooltip tooltip = getTooltip(node);
            if (tooltip != null) {
                Tooltip.uninstall(node, tooltip);
            }
        } else if (target instanceof Tab tab) {
            tab.setTooltip(null);
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
