package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 打包 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class PackageSVGGlyph extends SVGGlyph {

    /**
     * 构造打包 SVG 图标控件
     */
    public PackageSVGGlyph() {
        super("/fx-svg/package.svg");
    }

    /**
     * 构造指定尺寸的打包 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public PackageSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
