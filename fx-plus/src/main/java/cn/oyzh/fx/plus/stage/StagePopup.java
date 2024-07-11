package cn.oyzh.fx.plus.stage;

import javafx.scene.Parent;
import javafx.stage.Window;
import lombok.NonNull;

/**
 * 舞台弹窗
 *
 * @author oyzh
 * @since 2024/07/11
 */
public class StagePopup extends StageExt {

    public StagePopup(@NonNull Window owner) {
        super(owner, null, null, null);
    }

    public StagePopup(@NonNull Window owner, Parent root) {
        super(owner, root, null, null);
    }
}
