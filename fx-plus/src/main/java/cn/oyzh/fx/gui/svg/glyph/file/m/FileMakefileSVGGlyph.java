package cn.oyzh.fx.gui.svg.glyph.file.m;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Makefile 构建文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileMakefileSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileMakefileSVGGlyph() {
        super("/fx-svg/file/m/file-makefile.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileMakefileSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
