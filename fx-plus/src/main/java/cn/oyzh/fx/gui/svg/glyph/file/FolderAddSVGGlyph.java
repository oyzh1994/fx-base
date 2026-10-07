package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 新增文件夹 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FolderAddSVGGlyph extends SVGGlyph {

    /**
     * 构造新增文件夹 SVG 图标控件
     */
    public FolderAddSVGGlyph() {
        super("/fx-svg/file/folder-add.svg");
    }

    /**
     * 构造指定尺寸的新增文件夹 SVG 图标控件
     *
     * @param size 尺寸
     */
    public FolderAddSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
