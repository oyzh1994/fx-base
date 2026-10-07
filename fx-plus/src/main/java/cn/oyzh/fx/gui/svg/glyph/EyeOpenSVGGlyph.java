package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 睁眼 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class EyeOpenSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public EyeOpenSVGGlyph() {
        super("/fx-svg/eye-open.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public EyeOpenSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.7;
    }
}
