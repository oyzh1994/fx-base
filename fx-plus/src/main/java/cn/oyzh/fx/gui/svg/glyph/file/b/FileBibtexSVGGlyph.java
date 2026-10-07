package cn.oyzh.fx.gui.svg.glyph.file.b;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * BibTeX 文献文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileBibtexSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileBibtexSVGGlyph() {
        super("/fx-svg/file/b/file-bibtex.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileBibtexSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
