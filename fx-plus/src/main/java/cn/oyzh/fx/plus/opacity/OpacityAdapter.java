package cn.oyzh.fx.plus.opacity;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.titlebar.TitleBar;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.stage.Window;


/**
 * @author oyzh
 * @since 2024/4/19
 */
public interface OpacityAdapter extends PropAdapter {

    String ENABLE_OPACITY_KEY = "enable:opacity";

    /**
     * 禁用透明度
     */
    default void disableOpacity() {
        this.setProp(ENABLE_OPACITY_KEY, false);
    }

    /**
     * 启用透明度
     */
    default void enableOpacity() {
        this.removeProp(ENABLE_OPACITY_KEY);
    }

    /**
     * 设置启用透明度
     *
     * @param enableOpacity 启用透明度
     */
    default void setEnableOpacity(boolean enableOpacity) {
        this.setProp(ENABLE_OPACITY_KEY, enableOpacity);
    }

    /**
     * 是否启用透明度
     *
     * @return 结果
     */
    default boolean isEnableOpacity() {
        Boolean b = this.getProp(ENABLE_OPACITY_KEY);
        return b == null || b;
    }

    /**
     * 更改
     *
     * @param opacity 透明度
     */
    default void changeOpacity(OpacityConfig opacity) {
        if (this.isEnableOpacity()) {
            try {
                // 窗口处理
                Float windowOpacity = opacity.getWindowOpacity();
                if (windowOpacity != null && !Float.isNaN(windowOpacity)) {
                    windowOpacity /= 100;
                    windowOpacity = OpacityManager.fixOpacity(windowOpacity);
                    if (this instanceof Window window) {
                        window.setOpacity(windowOpacity);
                    } else if (this instanceof StageAdapter adapter) {
                        adapter.setOpacity(windowOpacity);
                    }
                }
                // 标题栏处理
                Float titleOpacity = opacity.getTitleOpacity();
                if (titleOpacity != null && !Float.isNaN(titleOpacity) && this instanceof StageAdapter adapter) {
                    TitleBar titleBar = adapter.getTitleBar();
                    if (titleBar != null) {
                        titleOpacity /= 100;
                        titleOpacity = OpacityManager.fixOpacity(titleOpacity);
                        titleBar.setOpacity(titleOpacity);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JulLog.warn("changeOpacity error", ex);
            }
        }
    }
}
