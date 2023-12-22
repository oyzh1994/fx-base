package cn.oyzh.fx.plus.stage;

import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.NonNull;

/**
 * 舞台扩展
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class StageExt extends Stage implements StageWrapper {

    public StageExt(@NonNull StageAttribute attribute, Window owner) {
        this.init(attribute, owner);
        this.setProp("_stageReference", this);
    }

    public StageExt(Window owner) {
        if (owner != null) {
            this.initOwner(owner);
        }
        this.setProp("_stageReference", this);
    }

    @Override
    public Stage stage() {
        return this;
    }
}
