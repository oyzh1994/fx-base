package cn.oyzh.fx.gui.svg.glyph.file.x;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Excel 表格文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileXlsSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileXlsSVGGlyph() {
        super("/fx-svg/file/x/file-xls.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileXlsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
