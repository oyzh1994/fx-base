package cn.oyzh.fx.gui.svg.glyph.file.z;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * ZIP 压缩文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileZipSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileZipSVGGlyph() {
        super("/fx-svg/file/z/file-zip.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileZipSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
