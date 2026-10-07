package cn.oyzh.fx.gui.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Perl 源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FilePerlSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FilePerlSVGGlyph() {
        super("/fx-svg/file/p/file-perl.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FilePerlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
