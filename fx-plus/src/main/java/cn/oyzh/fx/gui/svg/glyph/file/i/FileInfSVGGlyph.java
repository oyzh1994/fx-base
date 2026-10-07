package cn.oyzh.fx.gui.svg.glyph.file.i;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 安装信息文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileInfSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileInfSVGGlyph() {
        super("/fx-svg/file/i/file-inf.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileInfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
