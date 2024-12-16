package cn.oyzh.fx.gui.titlebar;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.util.MouseUtil;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TitleBar extends FlexHBox {

    {
        this.realHeight(30);
        this.setMaxHeight(30);
        this.setFlexWidth("100%");
    }

    /**
     * 原始x
     */
    private AtomicReference<Double> originalX;

    /**
     * 原始y
     */
    private AtomicReference<Double> originalY;

    protected void setOriginalX(double x) {
        if (this.originalX == null) {
            this.originalX = new AtomicReference<>();
        }
        this.originalX.set(x);
    }

    protected Double getOriginalX() {
        return this.originalX == null ? null : this.originalX.get();
    }

    protected void setOriginalY(double y) {
        if (this.originalY == null) {
            this.originalY = new AtomicReference<>();
        }
        this.originalY.set(y);
    }

    protected Double getOriginalY() {
        return this.originalY == null ? null : this.originalY.get();
    }

    public TitleBar(TitleBarConfig config) {
        if (config.getShowType() == 1) {
            List<Node> actions = config.getActions();
            if (actions == null) {
                actions = new ArrayList<>();
            }
            if (config.isShowMinimum()) {
                actions.add(new SVGGlyph("/fx-svg/titlebar/minimum.svg"));
            }
            if (config.isShowMaximum()) {
                actions.add(new SVGGlyph("/fx-svg/titlebar/maximum.svg"));
            }
            if (config.isShowClose()) {
                actions.add(new SVGGlyph("/fx-svg/titlebar/close.svg"));
            }
            this.getChildren().addAll(actions);
        }
        // 鼠标按下事件
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // 记录位置
                if (event.getClickCount() == 1) {
                    if (this.checkNotInvalid()) {
                        this.doRecordLocation();
                        event.consume();
                    }
                } else if (event.getClickCount() == 2) {// 最大化
                    Stage stage = this.stage();
                    if (stage != null) {
                        this.maximum(!stage.isMaximized());
                        event.consume();
                    }
                }
            }
        });
        // 鼠标拖动事件
        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // 更新位置
                if (this.checkNotInvalid()) {
                    this.doUpdateLocation();
                    event.consume();
                }
            }
        });
        // 鼠标释放事件
        this.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // 检查状态
                if (this.checkNotInvalid()) {
                    this.doClearLocation();
                    event.consume();
                }
            }
        });
    }

    protected void doRecordLocation() {
        // 记录原始位置
        double[] originalPosition = MouseUtil.getMousePosition();
        this.setOriginalX(originalPosition[0]);
        this.setOriginalY(originalPosition[1]);
    }

    protected void doClearLocation() {
        if (this.originalX != null) {
            this.originalX.set(null);
        }
        if (this.originalY != null) {
            this.originalY.set(null);
        }
    }

    private void doUpdateLocation() {
        try {
            if (this.originalX.get() == null || this.originalY.get() == null) {
                JulLog.warn("originalX or originalY is null!");
                return;
            }
            Window window = this.window();
            double[] position = MouseUtil.getMousePosition();
            double mouseX = position[0];
            double mouseY = position[1];
            double nodeX = window.getX();
            double nodeY = window.getY();
            // 计算x差值，不等于0时则更新组件x位置，并更新x值
            double x1 = mouseX - this.originalX.get();
            if (x1 != 0) {
                window.setX(nodeX + x1);
                this.originalX.set(mouseX);
            }
            // 计算y差值，不等于0时则更新组件x位置，并更新y值
            double y1 = mouseY - this.originalY.get();
            if (y1 != 0) {
                window.setY(nodeY + y1);
                this.originalY.set(mouseY);
            }
            JulLog.debug("doUpdateLocation mouseX:{} mouseY:{} nodeX:{} nodeY:{}", mouseX, mouseY, nodeX, nodeY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected boolean checkNotInvalid() {
        Stage stage = this.stage();
        // 最大化、最小化、全屏情况下不执行操作
        return stage != null && !stage.isMaximized() && !stage.isIconified() && !stage.isFullScreen();
    }

    public void close() {
        Window window = this.window();
        if (window != null) {
            window.hide();
        } else {
            JulLog.warn("window is null!");
        }
    }

    public void maximum(boolean maximum) {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setMaximized(maximum);
            // 最大化设置高度不超过显示边界
            if (maximum) {
                Rectangle2D rectangle2D = Screen.getPrimary().getVisualBounds();
                stage.setHeight(rectangle2D.getHeight());
            }
        } else {
            JulLog.warn("window is null!");
        }
    }

    public void minimum(boolean minimum) {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setIconified(minimum);
        } else {
            JulLog.warn("window is null!");
        }
    }
}
