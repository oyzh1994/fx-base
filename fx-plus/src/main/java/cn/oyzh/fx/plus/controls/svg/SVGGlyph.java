package cn.oyzh.fx.plus.controls.svg;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.thread.Task;
import cn.oyzh.fx.common.thread.TaskManager;
import cn.oyzh.fx.plus.adapter.MouseAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.AnimationUtil;
import cn.oyzh.fx.plus.util.ControlUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.animation.RotateTransition;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;


/**
 * svg图像
 *
 * @author oyzh
 * @since 2022/5/31
 */
public class SVGGlyph extends Region implements NodeGroup, NodeAdapter, ThemeAdapter, MouseAdapter, TipAdapter, StateAdapter {

    /**
     * 图标地址
     */
    @Getter
    private String url;

    /**
     * 图标颜色
     */
    @Getter
    private Paint color;

    /**
     * 是否激活态
     */
    @Getter
    private boolean active;

    /**
     * 是否等待中
     */
    @Getter
    @Setter
    private boolean waiting;

    /**
     * 是否开启动画功能
     */
    @Setter
    @Getter
    private boolean enableWaiting = true;

    {
        NodeManager.init(this);
    }

    @Override
    public void enableTheme() {
        ThemeAdapter.super.enableTheme();
        this.getStyleClass().add("svg-glyph");
    }

    @Override
    public void disableTheme() {
        ThemeAdapter.super.disableTheme();
        this.getStyleClass().remove("svg-glyph");
    }

    /**
     * 设置初始鼠标
     *
     * @param initCursor 鼠标类型
     */
    public void setInitCursor(String initCursor) {
        if (initCursor != null) {
            this.setCursor(Cursor.cursor(initCursor));
        }
    }

    /**
     * 获取初始鼠标
     *
     * @return 初始鼠标
     */
    public String getInitCursor() {
        return this.getCursor().toString();
    }

    /**
     * 更新内容
     */
    private void updateContent() {
        // 获取图标
        SVGPath svgPath = (SVGPath) this.getShape();
        if (svgPath != null) {
            // // 更新颜色
            // Paint paint = ControlUtil.backgroundFill(this);
            // if (paint != svgPath.getColor()) {
            //     svgPath.setColor(paint);
            // }
            // if (paint != svgPath.getFill()) {
            //     svgPath.setFill(paint);
            // }
            // 更新鼠标
            if (!SVGManager.isLoading(svgPath) && this.getCursor() != svgPath.getCursor()) {
                svgPath.setCursor(this.getCursor());
            }
        }
    }

    /**
     * 初始化内容
     */
    public void initContent() {
        if (this.url == null) {
            return;
        }
        // 结束动画
        if (this.waitingTransition != null) {
            this.waitingTransition.stop();
            this.waitingTransition = null;
        }
        // 初始化角度
        this.setRotate(0);
        // 创建图标
        SVGPath svgPath =  SVGManager.load(this.url);
        // svgPath.setColor(this.color);
        // svgPath.setFill(this.color);
        svgPath.setCursor(this.getCursor());
        this.setShape(svgPath);
    }

    /**
     * 等待动画
     */
    private RotateTransition waitingTransition;

    /**
     * 初始化等待
     */
    public void initWaiting() {
        // 创建图标
        SVGPath svgPath = SVGManager.load("/fx-plus/font/loading.svg");
        SVGManager.setLoading(svgPath);
        // svgPath.setColor(this.color);
        // svgPath.setFill(this.color);
        svgPath.setCursor(Cursor.NONE);
        this.setShape(svgPath);
        // 初始化动画
        this.waitingTransition = AnimationUtil.rotate(this);
        this.waitingTransition.play();
    }

    @Override
    public void setOnMousePrimaryClicked(EventHandler<? super MouseEvent> handler) {
        if (handler != null) {
            if (this.enableWaiting) {
                MouseAdapter.super.setOnMousePrimaryClicked(event -> {
                    try {
                        this.startWaiting();
                        handler.handle(event);
                    } finally {
                        this.stopWaiting();
                    }
                });
            } else {
                MouseAdapter.super.setOnMousePrimaryClicked(handler);
            }
        }
    }

    @Override
    public EventHandler<? super MouseEvent> getOnMousePrimaryClicked() {
        return MouseAdapter.super.getOnMousePrimaryClicked();
    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

    public SVGGlyph() {
    }

    public SVGGlyph(@NonNull String url) {
        this();
        this.setUrl(url);
    }

    public SVGGlyph(@NonNull String url, @NonNull Color color) {
        this();
        this.setUrl(url);
        this.setColor(color);
    }

    public SVGGlyph(@NonNull String url, @NonNull String size) {
        this();
        this.setUrl(url);
        this.setSizeStr(size);
    }

    public SVGGlyph(@NonNull String url, double size) {
        this();
        this.setUrl(url);
        this.setSize(size);
    }

    /**
     * 设置svg地址
     *
     * @param url svg地址
     */
    public void setUrl(@NonNull String url) {
        this.url = url;
        this.initContent();
    }

    /**
     * 设置颜色
     *
     * @param color 颜色
     */
    public void setColor(String color) {
        try {
            if (StrUtil.isNotBlank(color)) {
                this.setColor(Color.valueOf(color.trim()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 设置颜色
     *
     * @param color 颜色
     */
    public void setColor(Paint color) {
        this.color = color;
        this._setColor(color);
    }

    /**
     * 设置颜色
     *
     * @param color 颜色
     */
    private void _setColor(Paint color) {
        // 默认颜色
        if (color == null) {
            this.addClass("svg-glyph");
        } else {// 自定义颜色
            this.removeClass("svg-glyph");
        }
        if (!this.isDisabled() && color != null) {
            FXUtil.runLater(() -> this.setBackground(ControlUtil.background(color)), 50);
        }
    }

    /**
     * 设置大小
     *
     * @param size 大小
     */
    public void setSize(double size) {
        if (size > 0) {
            this.setSizeStr(size + "");
        }
    }

    /**
     * 获取大小
     *
     * @return 大小
     */
    public double getSize() {
        return this.getWidth();
    }

    /**
     * 获取大小字符串形式
     *
     * @return 大小字符串形式
     */
    public String getSizeStr() {
        return ControlUtil.boundedWidth(this) + "," + ControlUtil.boundedHeight(this);
    }

    /**
     * 设置大小，符串形式
     *
     * @param size 大小字符串形式
     */
    public void setSizeStr(String size) {
        if (StrUtil.isBlank(size)) {    // 判断大小字符串是否为空
            return;
        }
        try {
            size = size.trim();    // 去除大小字符串的首尾空格
            double w, h;
            if (size.contains(",")) {    // 判断大小字符串中是否包含逗号
                String[] strArr = size.split(",");    // 使用逗号分割大小字符串
                w = Double.parseDouble(strArr[0]);    // 转换并解析第一个值作为宽度
                h = Double.parseDouble(strArr[1]);    // 转换并解析第二个值作为高度
            } else {
                w = h = Double.parseDouble(size);    // 解析大小字符串作为宽度和高度
            }
            this.setMinSize(w, h);    // 设置最小大小为宽度和高度
            this.setMaxSize(w, h);    // 设置最大大小为宽度和高度
            this.setPrefSize(w, h);    // 设置首选大小为宽度和高度
        } catch (Exception ex) {
            ex.printStackTrace();    // 打印异常信息
        }
    }

    /**
     * 开始等待动画
     *
     * @param task 任务
     */
    public void startWaiting(@NonNull Runnable task) {
        this.startWaiting();
        TaskManager.startDelay(task, 20);
    }

    /**
     * 开始等待动画
     *
     * @param task 任务
     */
    public void startWaiting(@NonNull Task task) {
        this.startWaiting();
        TaskManager.startDelay(task, 20);
    }

    /**
     * 开始等待动画
     */
    public void startWaiting() {
        SVGManager.startWaiting(this);
    }

    /**
     * 停止等待动画
     */
    public void stopWaiting() {
        SVGManager.stopWaiting(this);
    }

    @Override
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }

    @Override
    public boolean isEnableTheme() {
        return ThemeAdapter.super.isEnableTheme();
    }

    @Override
    public void setEnableTheme(boolean enableTheme) {
        ThemeAdapter.super.setEnableTheme(enableTheme);
    }

    @Override
    public void initNode() {
        this.setSize(16);
        this.setPickOnBounds(true);
        this.setCursor(Cursor.HAND);
        this.setFocusTraversable(false);
        this.setPadding(new Insets(0));
        this.cursorProperty().addListener((obs, t0, t1) -> this.updateContent());
        this.backgroundProperty().addListener((obs, t0, t1) -> this.updateContent());
        this.addClass("svg-glyph");
    }

    /**
     * 激活
     */
    public void active() {
        this.setActive(true);
    }

    /**
     * 设置激活态
     *
     * @param active 激活态
     */
    public void setActive(boolean active) {
        this.active = active;
        // 设置高亮颜色
        if (active) {
            this._setColor(Color.ORANGERED);
        } else {// 设置预设颜色
            this._setColor(this.color);
        }
    }

    @Override
    public void disable() {
        StateAdapter.super.disable();
        this.setActive(false);
    }

    @Override
    public void setGroupId(String groupId) {
        NodeGroup.super.groupId(groupId);
    }

    @Override
    public String getGroupId() {
        return NodeGroup.super.groupId();
    }
}
