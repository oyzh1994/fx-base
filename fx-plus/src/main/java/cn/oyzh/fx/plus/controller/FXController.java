package cn.oyzh.fx.plus.controller;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.plus.view.FXView;
import cn.oyzh.fx.plus.view.FXViewListener;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.NonNull;
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
public abstract class FXController implements FXViewListener {

    /**
     * 窗口
     */
    @Getter
    protected FXView view;

    /**
     * 父组件
     */
    private FXController parent;

    ///**
    // * 释放标志位
    // */
    //@Setter
    //@Getter
    //private boolean released;

    /**
     * 设置窗口
     *
     * @param view 窗口
     */
    public void setView(@NonNull FXView view) {
        this.view = view;
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.setView(view);
                controller.parent = this;
            }
        }
    }

    /**
     * 获取父组件
     *
     * @return 父组件
     */
    public FXController parent() {
        return this.parent;
    }

    @Override
    public void onViewShowing(WindowEvent event) {
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.onViewShowing(event);
            }
        }
    }

    @Override
    public void onViewShown(WindowEvent event) {
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.onViewShown(event);
            }
        }
        this.bindListeners();
    }

    @Override
    public void onViewHiding(WindowEvent event) {
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.onViewHiding(event);
            }
        }
    }

    @Override
    public void onViewHidden(WindowEvent event) {
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.onViewHidden(event);
            }
        }
    }

    @Override
    public void onViewCloseRequest(WindowEvent event) {
        if (CollUtil.isNotEmpty(this.getSubControllers())) {
            for (FXController controller : this.getSubControllers()) {
                controller.onViewCloseRequest(event);
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
     * 关闭页面
     */
    protected void closeView() {
        if (this.view != null) {
            this.view.close();
        }
    }

    /**
     * 获取页面属性
     *
     * @param key key
     * @param <T> 泛型
     * @return 值
     */
    protected <T> T getViewProp(String key) {
        return this.view == null ? null : this.view.getProp(key);
    }

    @Override
    public void onViewInitialize(FXView view) {
        // 设置页面
        this.setView(view);
    }

    public List<FXController> getSubControllers() {
        return Collections.emptyList();
    }

}
