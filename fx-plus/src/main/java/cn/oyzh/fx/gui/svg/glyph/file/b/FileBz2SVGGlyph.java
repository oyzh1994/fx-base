package cn.oyzh.fx.gui.svg.glyph.file.b;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * BZ2 压缩文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileBz2SVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileBz2SVGGlyph() {
        super("/fx-svg/file/b/file-bz2.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileBz2SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
