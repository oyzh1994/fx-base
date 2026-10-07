package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 压缩文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileCompressSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileCompressSVGGlyph() {
        super("/fx-svg/file/c/file-compress.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileCompressSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
