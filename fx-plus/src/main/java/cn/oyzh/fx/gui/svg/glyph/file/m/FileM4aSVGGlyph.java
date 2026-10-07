package cn.oyzh.fx.gui.svg.glyph.file.m;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * M4A 音频文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileM4aSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileM4aSVGGlyph() {
        super("/fx-svg/file/m/file-m4a.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileM4aSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
