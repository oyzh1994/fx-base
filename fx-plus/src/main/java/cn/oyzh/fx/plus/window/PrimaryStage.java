package cn.oyzh.fx.plus.window;

import cn.oyzh.fx.plus.opacity.OpacityAdapter;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * 主舞台
 *
 * @author oyzh
 * @since 2023/10/12
 */
@Getter
public class PrimaryStage implements StageAdapter, OpacityAdapter {

    /**
     * 舞台
     */
    @Accessors(fluent = true, chain = false)
    private final Stage stage;

    public PrimaryStage(@NonNull Stage primaryStage, @NonNull StageAttribute attribute, Window owner) {
        this.stage = primaryStage;
        this.stage.getProperties().put(StageManager.REF_ATTR, this);
        this.init(attribute, owner);
    }
}
