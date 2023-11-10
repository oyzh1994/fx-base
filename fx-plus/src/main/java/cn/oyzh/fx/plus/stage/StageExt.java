package cn.oyzh.fx.plus.stage;

import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 舞台扩展
 *
 * @author oyzh
 * @since 2023/10/12
 */
@Slf4j
public class StageExt extends Stage implements StageWrapper {

    public StageExt(@NonNull StageAttribute window, Window owner) {
        this.init(window, owner);
        this.setProp("_stageReference", this);
    }

    @Override
    public Stage stage() {
        return this;
    }
}
