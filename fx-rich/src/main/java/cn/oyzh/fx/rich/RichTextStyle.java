package cn.oyzh.fx.rich;

import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2023/10/8
 */
@Deprecated
public class RichTextStyle {

    private int start;

    private int end;

    private String style;

    private Color color;

    public RichTextStyle(int start, int end, Color color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }

    public RichTextStyle(int start, int end, String style) {
        this.start = start;
        this.end = end;
        this.style = style;
    }

    public int start() {
        return start;
    }

    public void start(int start) {
        this.start = start;
    }

    public int end() {
        return end;
    }

    public void end(int end) {
        this.end = end;
    }

    public String style() {
        return style;
    }

    public void style(String style) {
        this.style = style;
    }

    public Color color() {
        return color;
    }

    public void color(Color color) {
        this.color = color;
    }
}
