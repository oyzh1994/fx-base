package cn.oyzh.fx.gui.svg.glyph.file.d;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * macOS 磁盘映像文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileDmgSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileDmgSVGGlyph() {
        super("/fx-svg/file/d/file-dmg.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileDmgSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
