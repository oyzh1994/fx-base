package cn.oyzh.fx.plus.controller;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.event.EventListener;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageListener;
import javafx.stage.WindowEvent;

/**
 * 窗口控制器
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class StageController extends Controller implements StageListener, EventListener {

    /**
     * 舞台
     */
    protected StageAdapter stage;

    public StageAdapter getStage() {
        return stage;
    }

    /**
     * 设置舞台
     *
     * @param stage 舞台
     */
    protected void setWindow( StageAdapter stage) {
        this.stage = stage;
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        // 设置页面
        this.setWindow(stage);
        // 处理标题
        if (StringUtil.isEmpty(this.stage.title())) {
            String title = this.getViewTitle();
            if (title != null) {
                this.stage.title(title);
            }
        }
        NodeManager.init(this);
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        EventListener.super.register();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.bindListeners();
    }

    @Override
    public void onWindowCloseRequest(WindowEvent event) {
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        EventListener.super.unregister();
    }

    @Override
    public void onSystemExit() {
    }

    @Override
    protected void closeWindow() {
        if (this.stage != null) {
            this.stage.disappear();
        }
    }

    @Override
    protected <T> T getWindowProp(String key) {
        return this.stage == null ? null : this.stage.getProp(key);
    }

    public String getViewTitle() {
        return null;
    }

    protected void disable() {
        this.stage.disable();
    }

    protected void enable() {
        this.stage.enable();
    }

    protected void restoreTitle() {
        this.stage.restoreTitle();
    }

    protected void appendTitle(String title) {
        this.stage.appendTitle(title);
    }
}
