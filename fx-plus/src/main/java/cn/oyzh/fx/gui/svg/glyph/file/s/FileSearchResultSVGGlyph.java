package cn.oyzh.fx.gui.svg.glyph.file.s;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 搜索结果 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileSearchResultSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileSearchResultSVGGlyph() {
        super("/fx-svg/file/s/file-search-result.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileSearchResultSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
