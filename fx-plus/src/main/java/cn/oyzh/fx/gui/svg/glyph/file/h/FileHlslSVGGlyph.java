package cn.oyzh.fx.gui.svg.glyph.file.h;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * HLSL 着色器源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileHlslSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileHlslSVGGlyph() {
        super("/fx-svg/file/h/file-hlsl.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileHlslSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
