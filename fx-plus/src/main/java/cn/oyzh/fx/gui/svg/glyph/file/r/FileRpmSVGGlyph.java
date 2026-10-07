package cn.oyzh.fx.gui.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * RPM 安装包 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileRpmSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileRpmSVGGlyph() {
        super("/fx-svg/file/r/file-rpm.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileRpmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
