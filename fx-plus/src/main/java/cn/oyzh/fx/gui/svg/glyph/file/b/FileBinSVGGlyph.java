package cn.oyzh.fx.gui.svg.glyph.file.b;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 二进制文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileBinSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileBinSVGGlyph() {
        super("/fx-svg/file/b/file-bin.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileBinSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
