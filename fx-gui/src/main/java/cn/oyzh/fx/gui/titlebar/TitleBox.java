package cn.oyzh.fx.gui.titlebar;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.fx.plus.controls.box.FlexVBox;
import cn.oyzh.fx.plus.util.MouseUtil;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import java.util.concurrent.atomic.AtomicReference;

public class TitleBox extends FlexVBox {

    {
        this.init();
    }

    public static byte Threshold = 5;

    public void setTitleBar(TitleBar titleBar) {
        this.setChild(0, titleBar);
    }

    public void setContent(Node content) {
        this.setChild(1, content);
    }

    protected void init() {
        this.setFlexWidth("100%");
        this.setFlexHeight("100%");
        this.initBorder();
        this.initEvents();
    }

    protected void initBorder() {
        BorderStroke stroke = new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, null, new BorderWidths(1));
        Border border = new Border(stroke);
        this.setBorder(border);
    }

    private boolean xChange;
    private boolean yChange;
    private boolean widthResize;
    private boolean heightResize;

    protected void initEvents() {
        this.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            double x = event.getX();
            double y = event.getY();
            double w = this.realWidth();
            double h = this.realHeight();
//            System.out.println("w: " + w);
//            System.out.println("h: " + h);
//            System.out.println("x: " + x);
//            System.out.println("y: " + y);
            // 左上
            if (x <= Threshold && y <= Threshold) {
                this.setCursor(Cursor.NW_RESIZE);
                this.widthResize = this.heightResize = this.xChange = this.yChange = true;
                return;
            }
            // 左下
            if (x <= Threshold && Math.abs(h - y) <= Threshold) {
                this.setCursor(Cursor.SW_RESIZE);
                this.widthResize = this.heightResize = true;
                this.xChange = this.yChange = false;
                return;
            }
            // 右上
            if (Math.abs(w - x) <= Threshold && y <= Threshold) {
                this.setCursor(Cursor.SW_RESIZE);
                this.widthResize = this.heightResize = this.xChange = this.yChange = true;
                return;
            }
            // 右下
            if (Math.abs(w - x) <= Threshold && Math.abs(h - y) <= Threshold) {
                this.setCursor(Cursor.NW_RESIZE);
                this.widthResize = this.heightResize = true;
                this.xChange = this.yChange = false;
                return;
            }
            // 右边
            if (Math.abs(w - x) <= Threshold) {
                this.setCursor(Cursor.W_RESIZE);
                this.widthResize = true;
                this.heightResize = this.xChange = this.yChange = false;
                return;
            }
            // 左边
            if (x <= Threshold) {
                this.setCursor(Cursor.W_RESIZE);
                this.widthResize = this.xChange = true;
                this.heightResize = this.yChange = false;
                return;
            }
            // 上边
            if (y <= Threshold) {
                this.setCursor(Cursor.S_RESIZE);
                this.heightResize = this.yChange = true;
                this.widthResize = this.xChange = false;
                return;
            }
            // 下边
            if ((h - y) <= Threshold) {
                this.setCursor(Cursor.S_RESIZE);
                this.heightResize = true;
                this.widthResize = this.xChange = this.yChange = false;
                return;
            }
            // 正常
            this.widthResize = this.heightResize = this.xChange = this.yChange = false;
            this.setCursor(Cursor.DEFAULT);
        });
        this.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            // 正常
            this.setCursor(Cursor.DEFAULT);
        });
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            // 位置追踪
            if (event.getButton() == MouseButton.PRIMARY) {
                TaskManager.cancelInterval("mouse:resize:track");
                Window window = this.window();
                // 原始位置
                double[] originalPosition = MouseUtil.getMousePosition();
                AtomicReference<Double> originalX = new AtomicReference<>(originalPosition[0]);
                AtomicReference<Double> originalY = new AtomicReference<>(originalPosition[1]);
                double originalW = window.getWidth();
                double originalH = window.getHeight();
                // 追踪位置
                TaskManager.startInterval("mouse:resize:track", () -> {
                    try {
                        double[] position = MouseUtil.getMousePosition();
                        if (this.widthResize) {
                            double mouseX = position[0];
                            double x1 = mouseX - originalX.get();
                            if (x1 != 0) {
                                window.setWidth(originalW + x1);
                            }
                        }
                        if (this.heightResize) {
                            double mouseY = position[1];
                            double y1 = mouseY - originalY.get();
                            if (y1 != 0) {
                                window.setHeight(originalH + y1);
                            }
                        }
                    } finally {

                    }
                }, 1);
            }
        });

        this.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            TaskManager.cancelInterval("mouse:resize:track");
        });
//        this.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
//            TaskManager.cancelInterval("mouse:resize:track");
//        });
    }

}
