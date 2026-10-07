package cn.oyzh.fx.tty;

import cn.oyzh.common.log.JulLog;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


/**
 * 终端字体度量信息，用于计算指定字体下文本的宽高与基线下降值。
 *
 * @author oyzh
 * @since 2026-07-06
 */
public class TtyFontMetrics {

    /**
     * 根据字体与文本创建字体度量。
     *
     * @param font 字体
     * @param str  用于测量的文本
     * @return 字体度量
     */
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

    /** 基线下降值 */
    private final double descent;

    /** 文本宽度 */
    private final double width;

    /** 文本高度 */
    private final double height;

    /**
     * 构造字体度量。
     *
     * @param width   文本宽度
     * @param height  文本高度
     * @param descent 基线下降值
     */
    private TtyFontMetrics(double width, double height, double descent) {
        this.descent = descent;
        this.width = width;
        this.height = height;
    }

    /**
     * 获取基线下降值。
     *
     * @return 基线下降值
     */
    public double getDescent() {
        return descent;
    }

    /**
     * 获取文本宽度。
     *
     * @return 文本宽度
     */
    public double getWidth() {
        return width;
    }

    /**
     * 获取文本高度。
     *
     * @return 文本高度
     */
    public double getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "{" + "descent=" + descent + ", width=" + width + ", height=" + height + '}';
    }
}
