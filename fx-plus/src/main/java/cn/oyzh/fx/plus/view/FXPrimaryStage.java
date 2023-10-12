package cn.oyzh.fx.plus.view;

import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;

/**
 * 主窗口
 *
 * @author oyzh
 * @since 2023/3/2
 */
@Deprecated
public class FXPrimaryStage extends FXStage {

    /**
     * 窗口，不使用弱引用，避免被回收
     */
    @Getter
    private Stage stage;

    public FXPrimaryStage(@NonNull Stage stage) {
        super(stage);
        this.stage = stage;
    }
}
