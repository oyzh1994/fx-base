package cn.oyzh.fx.plus.svg;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.common.thread.Task;
import cn.oyzh.fx.common.thread.ThreadUtil;
import cn.oyzh.fx.plus.adapter.MouseAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * svg图像
 *
 * @author oyzh
 * @since 2022/5/31
 */
@Slf4j
public class SVGGlyph extends Region implements ThemeAdapter, MouseAdapter, TipAdapter, StateAdapter {

    /**
     * 颜色
     */
    @Getter
    private Color color;

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

    /**
     * svg鼠标样式
     */
    @Getter
    private Cursor svgCursor = Cursor.HAND;

    /**
     * 加载器
     */
    @Setter
    @Getter
    private SVGLoader loader = SVGLoader.INSTANCE;

    {
        this.setSize(16);
        this.setCache(true);
        this.setCacheShape(true);
        this.setPickOnBounds(true);
        this.setCursor(this.svgCursor);
        this.setPadding(new Insets(0));
        this.setFocusTraversable(false);
        this.setCacheHint(CacheHint.QUALITY);
        this.disabledProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.disable();
            } else {
                this.enable();
            }
        });
    }

    /**
     * 设置初始鼠标
     *
     * @param cursor 鼠标类型
     */
    public void setInitCursor(String cursor) {
        if (cursor != null) {
            this.setSvgCursor(Cursor.cursor(cursor));
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
     * 设置svg鼠标
     *
     * @param svgCursor svg鼠标
     */
    public void setSvgCursor(Cursor svgCursor) {
        this.svgCursor = svgCursor;
        if (!this.isDisable()) {
            this.setCursor(svgCursor);
            SVGPath svgPath = this.svgPath(false);
            if (svgPath != null) {
                svgPath.setCursor(svgCursor);
            }
        }
    }

    /**
     * 初始化svg内容
     *
     * @param url 地址
     */
    private void initContent(String url) {
        if (this.loader != null && url != null) {
            String content = this.loader.load(url);
            if (content != null) {
                this.svgPath(true).setContent(content);
                this.setCache(true);
                this.setCacheShape(true);
            }
        }
    }

    /**
     * 获取svg内容
     *
     * @return svg内容
     */
    public SVGPath svgPath(boolean initIfNull) {
        if (this.getShape() == null && initIfNull) {
            this.setShape(new SVGPath());
        }
        return (SVGPath) this.getShape();
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
    public void setTipText(String tipTitle) {
        TipAdapter.super._setTipText(tipTitle);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super._getTipText();
    }

    public SVGGlyph() {
        this.setColor(this.isDisabled() ? Color.GREY : Color.BLACK);
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
    public void setUrl(String url) {
        this.setProp("_url", url.intern());
        this.initContent(url);
    }

    /**
     * 获取svg地址
     *
     * @return svg地址
     */
    public String getUrl() {
        return this.getProp("_url");
    }

    /**
     * 设置颜色
     *
     * @param colorStr 颜色
     */
    public void setColor(String colorStr) {
        try {
            if (StrUtil.isNotBlank(colorStr)) {
                Color color = Color.valueOf(colorStr.trim());
                this.setColor(color);
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
    public void setColor(Color color) {
        if (color == null) {
            color = Color.BLACK;
        }
        if (this.color != color) {
            this.color = color;
            this.svgPath(true).setFill(color);
            this.setBackground(ControlUtil.backgroundOfColor(color));
        }
    }

    @Override
    public void disable() {
        if (!this.isDisabled()) {
            this.setDisable(true);
        }
        SVGPath svgPath = this.svgPath(false);
        if (svgPath != null) {
            this.setCursor(Cursor.NONE);
            svgPath.setFill(Color.GRAY);
            this.setBackground(ControlUtil.backgroundOfColor(Color.GRAY));
        }
    }

    @Override
    public void enable() {
        if (this.isDisabled()) {
            this.setDisable(false);
        }
        SVGPath svgPath = this.svgPath(false);
        if (svgPath != null) {
            this.setCursor(this.svgCursor);
            svgPath.setFill(this.color);
            this.setBackground(ControlUtil.backgroundOfColor(this.color));
        }
    }

    /**
     * 设置svg大小
     *
     * @param size 大小
     */
    public void setSize(@NonNull Number size) {
        this.setSizeStr(size.toString());
    }

    /**
     * 获取svg大小
     *
     * @return svg大小
     */
    public Number getSize() {
        return this.getWidth();
    }

    /**
     * 获取svg大小
     *
     * @return svg大小
     */
    public String getSizeStr() {
        return null;
    }

    /**
     * 设置svg大小
     *
     * @param size 大小
     */
    public void setSizeStr(String size) {
        if (StrUtil.isBlank(size)) {
            return;
        }
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
            this.setMinSize(w, h);
            this.setMaxSize(w, h);
            this.setPrefSize(w, h);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 开始等待动画
     *
     * @param handler 处理业务
     */
    public void startWaiting(@NonNull Runnable handler) {
        this.startWaiting();
        ExecutorUtil.start(handler, 20);
    }

    /**
     * 开始等待动画
     *
     * @param task 处理业务
     */
    public void startWaiting(@NonNull Task task) {
        ExecutorUtil.submit(this::startWaiting);
        ThreadUtil.startVirtual(task);
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
    public void onDarkTheme() {
        this.setColor(Color.WHITE);
    }

    @Override
    public void onLightTheme() {
        this.setColor(Color.BLACK);
    }
}
