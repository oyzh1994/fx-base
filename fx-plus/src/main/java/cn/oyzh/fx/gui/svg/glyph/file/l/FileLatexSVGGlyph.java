package cn.oyzh.fx.gui.svg.glyph.file.l;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * LaTeX 文档 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileLatexSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileLatexSVGGlyph() {
        super("/fx-svg/file/l/file-latex.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileLatexSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
