package cn.oyzh.fx.plus.window;

import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.opacity.OpacityAdapter;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.NonNull;

/**
 * 舞台扩展
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class StageExt extends Stage implements StageAdapter, OpacityAdapter {

    public StageExt(@NonNull StageAttribute attribute, Window owner) {
        this.init(attribute, owner);
        this.setProp(StageManager.REF_ATTR, this);
    }

    public StageExt(Window owner) {
        this(owner, null, null, null);
    }

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
    }

    @Override
    public Stage stage() {
        return this;
    }
}
