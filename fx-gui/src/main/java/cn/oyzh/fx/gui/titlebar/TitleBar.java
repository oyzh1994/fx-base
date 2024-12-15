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
        this.setFlexWidth("100%");
    }

    private boolean maximum;

    private final AtomicReference<Double> originalX = new AtomicReference<>(0.d);

    private final AtomicReference<Double> originalY = new AtomicReference<>(0.d);

    private final AtomicReference<Boolean> positionUpdated = new AtomicReference<>(false);

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
//        this.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
//            // 最大化监听
//            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
//                this.maximum(!this.maximum);
//                event.consume();
//                System.out.println("-------3");
//            }
//        });
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            // 位置追踪
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                // 清除任务
                TaskManager.cancelInterval("mouse:position:track");
                // 记录原始位置
                double[] originalPosition = MouseUtil.getMousePosition();
                this.originalX.set(originalPosition[0]);
                this.originalY.set(originalPosition[1]);
                // 追踪位置并更新
                TaskManager.startInterval("mouse:position:track", ()->{
                    this.positionUpdated.set(true);
                    this.doUpdateLocation();
                }, 5, 150);
                System.out.println("-------1");
                event.consume();
            } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                // 清除任务
                TaskManager.cancelInterval("mouse:position:track");
                this.maximum(!this.maximum);
                event.consume();
                System.out.println("-------4");
            }
        });
        this.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            TaskManager.cancelInterval("mouse:position:track");
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                // 清除任务
                if (this.positionUpdated.get()) {
                    // 更新位置
                    this.doUpdateLocation();
                    // 更新位置信息
                    double[] originalPosition = MouseUtil.getMousePosition();
                    this.originalX.set(originalPosition[0]);
                    this.originalY.set(originalPosition[1]);
                    System.out.println("-------2");
                    this.positionUpdated.set(false);
                }
                event.consume();
            }
        });
//        this.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
//            // 清除任务
//            TaskManager.cancelInterval("mouse:position:track");
//            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
//                // 更新位置
//                this.doUpdateLocation();
//                // 更新位置信息
//                double[] originalPosition = MouseUtil.getMousePosition();
//                this.originalX.set(originalPosition[0]);
//                this.originalY.set(originalPosition[1]);
//            }
//            event.consume();
//        });
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
//        // 更新位置信息
//        double[] originalPosition = MouseUtil.getMousePosition();
//        this.originalX.set(originalPosition[0]);
//        this.originalY.set(originalPosition[1]);
    }

    public void minimum(boolean minimum) {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setIconified(minimum);
        } else {
            JulLog.warn("window is null!");
        }
//        // 更新位置信息
//        double[] originalPosition = MouseUtil.getMousePosition();
//        this.originalX.set(originalPosition[0]);
//        this.originalY.set(originalPosition[1]);
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
}
