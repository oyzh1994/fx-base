package cn.oyzh.fx.plus.controls.svg;

import javafx.geometry.Insets;
import javafx.scene.paint.Paint;


/**
 * 缩放svg图像
 *
 * @author oyzh
 * @since 2026/5/29
 */
public class ScalingSVGGlyph extends SVGGlyph {

    public ScalingSVGGlyph(String url) {
        super(url);
    }

    public ScalingSVGGlyph(String url, Paint color) {
        super(url, color);
    }

    public ScalingSVGGlyph(String url, String size) {
        super(url, size);
    }

    public ScalingSVGGlyph(String url, double size) {
        super(url, size);
    }

    @Override
    protected double[] calculateSize(double size) {
        double w, h;
        // 针对部分图标的处理
        if (this.sizeScaling() != 1.0) {
            w = h = size * this.sizeScaling();
        } else {
            w = h = size;
        }
        if (this.widthScaling() != 1.0) {
            w *= this.widthScaling();
        }
        if (this.heightScaling() != 1.0) {
            h *= this.heightScaling();
        }
        if (h != size) {
            Insets insets = new Insets((size - h) / 2, 0, 0, 0);
            this.setPadding(insets);
        }
        return new double[]{w, h};
    }

    /**
     * 当设置标准大小时，缩放此组件大小
     *
     * @return 结果
     */
    public double sizeScaling() {
        return 1.0;
    }

    /**
     * 当设置标准大小时，缩放此组件宽大小
     *
     * @return 结果
     */
    public double widthScaling() {
        return 1.0;
    }

    /**
     * 当设置标准大小时，缩放此组件高大小
     *
     * @return 结果
     */
    public double heightScaling() {
        return 1.0;
    }

}
