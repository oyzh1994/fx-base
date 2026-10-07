package cn.oyzh.fx.plus.window;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.opacity.OpacityAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * 舞台扩展
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class StageExt extends Stage implements StageAdapter, OpacityAdapter, ThemeAdapter {

    /**
     * 根据舞台属性构造舞台
     *
     * @param attribute 舞台属性
     * @param owner     父窗口
     */
    public StageExt(StageAttribute attribute, Window owner) {
        this.init(attribute, owner);
        this.setProp(StageManager.REF_ATTR, this);
        ObjectWatcherManager.watch(this);
    }

    /**
     * 根据父窗口构造舞台
     *
     * @param owner 父窗口
     */
    public StageExt(Window owner) {
        this(owner, null, null, null);
    }

    /**
     * 构造舞台
     *
     * @param owner  父窗口
     * @param root   根节点
     * @param width  宽
     * @param height 高
     */
    public StageExt(Window owner, Parent root, Double width, Double height) {
        if (owner != null) {
            this.initOwner(owner);
        }
        this.setProp(StageManager.REF_ATTR, this);
        NodeManager.init(this);
        if (root != null) {
            this.root(root);
        }
        if (width != null) {
            this.setWidth(width);
        }
        if (height != null) {
            this.setHeight(height);
        }
        ObjectWatcherManager.watch(this);
    }

    @Override
    public Stage stage() {
        return this;
    }
}
