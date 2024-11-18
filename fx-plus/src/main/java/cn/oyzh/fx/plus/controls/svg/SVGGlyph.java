package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.MouseAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
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

    {
        NodeManager.init(this);
    }

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
    private Boolean active;

    /**
     * 是否等待中
     */
    @Getter
    @Setter
    private Boolean waiting;

    /**
     * 是否开启动画功能
     */
    @Setter
    @Getter
    private Boolean enableWaiting = true;

    public boolean isActive() {
        return this.active != null && this.active;
    }

    public boolean isWaiting() {
        return this.waiting != null && this.waiting;
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
        // 无内容
        if (svgPath == null) {
            return;
        }
        // loading图标
        if (SVGManager.isLoadingSvgPath(svgPath)) {
            return;
        }
        // 更新鼠标
        if (this.getCursor() != svgPath.getCursor()) {
            svgPath.setCursor(this.getCursor());
        }
        // 更新透明度
        if (this.isDisabled() || this.isDisabled()) {
            this.setOpacity(0.4);
        } else {
            this.setOpacity(1.0);
        }
        // 更新颜色
        if (this.isActive()) {
            this.setBackground(ControlUtil.background(Color.ORANGERED));
        } else if (this.isEnableTheme()) {
            this.setBackground(ControlUtil.background(ThemeManager.currentForegroundColor()));
        } else if (this.color != null) {
            this.setBackground(ControlUtil.background(this.color));
        }
    }

    /**
     * 原始svg组件
     */
    private SVGPath original;

    /**
     * 等待动画
     */
    private RotateTransition waitingTransition;

    /**
     * 开始动画
     */
    public void startWaiting() {
        if (this.isWaiting()) {
            return;
        }
        this.setWaiting(true);
        // 记录原始图标
        this.original = (SVGPath) this.getShape();
        // 设置loading图标
        this.setShape(SVGManager.getLoadingSvgPath());
        // 初始化动画
        this.waitingTransition = AnimationUtil.rotate(this);
        // 播放动画
        this.waitingTransition.play();
    }

    /**
     * 结束动画
     */
    public void stopWaiting() {
        // 结束动画
        if (this.waitingTransition != null) {
            FXUtil.runWait(this.waitingTransition::stop);
            this.waitingTransition = null;
        }
        this.setShape(this.original);
        this.setRotate(0);
        this.setWaiting(false);
        this.original = null;
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

    public SVGGlyph(@NonNull String url, @NonNull Paint color) {
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
        this.url = url.intern();
        // 创建图标
        SVGPath svgPath = SVGManager.load(this.url);
        if (svgPath == null) {
            throw new RuntimeException("SVG path " + this.url + " is not found");
        }
        svgPath.setCursor(this.getCursor());
        this.setShape(svgPath);
    }

    /**
     * 设置颜色
     *
     * @param color 颜色
     */
    public void setColor(String color) {
        try {
            if (StringUtil.isNotBlank(color)) {
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
        this.updateContent();
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
        if (StringUtil.isNotBlank(size)) {
            try {
                size = size.trim();
                double w, h;
                if (size.contains(",")) {
                    String[] strArr = size.split(",");
                    w = Double.parseDouble(strArr[0]);
                    h = Double.parseDouble(strArr[1]);
                } else {
                    w = h = Double.parseDouble(size);
                }
                this.setMaxSize(w, h);
                this.setPrefSize(w, h);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        if (style != null && this.isEnableTheme() && !this.isActive()) {
            this.updateContent();
        }
    }

    @Override
    public void setCursor(Cursor value) {
        super.setCursor(value);
        this.updateContent();
    }

    @Override
    public void setDisable(boolean value) {
        super.setDisable(value);
        this.updateContent();
    }

    @Override
    public void setDisabled(boolean value) {
        super.setDisabled(value);
        this.updateContent();
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
        this.updateContent();
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

    @Override
    public SVGGlyph clone() {
        SVGGlyph glyph = new SVGGlyph(this.url, this.color);
        glyph.setSizeStr(this.getSizeStr());
        return glyph;
    }
}
