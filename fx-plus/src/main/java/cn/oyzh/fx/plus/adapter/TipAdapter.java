package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.util.TooltipUtil;
import javafx.event.EventTarget;

/**
 * 提示标题
 *
 * @author oyzh
 * @since 2023/3/15
 */
public interface TipAdapter extends EventTarget {

    /**
     * 获取提示标题
     *
     * @return 提示标题
     */
    default String getTipText() {
        return this.tipText();
    }

    /**
     * 获取提示标题实现
     *
     * @return 提示标题
     */
    default String tipText() {
        return TooltipUtil.getTipText(this);
    }

    /**
     * 设置提示标题
     *
     * @param tipText 提示标题
     */
    default void setTipText(String tipText) {
        this.tipText(tipText);
    }

    /**
     * 设置提示标题实现
     *
     * @param tipText 提示标题
     */
    default void tipText(String tipText) {
        TooltipUtil.setTipText(this, tipText);
    }
}
