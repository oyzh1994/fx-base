package cn.oyzh.fx.plus.adapter;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Text;

/**
 * 文本组件适配器
 *
 * @author oyzh
 * @since 2023/1/29
 */
public interface TextAdapter extends PropAdapter{

    /**
     * 解除文本变化监听器
     */
    default void unbindTextChangeListener() {
        if (this instanceof Text text) {
            text.textProperty().unbind();
        } else if (this instanceof Labeled labeled) {
            labeled.textProperty().unbind();
        } else if (this instanceof TextInputControl inputControl) {
            inputControl.textProperty().unbind();
        // } else if (this instanceof GenericStyledArea<?, ?, ?> area) {
        }
    }

    /**
     * 移除文本变化监听器
     *
     * @param listener 监听器
     */
    default void removeTextChangeListener(ChangeListener<String> listener) {
        if (listener == null) {
            return;
        }
        if (this instanceof Text text) {
            text.textProperty().removeListener(listener);
        } else if (this instanceof Labeled labeled) {
            labeled.textProperty().removeListener(listener);
        } else if (this instanceof TextInputControl inputControl) {
            inputControl.textProperty().removeListener(listener);
        // } else if (this instanceof GenericStyledArea<?, ?, ?> area) {
        //     area.textProperty().removeListener(listener);
        }
    }

    /**
     * 添加文本变化事件
     *
     * @param listener 监听器
     */
    default void addTextChangeListener(ChangeListener<String> listener) {
        if (listener != null) {
//            synchronized (this) {
                if (this instanceof Text text) {
                    text.textProperty().addListener(listener);
//                    text.textProperty().addListener(new WeakChangeListener<>(listener));
                } else if (this instanceof Labeled labeled) {
                    labeled.textProperty().addListener(listener);
//                    labeled.textProperty().addListener(new WeakChangeListener<>(listener));
                } else if (this instanceof TextInputControl inputControl) {
                    inputControl.textProperty().addListener(listener);
//                    inputControl.textProperty().addListener(new WeakChangeListener<>(listener));
//                 } else if (this instanceof GenericStyledArea<?, ?, ?> area) {
//                     area.textProperty().addListener(listener);
//                    area.textProperty().addListener(new WeakChangeListener<>(listener));
                }
//            }
        }
    }

    /**
     * 获取去除空格的文本
     *
     * @return 文本
     */
    default String getTextTrim() {
        String str = null;
        if (this instanceof Text text) {
            str = text.getText();
        } else if (this instanceof Labeled labeled) {
            str = labeled.getText();
        } else if (this instanceof TextInputControl inputControl) {
            str = inputControl.getText();
        // } else if (this instanceof GenericStyledArea<?, ?, ?> area) {
        //     str = area.getText();
        }
        return str == null ? null : str.trim();
    }


    /**
     * 是否忽略节点改变事件
     *
     * @return 结果
     */
    default boolean isIgnoreChanged() {
        Object _ignoreChanged = this.getProp("_ignoreChanged");
        return _ignoreChanged instanceof Boolean && (boolean) _ignoreChanged;
    }

    /**
     * 设置忽略节点改变事件
     *
     * @param ignoreChanged 忽略节点改变事件
     */
    default void setIgnoreChanged(boolean ignoreChanged) {
        this.setProp("_ignoreChanged", ignoreChanged);
    }
}
