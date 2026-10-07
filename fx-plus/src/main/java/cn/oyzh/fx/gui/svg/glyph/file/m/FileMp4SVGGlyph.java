package cn.oyzh.fx.gui.svg.glyph.file.m;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MP4 视频文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-06
 */
public class FileMp4SVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileMp4SVGGlyph() {
        super("/fx-svg/file/m/file-mp4.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileMp4SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
