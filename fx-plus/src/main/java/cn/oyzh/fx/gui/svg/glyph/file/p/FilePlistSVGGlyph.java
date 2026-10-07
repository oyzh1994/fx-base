package cn.oyzh.fx.gui.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * PLIST 配置文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FilePlistSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FilePlistSVGGlyph() {
        super("/fx-svg/file/p/file-plist.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FilePlistSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
