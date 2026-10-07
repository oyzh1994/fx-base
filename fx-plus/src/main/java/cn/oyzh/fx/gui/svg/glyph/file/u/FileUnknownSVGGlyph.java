package cn.oyzh.fx.gui.svg.glyph.file.u;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 未知类型文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileUnknownSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileUnknownSVGGlyph() {
        super("/fx-svg/file/u/file-unknown.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileUnknownSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
