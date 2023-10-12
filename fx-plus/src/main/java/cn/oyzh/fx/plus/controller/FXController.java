package cn.oyzh.fx.plus.controller;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.plus.stage.StageListener;
import cn.oyzh.fx.plus.stage.StageWrapper;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * fxController
 *
 * @author oyzh
 * @since 2023/1/4
 */
@Slf4j
public class FXController implements StageListener {
// public class FXController implements FXViewListener, StageListener {

    // /**
    //  * 窗口
    //  */
    // @Getter
    // protected FXView view;

    /**
     * 父组件
     */
    @Getter
    @Accessors(fluent = true, chain = false)
    private FXController parent;

    /**
     * 舞台
     */
    @Getter
    protected StageWrapper stage;

    // /**
    //  * 设置窗口
    //  *
    //  * @param view 窗口
    //  */
    // public void setView(@NonNull FXView view) {
    //     this.view = view;
    //     if (CollUtil.isNotEmpty(this.getSubControllers())) {
    //         for (FXController controller : this.getSubControllers()) {
    //             controller.setView(view);
    //             controller.parent = this;
    //         }
    //     }
    // }

    /**
     * 设置舞台
     *
     * @param stage 舞台
     */
    public void setStage(@NonNull StageWrapper stage) {
        this.stage = stage;
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.setStage(stage);
                controller.parent = this;
            }
        }
    }

    // /**
    //  * 获取父组件
    //  *
    //  * @return 父组件
    //  */
    // public FXController parent() {
    //     return this.parent;
    // }

    // @Override
    // public void onViewShowing(WindowEvent event) {
    //     if (CollUtil.isNotEmpty(this.getSubControllers())) {
    //         for (FXController controller : this.getSubControllers()) {
    //             controller.onViewShowing(event);
    //         }
    //     }
    // }
    //
    // @Override
    // public void onViewShown(WindowEvent event) {
    //     if (CollUtil.isNotEmpty(this.getSubControllers())) {
    //         for (FXController controller : this.getSubControllers()) {
    //             controller.onViewShown(event);
    //         }
    //     }
    //     this.bindListeners();
    // }
    //
    // @Override
    // public void onViewHiding(WindowEvent event) {
    //     if (CollUtil.isNotEmpty(this.getSubControllers())) {
    //         for (FXController controller : this.getSubControllers()) {
    //             controller.onViewHiding(event);
    //         }
    //     }
    // }
    //
    // @Override
    // public void onViewHidden(WindowEvent event) {
    //     if (CollUtil.isNotEmpty(this.getSubControllers())) {
    //         for (FXController controller : this.getSubControllers()) {
    //             controller.onViewHidden(event);
    //         }
    //     }
    // }
    //
    // @Override
    // public void onViewCloseRequest(WindowEvent event) {
    //     if (CollUtil.isNotEmpty(this.getSubControllers())) {
    //         for (FXController controller : this.getSubControllers()) {
    //             controller.onViewCloseRequest(event);
    //         }
    //     }
    // }

    @Override
    public void onStageInitialize(StageWrapper stage) {
        // 设置页面
        this.setStage(stage);
    }

    @Override
    public void onStageShowing(WindowEvent event) {
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.onStageShowing(event);
            }
        }
    }

    @Override
    public void onStageShown(WindowEvent event) {
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.onStageShown(event);
            }
        }
    }

    @Override
    public void onStageCloseRequest(WindowEvent event) {
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.onStageCloseRequest(event);
            }
        }
    }

    @Override
    public void onStageHiding(WindowEvent event) {
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.onStageHiding(event);
            }
        }
    }

    @Override
    public void onStageHidden(WindowEvent event) {
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.onStageHidden(event);
            }
        }
    }

    @Override
    public void onSystemExit() {
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.onSystemExit();
            }
        }
    }

    /**
     * 绑定监听器相关业务
     */
    protected void bindListeners() {

    }

    /**
     * 关闭舞台
     */
    protected void closeStage() {
        if (this.stage != null) {
            this.stage.close();
        }
    }

    /**
     * 获取属性
     *
     * @param key key
     * @param <T> 泛型
     * @return 值
     */
    protected <T> T getStageProp(String key) {
        return this.stage == null ? null : this.stage.getProp(key);
    }

    // /**
    //  * 关闭页面
    //  */
    // protected void closeView() {
    //     if (this.view != null) {
    //         this.view.close();
    //     }
    // }
    //
    // /**
    //  * 获取页面属性
    //  *
    //  * @param key key
    //  * @param <T> 泛型
    //  * @return 值
    //  */
    // protected <T> T getViewProp(String key) {
    //     return this.view == null ? null : this.view.getProp(key);
    // }

    // @Override
    // public void onViewInitialize(FXView view) {
    //     // 设置页面
    //     this.setView(view);
    // }

    public List<FXController> getSubControllers() {
        return Collections.emptyList();
    }

}
