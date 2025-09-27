package cn.oyzh.fx.plus.adapter;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.object.Destroyable;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;


/**
 * 可销毁适配器
 *
 * @author oyzh
 * @since 2023/1/29
 */
public interface DestroyAdapter extends Destroyable {

    default void initDestroyListener() {
        if (this instanceof Node node) {
//            ChangeListener<Scene> listener = (observable, oldValue, newValue) -> {
//                if (newValue == null) {
//                    this.destroy();
//                    JulLog.debug("{} is destroyed.", this.getClass().getSimpleName() + "@" + this);
//                }
//            };
//            node.sceneProperty().addListener(listener);
            node.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    this.destroy();
                    if (JulLog.isDebugEnabled()) {
                        JulLog.debug("{} is destroyed.", this.getClass().getSimpleName() + "@" + this);
                    }
                }
            });
        } else if (this instanceof MenuItem node) {
//            ChangeListener<ContextMenu> listener = (observable, oldValue, newValue) -> {
//                if (newValue == null) {
//                    this.destroy();
//                    JulLog.debug("{} is destroyed.", this.getClass().getSimpleName() + "@" + this);
//                }
//            };
//            node.parentPopupProperty().addListener(listener);
            node.parentPopupProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    this.destroy();
                    if (JulLog.isDebugEnabled()) {
                        JulLog.debug("{} is destroyed.", this.getClass().getSimpleName() + "@" + this);
                    }
                }
            });
        }
    }
}
