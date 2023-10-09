package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.event.EventTarget;

/**
 * 提示标题
 *
 * @author oyzh
 * @since 2023/3/15
 */
public interface TipAdapter {

    /**
     * 获取提示标题
     *
     * @return 提示标题
     */
    String getTipText();

    /**
     * 获取提示标题实现
     *
     * @return 提示标题
     */
    default String _getTipText() {
        if (this instanceof EventTarget target) {
            return ControlUtil.getTipTitle(target);
        }
        return null;
    }

    /**
     * 设置提示标题
     *
     * @param tipText 提示标题
     */
    void setTipText(String tipText);

    /**
     * 设置提示标题实现
     *
     * @param tipText 提示标题
     */
    default void _setTipText(String tipText) {
        if (this instanceof EventTarget target) {
            ControlUtil.setTipTitle(target, tipText);
        }
    }
}
