package cn.oyzh.fx.plus.stage;

import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * 舞台弹窗
 *
 * @author oyzh
 * @since 2024/07/11
 */
public class StagePopup extends StageExt {

    public StagePopup(Window owner) {
        this(owner, null);
    }

    public StagePopup(Window owner, Parent root) {
        super(owner, root, null, null);
        if (owner == null) {
            this.initModality(Modality.APPLICATION_MODAL);
        } else {
            this.initModality(Modality.WINDOW_MODAL);
        }
    }
}
