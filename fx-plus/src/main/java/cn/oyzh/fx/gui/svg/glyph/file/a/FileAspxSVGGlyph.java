package cn.oyzh.fx.gui.svg.glyph.file.a;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * ASPX 文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileAspxSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileAspxSVGGlyph() {
        super("/fx-svg/file/a/file-aspx.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileAspxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
