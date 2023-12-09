package cn.oyzh.fx.plus.controls.svg;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.common.thread.Task;
import cn.oyzh.fx.common.thread.ThreadUtil;
import cn.oyzh.fx.plus.adapter.MouseAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;


/**
 * svg图像
 *
 * @author oyzh
 * @since 2022/5/31
 */
//@Slf4j
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
    @Accessors(fluent = true, chain = false)
    private Cursor cursor = Cursor.HAND;

    {
        this.setSize(16);
        this.setCache(true);
        this.setCacheShape(true);
        this.setPickOnBounds(true);
        this.setPadding(new Insets(0));
        this.setFocusTraversable(false);
        this.setCacheHint(CacheHint.QUALITY);
        this.disabledProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.setBackground(ControlUtil.background(Color.GRAY));
            } else {
                this.setBackground(ControlUtil.background(this.color));
            }
        });
        this.backgroundProperty().addListener((observableValue, background, t1) -> {
            if (t1 != null && t1.getFills() != null && !t1.isEmpty()) {
                SVGPathExt svgPathExt = this.shape();
                svgPathExt.setColor(t1.getFills().getFirst().getFill());
            }
        });
        this.cursorProperty().addListener((observableValue, background, t1) -> {
            SVGPathExt svgPathExt = this.shape();
            svgPathExt.setCursor(t1);
        });
        this.cursor(this.cursor);
    }

    /**
     * 设置初始鼠标
     *
     * @param cursor 鼠标类型
     */
    public void setInitCursor(String cursor) {
        if (cursor != null) {
            this.cursor(Cursor.cursor(cursor));
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
     * 设置鼠标样式
     *
     * @param cursor 鼠标样式
     */
    public void cursor(Cursor cursor) {
        this.cursor = cursor;
        this.setCursor(cursor);
    }

    /**
     * 获取svg内容
     *
     * @return svg内容
     */
    public SVGPathExt shape() {
        SVGPathExt svgPathExt = (SVGPathExt) this.getShape();
        if (svgPathExt == null) {
            svgPathExt = new SVGPathExt();
            this.setShape(svgPathExt);
        }
        return svgPathExt;
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

    public SVGGlyph(@NonNull String url, Number size) {
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
        this.shape().setUrl(url);
        this.shape().setColor(this.color);
    }

    /**
     * 获取svg地址
     *
     * @return svg地址
     */
    public String getUrl() {
        return this.shape().getUrl();
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
    public void setColor(Color color) {
        if (color == null) {
            color = Color.BLACK;
        }
        this.color = color;
        this.setBackground(ControlUtil.background(color));
    }

    /**
     * 设置大小
     *
     * @param size 大小
     */
    public void setSize(Number size) {
        if (size != null && size.doubleValue() > 0) {
            this.setSizeStr(size.toString());
        }
    }

    /**
     * 获取大小
     *
     * @return 大小
     */
    public Number getSize() {
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
        this.startWaiting();
        ExecutorUtil.start(task, 20);
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

    @Override
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }
}
