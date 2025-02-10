package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.util.TooltipUtil;
import javafx.event.EventTarget;

/**
 * 提示标题
 *
 * @author oyzh
 * @since 2023/3/15
 */
public interface TipAdapter extends EventTarget, PropAdapter {

    /**
     * 获取提示标题
     *
     * @return 提示标题
     */
    default String getTipText() {
        String text = this.tipText();
        if (text == null) {
            return this.getProp("_tipText");
        }
        return text;
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
        this.setProp("_tipText", tipText);
    }

    /**
     * 设置提示标题实现
     *
     * @param tipText 提示标题
     */
    default void tipText(String tipText) {
        TooltipUtil.setTipText(this, tipText);
    }

    /**
     * 设置追加提示标题
     *
     * @param appendTipText 追加提示标题
     */
    default void setAppendTipText(String appendTipText) {
        String tipText = this.getTipText();
        this.tipText(tipText == null ? appendTipText : tipText + appendTipText);
    }

    /**
     * 获取追加提示标题
     */
    default String getAppendTipText() {
        return null;
    }
}
