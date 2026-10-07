package cn.oyzh.fx.gui.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class FileSVGGlyph extends SVGGlyph {

    /**
     * 构造文件 SVG 图标控件
     */
    public FileSVGGlyph() {
        super("/fx-svg/file/file.svg");
    }

    /**
     * 构造指定尺寸的文件 SVG 图标控件
     *
     * @param size 尺寸
     */
    public FileSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
