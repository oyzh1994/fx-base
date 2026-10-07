package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 上级目录 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ParentDirSVGGlyph extends SVGGlyph {

    /**
     * 构造上级目录 SVG 图标控件
     */
    public ParentDirSVGGlyph() {
        super("/fx-svg/parent-dir.svg");
    }

    /**
     * 构造指定尺寸的上级目录 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public ParentDirSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
