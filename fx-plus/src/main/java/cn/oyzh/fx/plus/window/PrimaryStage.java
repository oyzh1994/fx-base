package cn.oyzh.fx.plus.window;

import cn.oyzh.fx.plus.opacity.OpacityAdapter;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * 主舞台
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class PrimaryStage implements StageAdapter, OpacityAdapter {

    /**
     * 舞台
     */
    private final Stage stage;

    public PrimaryStage(Stage primaryStage, StageAttribute attribute, Window owner) {
        this.stage = primaryStage;
        this.stage.getProperties().put(StageManager.REF_ATTR, this);
        this.init(attribute, owner);
    }

    @Override
    public Stage stage() {
        return this.stage;
    }
}
