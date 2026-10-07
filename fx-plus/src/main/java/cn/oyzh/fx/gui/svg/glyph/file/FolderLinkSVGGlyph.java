package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 文件夹链接 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FolderLinkSVGGlyph extends SVGGlyph {

    /**
     * 构造文件夹链接 SVG 图标控件
     */
    public FolderLinkSVGGlyph() {
        super("/fx-svg/file/file-symlink-directory.svg");
    }

    /**
     * 构造指定尺寸的文件夹链接 SVG 图标控件
     *
     * @param size 尺寸
     */
    public FolderLinkSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
