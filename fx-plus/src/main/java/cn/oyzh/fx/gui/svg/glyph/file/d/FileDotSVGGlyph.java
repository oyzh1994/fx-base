package cn.oyzh.fx.gui.svg.glyph.file.d;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Graphviz DOT 文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileDotSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileDotSVGGlyph() {
        super("/fx-svg/file/d/file-dot.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileDotSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
