package cn.oyzh.fx.plus.stage;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.common.util.SystemUtil;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.handler.EscHideHandler;
import cn.oyzh.fx.plus.handler.TabSwitchHandler;
import cn.oyzh.fx.plus.util.CursorUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.IconUtil;
import cn.oyzh.fx.plus.util.StyleUtil;
import cn.oyzh.fx.plus.view.FXMLLoaderExt;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 舞台扩展
 *
 * @author oyzh
 * @since 2023/3/2
 */
@Slf4j
public class StageExt extends Stage implements PropAdapter, StateAdapter {

    /**
     * 是否已使用过
     */
    @Getter
    private boolean used;

    public StageExt(@NonNull StageAttribute window, Window owner) {
        this.initStage(window, owner);
        // 监听显示属性
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.onClosed();
            } else if (!this.used) {
                this.used = true;
            }
        });
    }

    /**
     * 初始化舞台
     *
     * @param attribute 舞台属性
     * @param owner     父窗口
     */
    private void initStage(@NonNull StageAttribute attribute, Window owner) {
        // 初始化模态
        this.initModality(attribute.modality());
        // 设置窗口样式
        this.initStyle(attribute.stageStyle());
        // 初始化stage
        this.setTitle(attribute.title());
        this.setMaximized(attribute.maximized());
        this.setResizable(attribute.resizeable());
        // 设置icon
        if (ArrayUtil.isNotEmpty(attribute.iconUrls())) {
            this.getIcons().addAll(IconUtil.getIcons(attribute.iconUrls()));
        }
        // 初始化加载器
        FXMLLoaderExt loader = new FXMLLoaderExt();
        // 加载根节点
        Parent root = loader.load(attribute.value());
        if (root == null) {
            log.error("load root fail.");
            return;
        }
        // 设置controller
        this.setProp("_controller", loader.getController());
        // 设置scene
        FXUtil.runWait(() -> this.setScene(new Scene(root)));
        // 初始化父窗口
        if (owner != null) {
            this.initOwner(owner);
        }
        // 设置美化css类
        root.getStyleClass().addAll(
                JMetroStyleClass.BACKGROUND,
                JMetroStyleClass.LIGHT_BUTTONS,
                JMetroStyleClass.TABLE_GRID_LINES,
                JMetroStyleClass.UNDERLINE_TAB_PANE,
                JMetroStyleClass.ALTERNATING_ROW_COLORS
        );
        // 扩展风格
        new JMetro(this.root(), Style.LIGHT);
        // 加载自定义css文件
        if (ArrayUtil.isNotEmpty(attribute.cssUrls())) {
            root.getStylesheets().addAll(StyleUtil.split(attribute.cssUrls()));
        }
        // 设置事件
        if (this.controller() instanceof StageListener listener) {
            listener.onStageInitialize(this);
            this.setOnShown(listener::onStageShown);
            this.setOnHiding(listener::onStageHiding);
            this.setOnHidden(listener::onStageHidden);
            this.setOnShowing(listener::onStageShowing);
            this.setOnCloseRequest(listener::onStageCloseRequest);
        }
    }

    /**
     * 追加标题
     *
     * @param append 追加内容
     */
    public void appendTitle(String append) {
        this.appendTitle(append, -1);
    }

    /**
     * 追加标题
     *
     * @param append   追加内容
     * @param liveTime 追加内容存活时间
     */
    public void appendTitle(String append, int liveTime) {
        if (StrUtil.isNotBlank(append)) {
            String title = this.getProp("_title");
            if (title == null) {
                title = this.getTitle();
                this.setProp("_title", title);
            }
            String newTitle = title + append;
            this.setTitle(newTitle);
            if (liveTime >= 0) {
                ExecutorUtil.start(this::restoreTitle, liveTime);
            }
        }
    }

    /**
     * 恢复标题
     */
    public void restoreTitle() {
        this.setTitle(this.getProp("_title"));
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTitle1(String title) {
        if (title != null) {
            FXUtil.runWait(() -> super.setTitle(title));
        }
    }

    /**
     * 获取controller
     *
     * @return controller
     */
    public Object controller() {
        return this.getProp("_controller");
    }

    /**
     * 获取controller类
     *
     * @return controller类
     */
    public Class<?> controllerClass() {
        Object controller = this.controller();
        return controller == null ? null : controller.getClass();
    }

    /**
     * 关闭事件
     */
    protected void onClosed() {
        try {
            this.clearProps();
            // 取消eas监听
            this.unSwitchOnTab();
            this.unHideOnEscape();
            // 延迟gc
            SystemUtil.gcLater();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 显示
     */
    public void showStage() {
        if (!this.isShowing()) {
            FXUtil.runWait(super::show);
        }
    }

    @Override
    public void close() {
        FXUtil.runLater(super::close);
    }

    /**
     * 获取根节点
     *
     * @return 根节点
     */
    public Parent root() {
        if (this.getScene() == null) {
            return null;
        }
        return this.getScene().getRoot();
    }

    /**
     * hand鼠标样式
     */
    public void handCursor() {
        CursorUtil.handCursor(this);
    }

    /**
     * wait鼠标样式
     */
    public void waitCursor() {
        CursorUtil.waitCursor(this);
    }

    /**
     * 默认鼠标样式
     */
    public void defaultCursor() {
        CursorUtil.defaultCursor(this);
    }

    @Override
    public void toFront() {
        FXUtil.runLater(() -> {
            this.toFront();
            this.setIconified(false);
        });
    }

    /**
     * 设置按下eac时隐藏窗口
     */
    public void hideOnEscape() {
        if (!EscHideHandler.exists(this)) {
            EscHideHandler.init(this);
        }
    }

    /**
     * 取消按下eac时隐藏窗口
     */
    public void unHideOnEscape() {
        EscHideHandler.destroy(this);
    }

    /**
     * 是否按下esc时隐藏窗口
     *
     * @return 结果
     */
    public boolean isHideOnEscape() {
        return EscHideHandler.exists(this);
    }

    /**
     * 设置按下tab时切换组件
     */
    public void switchOnTab() {
        if (!TabSwitchHandler.exists(this)) {
            TabSwitchHandler.init(this);
        }
    }

    /**
     * 取消按下tab时切换组件
     */
    public void unSwitchOnTab() {
        TabSwitchHandler.destroy(this);
    }

    /**
     * 是否按下tab时切换组件
     *
     * @return 结果
     */
    public boolean isSwitchOnTab() {
        return TabSwitchHandler.exists(this);
    }
}
