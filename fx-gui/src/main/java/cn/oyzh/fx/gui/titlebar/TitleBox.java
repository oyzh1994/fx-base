package cn.oyzh.fx.gui.titlebar;

import cn.oyzh.fx.plus.controls.box.FXVBox;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;

public class TitleBox extends FXVBox {

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
        this.initBorder();
        this.initEvents();
    }

    protected void initBorder() {
        BorderStroke stroke = new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, null, new BorderWidths(1));
        Border border = new Border(stroke);
        this.setBorder(border);
    }

    protected void initEvents() {
        this.setOnMouseMoved(event -> {
            double x = event.getX();
            double y = event.getY();
            double w = this.realWidth();
            double h = this.realHeight();
            double windowX = this.window().getX();
            double windowY = this.window().getY();

//            System.out.println("windowX: " + windowX);
//            System.out.println("windowY: " + windowY);
//            System.out.println("w: " + w);
//            System.out.println("h: " + h);
//            System.out.println("x: " + x);
//            System.out.println("y: " + y);
            // 左上
            if (x <= Threshold && y <= Threshold) {
                this.setCursor(Cursor.NW_RESIZE);
                return;
            }
            // 左下
            if (x <= Threshold && Math.abs(h - y) <= Threshold) {
                this.setCursor(Cursor.SW_RESIZE);
                return;
            }
            // 右上
            if (Math.abs(w - x) <= Threshold && y <= Threshold) {
                this.setCursor(Cursor.SW_RESIZE);
                return;
            }
            // 右下
            if (Math.abs(w - x) <= Threshold && Math.abs(h - y) <= Threshold) {
                this.setCursor(Cursor.NW_RESIZE);
                return;
            }
            // 右边
            if (Math.abs(w - x) <= Threshold) {
                this.setCursor(Cursor.W_RESIZE);
                return;
            }
            // 左边
            if (x <= Threshold) {
                this.setCursor(Cursor.W_RESIZE);
                return;
            }
            // 上边
            if (y <= Threshold) {
                this.setCursor(Cursor.S_RESIZE);
                return;
            }
            // 下边
            if ((h - y) <= Threshold) {
                this.setCursor(Cursor.S_RESIZE);
                return;
            }
            // 正常
            this.setCursor(Cursor.DEFAULT);
        });
    }
}
