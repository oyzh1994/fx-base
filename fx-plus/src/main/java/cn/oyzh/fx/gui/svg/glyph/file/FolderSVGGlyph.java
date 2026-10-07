package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 文件夹 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class FolderSVGGlyph extends SVGGlyph {

    /**
     * 构造文件夹 SVG 图标控件
     */
    public FolderSVGGlyph() {
        super("/fx-svg/file/folder.svg");
    }

    /**
     * 构造指定尺寸的文件夹 SVG 图标控件
     *
     * @param size 尺寸
     */
    public FolderSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
