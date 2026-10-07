package cn.oyzh.fx.gui.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * TOML 配置文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTomlSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileTomlSVGGlyph() {
        super("/fx-svg/file/t/file-toml.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileTomlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
