package cn.oyzh.fx.gui.svg.glyph.file.m;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MKV 视频文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileMkvSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileMkvSVGGlyph() {
        super("/fx-svg/file/m/file-mkv.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileMkvSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
