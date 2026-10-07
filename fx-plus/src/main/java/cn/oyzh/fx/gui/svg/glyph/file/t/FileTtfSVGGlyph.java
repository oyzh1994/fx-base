package cn.oyzh.fx.gui.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * TTF 字体文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTtfSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileTtfSVGGlyph() {
        super("/fx-svg/file/t/file-ttf.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileTtfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
