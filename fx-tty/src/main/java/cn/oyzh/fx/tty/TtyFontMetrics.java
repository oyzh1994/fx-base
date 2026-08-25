package cn.oyzh.fx.tty;

import cn.oyzh.common.log.JulLog;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class TtyFontMetrics {

    public static TtyFontMetrics create(Font font, String str) {
        Text text = new Text(str);
        text.setFont(font);
        text.applyCss();//TODO???
        double width = text.getLayoutBounds().getWidth();
        double height = text.getLayoutBounds().getHeight();
        double descent = text.getLayoutBounds().getHeight() - text.getBaselineOffset();
        TtyFontMetrics metrics = new TtyFontMetrics(width, height, descent);
        if (JulLog.isTraceEnabled()) {
            JulLog.trace("Created metrics: {} for {}", metrics, font);
        }
        return metrics;
    }

    private final double descent;

    private final double width;

    private final double height;

    private TtyFontMetrics(double width, double height, double descent) {
        this.descent = descent;
        this.width = width;
        this.height = height;
    }

    public double getDescent() {
        return descent;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "{" + "descent=" + descent + ", width=" + width + ", height=" + height + '}';
    }
}
