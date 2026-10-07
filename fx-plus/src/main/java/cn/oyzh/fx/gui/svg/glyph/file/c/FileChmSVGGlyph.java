package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * CHM 帮助文档 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileChmSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileChmSVGGlyph() {
        super("/fx-svg/file/c/file-chm.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileChmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
