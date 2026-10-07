package cn.oyzh.fx.gui.svg.glyph.file.x;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * XML 数据文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileXmlSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileXmlSVGGlyph() {
        super("/fx-svg/file/x/file-xml.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileXmlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
