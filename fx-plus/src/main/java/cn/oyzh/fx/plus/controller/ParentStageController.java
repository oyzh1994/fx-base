package cn.oyzh.fx.plus.controller;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.stage.WindowEvent;

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
    protected void setWindow( StageAdapter stage) {
        super.setWindow(stage);
        if (CollectionUtil.isNotEmpty(this.getSubControllers())) {
            for (StageController controller : this.getSubControllers()) {
                controller.setWindow(stage);
                if (controller instanceof SubStageController subController) {
                    subController.setParent(this);
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
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        if (CollectionUtil.isNotEmpty(this.getSubControllers())) {
            for (StageController controller : this.getSubControllers()) {
                controller.onWindowShown(event);
            }
        }
    }

    @Override
    public void onWindowCloseRequest(WindowEvent event) {
        super.onWindowCloseRequest(event);
        if (CollectionUtil.isNotEmpty(this.getSubControllers())) {
            for (StageController controller : this.getSubControllers()) {
                controller.onWindowCloseRequest(event);
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
