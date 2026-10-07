package cn.oyzh.fx.gui.svg.glyph.file.s;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SWF 动画文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileSwfSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileSwfSVGGlyph() {
        super("/fx-svg/file/s/file-swf.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileSwfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
