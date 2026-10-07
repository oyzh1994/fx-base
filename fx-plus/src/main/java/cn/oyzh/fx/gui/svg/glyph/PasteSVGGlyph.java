package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 粘贴 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class PasteSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造粘贴 SVG 图标控件
     */
    public PasteSVGGlyph() {
        super("/fx-svg/file-paste.svg");
    }

    /**
     * 构造指定尺寸的粘贴 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public PasteSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double widthScaling() {
        return 0.875;
    }
}
