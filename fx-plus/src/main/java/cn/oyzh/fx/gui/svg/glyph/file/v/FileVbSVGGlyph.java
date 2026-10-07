package cn.oyzh.fx.gui.svg.glyph.file.v;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Visual Basic 源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileVbSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileVbSVGGlyph() {
        super("/fx-svg/file/v/file-vb.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileVbSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
