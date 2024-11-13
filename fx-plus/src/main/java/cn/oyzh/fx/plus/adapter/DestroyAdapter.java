package cn.oyzh.fx.plus.adapter;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.Destroyable;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.function.BiConsumer;


/**
 * 文本组件适配器
 *
 * @author oyzh
 * @since 2023/1/29
 */
public interface DestroyAdapter extends Destroyable {

    default void initDestroyListener() {
        if (this instanceof Node node) {
            ChangeListener<Scene> listener = (observable, oldValue, newValue) -> {
                if (newValue == null) {
                    this.destroy();
                    JulLog.debug("{} is destroyed.", this.getClass().getSimpleName() + "@" + this);
                }
            };
            node.sceneProperty().addListener(listener);
        // } else if (this instanceof TreeItem<?> node) {
        //     ChangeListener<TreeItem<?>> listener = (observable, oldValue, newValue) -> {
        //         if (newValue == null) {
        //             this.destroy();
        //             JulLog.debug("{} is destroyed.", this.getClass().getSimpleName() + "@" + this);
        //         }
        //     };
        //     node.parentProperty().addListener(listener);
        } else if (this instanceof MenuItem node) {
            ChangeListener<ContextMenu> listener = (observable, oldValue, newValue) -> {
                if (newValue == null) {
                    this.destroy();
                    JulLog.debug("{} is destroyed.", this.getClass().getSimpleName() + "@" + this);
                }
            };
            node.parentPopupProperty().addListener(listener);
        }
    }
}
