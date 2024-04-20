package cn.oyzh.fx.plus.stage;

import cn.oyzh.fx.plus.node.NodeManager;
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
public class PrimaryStage implements StageWrapper, OpacityAdapter {

    /**
     * 舞台
     */
    @Accessors(fluent = true, chain = false)
    private final Stage stage;

    public PrimaryStage(@NonNull Stage primaryStage, @NonNull StageAttribute attribute, Window owner) {
        this.stage = primaryStage;
        this.stage.getProperties().put("_stageReference", this);
        this.init(attribute, owner);
        NodeManager.init(this);
    }
}
