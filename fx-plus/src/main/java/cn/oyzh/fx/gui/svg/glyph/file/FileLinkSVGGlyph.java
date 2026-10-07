package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 文件链接 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileLinkSVGGlyph extends SVGGlyph {

    /**
     * 构造文件链接 SVG 图标控件
     */
    public FileLinkSVGGlyph() {
        super("/fx-svg/file/file-symlink-file.svg");
    }

    /**
     * 构造指定尺寸的文件链接 SVG 图标控件
     *
     * @param size 尺寸
     */
    public FileLinkSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
