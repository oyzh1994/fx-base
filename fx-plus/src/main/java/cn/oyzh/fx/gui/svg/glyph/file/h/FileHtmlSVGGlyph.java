package cn.oyzh.fx.gui.svg.glyph.file.h;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * HTML 网页文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileHtmlSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileHtmlSVGGlyph() {
        super("/fx-svg/file/h/file-html.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileHtmlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
