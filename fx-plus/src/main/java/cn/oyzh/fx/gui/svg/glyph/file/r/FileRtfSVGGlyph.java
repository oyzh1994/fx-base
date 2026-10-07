package cn.oyzh.fx.gui.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * RTF 文档 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileRtfSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileRtfSVGGlyph() {
        super("/fx-svg/file/r/file-rtf.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileRtfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
