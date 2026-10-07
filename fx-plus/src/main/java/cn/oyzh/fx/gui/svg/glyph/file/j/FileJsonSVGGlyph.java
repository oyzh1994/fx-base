package cn.oyzh.fx.gui.svg.glyph.file.j;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * JSON 数据文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileJsonSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileJsonSVGGlyph() {
        super("/fx-svg/file/j/file-json.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileJsonSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
