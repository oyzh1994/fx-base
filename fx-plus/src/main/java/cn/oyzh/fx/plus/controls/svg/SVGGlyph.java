package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.mouse.MouseAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXColorUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;


/**
 * svg图像
 *
 * @author oyzh
 * @since 2022/5/31
 */
public class SVGGlyph extends StackPane implements LayoutAdapter, NodeGroup, NodeAdapter, ThemeAdapter, MouseAdapter, TipAdapter, StateAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 默认大小
     */
    public static float DEFAULT_SIZE = 16;

    /**
     * 图标地址
     */
    private String url;

    /**
     * 图标颜色
     */
    private Paint color;

    // /**
    //  * 是否激活态
    //  */
    // protected Boolean active;

    /**
     * 激活时的颜色
     */
    protected Color activeColor = Color.ORANGERED;

    /**
     * 是否等待中
     */
    private Boolean waiting;

    /**
     * 是否开启动画功能
     */
    private boolean enableWaiting;
    // private boolean enableWaiting = true;

    /**
     * 原始svg组件
     */
    private FXSVGPath original;

    /**
     * 是否激活状态
     *
     * @return 结果
     */
    public boolean isActive() {
        return this.activeProperty != null && this.activeProperty().get();
    }

    /**
     * 是否等待状态
     *
     * @return 结果
     */
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
     * 更新内容
     */
    private void updateContent() {
        // 获取图标
        Node child = this.getChild(0);
        // 无内容
        if (child == null) {
            return;
        }
        // loading图标
        if (child instanceof ProgressIndicator) {
            return;
        }
        FXSVGPath svgPath = (FXSVGPath) child;
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
        if (this.isActive()) {// 激活
            svgPath.setFill(this.activeColor);
        } else if (this.color != null) {// 指定颜色
            svgPath.setFill(this.color);
        } else if (this.isEnableTheme()) {// 前景色
            svgPath.setFill(ThemeManager.currentForegroundColor());
        }
    }

    /**
     * 开始动画
     */
    public void startWaiting() {
        if (this.isWaiting()) {
            return;
        }
        try {
            this.setWaiting(true);
            // 动画
            ProgressIndicator progress = new ProgressIndicator();
            progress.setFocusTraversable(false);
            Color color = ThemeManager.currentForegroundColor();
            String colorHex = FXColorUtil.getColorHex(color);
            progress.setStyle("-fx-progress-color: " + colorHex);
            // 设置loading图标
            this.setChild(progress);
        } catch (Exception ex) {
            ex.printStackTrace();
            this.setWaiting(false);
        }
    }

    /**
     * 结束动画
     */
    public void stopWaiting() {
        this.setWaiting(false);
        // 恢复原始图标，并更新内容
        this.setChild(this.original);
        this.updateContent();
        this.waiting = null;
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

    public SVGGlyph() {
    }

    public SVGGlyph(String url) {
        this(url, FontManager.currentFontSize());
    }

    public SVGGlyph(String url, Paint color) {
        this(url, FontManager.currentFontSize());
        this.setColor(color);
    }

    public SVGGlyph(String url, String size) {
        this();
        this.setUrl(url);
        this.setSizeStr(size);
    }

    public SVGGlyph(String url, double size) {
        this();
        this.setUrl(url);
        this.setSize(size);
    }

    /**
     * 设置svg地址
     *
     * @param url svg地址
     */
    public void setUrl(String url) {
        this.url = url;
        // this.url = url.intern();
        // 创建图标
        this.original = SVGManager.load(this.url);
        if (this.original == null) {
            throw new RuntimeException("SVG path " + this.url + " is not found");
        }
        // 设置光标
        this.original.setCursor(this.getCursor());
        // 动态绑定缩放比例
        this.original.scaleXProperty().bind(this.widthProperty().divide(this.original.getBoundsInLocal().getWidth()));
        this.original.scaleYProperty().bind(this.heightProperty().divide(this.original.getBoundsInLocal().getHeight()));
        // 设置节点
        this.setChild(this.original);
        // 更新内容
        this.updateContent();
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
     * 设置大小
     *
     * @param width  宽
     * @param height 高
     */
    public void setSize(double width, double height) {
        if (width > 0 && height > 0) {
            this.setSizeStr(width + "," + height);
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
        return NodeUtil.getWidth(this) + "," + NodeUtil.getHeight(this);
        // return ControlUtil.boundedWidth(this) + "," + ControlUtil.boundedHeight(this);
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
                    w = Double.parseDouble(strArr[0].trim());
                    h = Double.parseDouble(strArr[1].trim());
                } else {
                    double size1 = Double.parseDouble(size);
                    // 针对部分图标的处理
                    if (this.sizeScaling() != 1.0) {
                        w = h = size1 * this.sizeScaling();
                    } else {
                        w = h = size1;
                    }
                    if (this.widthScaling() != 1.0) {
                        w *= this.widthScaling();
                    }
                    if (this.heightScaling() != 1.0) {
                        h *= this.heightScaling();
                    }
                }
                this.setMaxSize(w, h);
                this.setMinSize(w, h);
                // this.setPrefSize(w, h);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void initNode() {
        this.setCache(false);
        this.setSize(DEFAULT_SIZE);
        this.setPickOnBounds(true);
        this.setCursor(Cursor.HAND);
        this.setPadding(Insets.EMPTY);
        this.setFocusTraversable(false);
        this.cursorProperty().addListener((observable) -> this.updateContent());
        this.disableProperty().addListener((observable) -> this.updateContent());
        this.disabledProperty().addListener((observable) -> this.updateContent());
        NodeAdapter.super.initNode();
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        if (style != null && this.isEnableTheme() && !this.isActive()) {
            this.updateContent();
        }
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
        this.activeProperty().set(active);
    }

    @Override
    public void disable() {
        StateAdapter.super.disable();
        this.setActive(false);
    }

    @Override
    public SVGGlyph clone() {
        SVGGlyph glyph = new SVGGlyph(this.url, this.color);
        glyph.setSizeStr(this.getSizeStr());
        return glyph;
    }

    public void disableWaiting() {
        this.enableWaiting = false;
    }

    public String getUrl() {
        return url;
    }

    public Paint getColor() {
        return color;
    }

    public Color getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(Color activeColor) {
        this.activeColor = activeColor;
        this.setActive(activeColor != null);
    }

    public Boolean getWaiting() {
        return waiting;
    }

    public void setWaiting(Boolean waiting) {
        this.waiting = waiting;
    }

    public boolean isEnableWaiting() {
        return enableWaiting;
    }

    public void setEnableWaiting(boolean enableWaiting) {
        this.enableWaiting = enableWaiting;
    }

    public FXSVGPath getOriginal() {
        return original;
    }

    public void setOriginal(FXSVGPath original) {
        this.original = original;
    }

    private BooleanProperty activeProperty;

    public BooleanProperty activeProperty() {
        if (this.activeProperty == null) {
            this.activeProperty = new SimpleBooleanProperty(false);
            this.activeProperty.addListener((observable) -> {
                this.updateContent();
            });
        }
        return activeProperty;
    }

    /**
     * 当设置标准大小时，缩放此组件大小
     *
     * @return 结果
     */
    public double sizeScaling() {
        return 1.0;
    }

    /**
     * 当设置标准大小时，缩放此组件宽大小
     *
     * @return 结果
     */
    public double widthScaling() {
        return 1.0;
    }

    /**
     * 当设置标准大小时，缩放此组件高大小
     *
     * @return 结果
     */
    public double heightScaling() {
        return 1.0;
    }

}
