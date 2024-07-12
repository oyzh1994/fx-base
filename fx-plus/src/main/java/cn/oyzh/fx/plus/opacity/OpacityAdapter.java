package cn.oyzh.fx.plus.opacity;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.window.StageWrapper;
import javafx.stage.Window;


/**
 * @author oyzh
 * @since 2024/4/19
 */
public interface OpacityAdapter extends PropAdapter {

    /**
     * 禁用透明度
     */
    default void disableOpacity() {
        this.setProp("_enableOpacity", false);
    }

    /**
     * 启用透明度
     */
    default void enableOpacity() {
        this.removeProp("_enableOpacity");
    }

    /**
     * 设置启用透明度
     *
     * @param enableOpacity 启用透明度
     */
    default void setEnableOpacity(boolean enableOpacity) {
        this.setProp("_enableOpacity", enableOpacity);
    }

    /**
     * 是否启用透明度
     *
     * @return 结果
     */
    default boolean isEnableOpacity() {
        Boolean b = this.getProp("_enableOpacity");
        return b == null || b;
    }

    /**
     * 更改
     *
     * @param opacity 透明度
     */
    default void changeOpacity(double opacity) {
        if (this.isEnableOpacity()) {
            switch (this) {
                case Window window -> window.setOpacity(opacity);
                case StageWrapper wrapper -> wrapper.stage().setOpacity(opacity);
                default -> {
                }
            }
        }
    }
}
