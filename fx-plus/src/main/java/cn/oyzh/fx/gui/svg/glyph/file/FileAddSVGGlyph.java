package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 新增文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-06
 */
public class FileAddSVGGlyph extends SVGGlyph {

    /**
     * 构造新增文件 SVG 图标控件
     */
    public FileAddSVGGlyph() {
        super("/fx-svg/file/file-add.svg");
    }

    /**
     * 构造指定尺寸的新增文件 SVG 图标控件
     *
     * @param size 尺寸
     */
    public FileAddSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
