package cn.oyzh.fx.gui.svg.glyph.file.v;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * VBScript 脚本文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileVbsSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileVbsSVGGlyph() {
        super("/fx-svg/file/v/file-vbs.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileVbsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
