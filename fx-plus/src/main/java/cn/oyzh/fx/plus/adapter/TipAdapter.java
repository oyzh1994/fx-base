package cn.oyzh.fx.plus.adapter;

import cn.oyzh.fx.plus.util.TooltipUtil;
import javafx.collections.ObservableMap;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.input.KeyCombination;

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
    private String tipText() {
        return TooltipUtil.getTipText(this);
    }

    /**
     * 设置提示标题
     *
     * @param tipText 提示标题
     */
    default void setTipText(String tipText) {
        this.setProp("_tipText", tipText);
        KeyCombination combination = this.getTipKeyCombination();
        if (combination != null) {
            this.tipText(tipText + "(" + combination + ")");
        } else {
            this.tipText(tipText);
        }
    }

    /**
     * 设置提示标题实现
     *
     * @param tipText 提示标题
     */
    private void tipText(String tipText) {
        TooltipUtil.setTipText(this, tipText);
    }

    /**
     * 设置追加提示标题
     *
     * @param appendTipText 追加提示标题
     */
    @Deprecated
    default void setAppendTipText(String appendTipText) {
        String tipText = this.getTipText();
        this.tipText(tipText == null ? appendTipText : tipText + appendTipText);
    }

    /**
     * 获取追加提示标题
     */
    @Deprecated
    default String getAppendTipText() {
        return null;
    }

    /**
     * 设置提示快捷键
     *
     * @param combination 快捷键
     */
    default void setTipKeyCombination(KeyCombination combination) {
        this.setProp("keyCombination", combination);
        String tipText = this.getTipText();
        String text = combination.toString();
        if (this instanceof Node node && "filterProcess".equals(node.getId())) {
            ObservableMap<Object, Object> properties = node.getProperties();
            System.out.println(properties);
        }
        this.tipText(tipText == null ? text : tipText + "(" + text + ")");
    }

    /**
     * 获取提示快捷键
     */
    default KeyCombination getTipKeyCombination() {
        return this.getProp("keyCombination");
    }

}
