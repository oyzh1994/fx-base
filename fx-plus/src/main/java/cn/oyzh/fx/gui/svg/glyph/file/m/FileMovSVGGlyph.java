package cn.oyzh.fx.gui.svg.glyph.file.m;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MOV 视频文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileMovSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileMovSVGGlyph() {
        super("/fx-svg/file/m/file-mov.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileMovSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
