package cn.oyzh.fx.gui.svg.glyph.file.g;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * GZIP 压缩文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileGzSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileGzSVGGlyph() {
        super("/fx-svg/file/g/file-gzip.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileGzSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
