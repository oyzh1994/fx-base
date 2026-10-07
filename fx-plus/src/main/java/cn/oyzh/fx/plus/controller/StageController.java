package cn.oyzh.fx.plus.controller;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.event.EventListener;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
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

    /**
     * 获取舞台
     *
     * @return 舞台
     */
    public StageAdapter getStage() {
        return stage;
    }

    /**
     * 设置舞台
     *
     * @param stage 舞台
     */
    protected void setWindow(StageAdapter stage) {
        this.stage = stage;
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        // 设置页面
        this.setWindow(stage);
        NodeManager.init(this);
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        EventListener.super.register();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.bindListeners();
        // 处理标题
        if (StringUtil.isEmpty(this.stage.title())) {
            String title = this.getViewTitle();
            this.stage.title(title);
        }
    }

    @Override
    public void onWindowCloseRequest(WindowEvent event) {
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        EventListener.super.unregister();
        this.destroy();
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
    public void setProp(String key, Object value) {
        if (this.stage != null) {
            this.stage.setProp(key, value);
        }
    }

    @Override
    public <T> T getProp(String key) {
        return this.stage == null ? null : this.stage.getProp(key);
    }

    @Override
    public boolean hasProp(String key) {
        return this.stage != null && this.stage.hasProp(key);
    }

    @Override
    public <T> T removeProp(String key) {
        return this.stage == null ? null : this.stage.removeProp(key);
    }

    @Override
    public void clearProps() {
        if (this.stage != null) {
            this.stage.clearProps();
        }
    }

    /**
     * 获取视图标题
     *
     * @return 视图标题
     */
    public String getViewTitle() {
        return null;
    }

    /**
     * 禁用窗口
     */
    protected void disable() {
        this.stage.disable();
    }

    /**
     * 启用窗口
     */
    protected void enable() {
        this.stage.enable();
    }

    /**
     * 恢复标题
     */
    protected void restoreTitle() {
        this.stage.restoreTitle();
    }

    /**
     * 追加标题
     *
     * @param title 标题
     */
    protected void appendTitle(String title) {
        this.stage.appendTitle(title);
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    protected void setTitle(String title) {
        this.stage.title(title);
    }

    /**
     * 获取标题
     *
     * @return 标题
     */
    protected String getTitle() {
        return this.stage.title();
    }

}
