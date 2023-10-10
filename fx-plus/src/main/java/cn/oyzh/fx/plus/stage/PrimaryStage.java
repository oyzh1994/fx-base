package cn.oyzh.fx.plus.stage;

import cn.oyzh.fx.plus.view.FXStage;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;

/**
 * 主窗口
 *
 * @author oyzh
 * @since 2023/3/2
 */
public class PrimaryStage extends FXStage {

    /**
     * 窗口，不使用弱引用，避免被回收
     */
    @Getter
    private final Stage stage;

    public PrimaryStage(@NonNull Stage stage) {
        super(stage);
        this.stage = stage;
    }
}
