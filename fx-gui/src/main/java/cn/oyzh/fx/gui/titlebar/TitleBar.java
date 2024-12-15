package cn.oyzh.fx.gui.titlebar;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.fx.plus.controls.box.FlexHBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.util.MouseUtil;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class TitleBar extends FlexHBox {

    {
        this.realHeight(30);
        this.setMaxHeight(30);
    }

    private boolean maximum;

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
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            // 最大化监听
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                this.maximum(!this.maximum);
            }
        });
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            // 位置追踪
            if (event.getButton() == MouseButton.PRIMARY) {
                TaskManager.cancelInterval("mouse:position:track");
                Window window = this.window();
                // 原始位置
                double[] originalPosition = MouseUtil.getMousePosition();
                AtomicReference<Double> originalX = new AtomicReference<>(originalPosition[0]);
                AtomicReference<Double> originalY = new AtomicReference<>(originalPosition[1]);
                // 追踪位置
                TaskManager.startInterval("mouse:position:track", () -> {
                    try {
                        double[] position = MouseUtil.getMousePosition();
                        double mouseX = position[0];
                        double mouseY = position[1];
                        double nodeX = window.getX();
                        double nodeY = window.getY();
                        // 计算x差值，不等于0时则更新组件x位置，并更新x值
                        double x1 = mouseX - originalX.get();
                        if (x1 != 0) {
                            window.setX(nodeX + x1);
                            originalX.set(mouseX);
                        }
                        // 计算y差值，不等于0时则更新组件x位置，并更新y值
                        double y1 = mouseY - originalY.get();
                        if (y1 != 0) {
                            window.setY(nodeY + y1);
                            originalY.set(mouseY);
                        }
                        System.out.println("mouseX:" + mouseX);
                        System.out.println("mouseY:" + mouseY);
                        System.out.println("nodeX:" + nodeX);
                        System.out.println("nodeY:" + nodeY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 1);
            }
        });
        this.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            TaskManager.cancelInterval("mouse:position:track");
        });
        this.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            TaskManager.cancelInterval("mouse:position:track");
        });
    }

    private Stage stage() {
        Window window = this.window();
        if (window instanceof Stage stage) {
            return stage;
        }
        return null;
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
        this.maximum = maximum;
        Stage stage = this.stage();
        if (stage != null) {
            stage.setMaximized(maximum);
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
