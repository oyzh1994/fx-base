package cn.oyzh.fx.plus.controller;

import cn.oyzh.fx.plus.stage.StageListener;
import cn.oyzh.fx.plus.stage.StageWrapper;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.NonNull;

/**
 * 组件控制器
 *
 * @author oyzh
 * @since 2023/10/12
 */
//@Slf4j
public class Controller implements StageListener {

    /**
     * 舞台
     */
    @Getter
    protected StageWrapper stage;

    /**
     * 设置舞台
     *
     * @param stage 舞台
     */
    protected void setStage(@NonNull StageWrapper stage) {
        this.stage = stage;
    }

    @Override
    public void onStageInitialize(StageWrapper stage) {
        // 设置页面
        this.setStage(stage);
    }

    @Override
    public void onStageShowing(WindowEvent event) {
    }

    @Override
    public void onStageShown(WindowEvent event) {
        this.bindListeners();
    }

    @Override
    public void onStageCloseRequest(WindowEvent event) {
    }

    @Override
    public void onStageHiding(WindowEvent event) {
    }

    @Override
    public void onStageHidden(WindowEvent event) {
    }

    @Override
    public void onSystemExit() {
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
            this.stage.disappear();
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

}
