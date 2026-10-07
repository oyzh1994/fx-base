package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * CoffeeScript 源文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCoffeeScriptSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileCoffeeScriptSVGGlyph() {
        super("/fx-svg/file/c/file-coffeescript.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileCoffeeScriptSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
