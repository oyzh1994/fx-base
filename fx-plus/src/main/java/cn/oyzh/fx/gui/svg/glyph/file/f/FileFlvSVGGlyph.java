package cn.oyzh.fx.gui.svg.glyph.file.f;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * FLV 视频文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileFlvSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileFlvSVGGlyph() {
        super("/fx-svg/file/f/file-flv.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileFlvSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
