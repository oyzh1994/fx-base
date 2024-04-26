package cn.oyzh.fx.plus.stage;

import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.opacity.OpacityAdapter;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.NonNull;

/**
 * 舞台扩展
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class StageExt extends Stage implements StageWrapper, OpacityAdapter {

    public StageExt(@NonNull StageAttribute attribute, Window owner) {
        this.init(attribute, owner);
        this.setProp("_stageReference", this);
    }

    public StageExt(Window owner) {
        if (owner != null) {
            this.initOwner(owner);
        }
        this.setProp("_stageReference", this);
        NodeManager.init(this);
    }

    @Override
    public Stage stage() {
        return this;
    }
}
