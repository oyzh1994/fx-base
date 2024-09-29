package cn.oyzh.fx.plus.controller;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.stage.WindowEvent;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * 父窗口控制器
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class ParentStageController extends StageController {

    @Override
    protected void setWindow(@NonNull StageAdapter stage) {
        super.setWindow(stage);
        if (CollectionUtil.isNotEmpty(this.getSubControllers())) {
            for (StageController controller : this.getSubControllers()) {
                controller.setWindow(stage);
                if (controller instanceof SubStageController subController) {
                    subController.parent(this);
                }
            }
        }
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        if (CollectionUtil.isNotEmpty(this.getSubControllers())) {
            for (StageController controller : this.getSubControllers()) {
                controller.onWindowShowing(event);
            }
        }
    }

    @Override
    public void onStageShown(WindowEvent event) {
        super.onStageShown(event);
        if (CollectionUtil.isNotEmpty(this.getSubControllers())) {
            for (StageController controller : this.getSubControllers()) {
                controller.onStageShown(event);
            }
        }
    }

    @Override
    public void onStageCloseRequest(WindowEvent event) {
        super.onStageCloseRequest(event);
        if (CollectionUtil.isNotEmpty(this.getSubControllers())) {
            for (StageController controller : this.getSubControllers()) {
                controller.onStageCloseRequest(event);
            }
        }
    }

    @Override
    public void onWindowHiding(WindowEvent event) {
        super.onWindowHiding(event);
        if (CollectionUtil.isNotEmpty(this.getSubControllers())) {
            for (StageController controller : this.getSubControllers()) {
                controller.onWindowHiding(event);
            }
        }
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        if (CollectionUtil.isNotEmpty(this.getSubControllers())) {
            for (StageController controller : this.getSubControllers()) {
                controller.onWindowHidden(event);
            }
        }
    }

    @Override
    public void onSystemExit() {
        super.onSystemExit();
        if (CollectionUtil.isNotEmpty(this.getSubControllers())) {
            for (StageController controller : this.getSubControllers()) {
                controller.onSystemExit();
            }
        }
    }

    /**
     * 获取子控制器列表
     *
     * @return 子控制器列表
     */
    public List<? extends StageController> getSubControllers() {
        return Collections.emptyList();
    }
}
