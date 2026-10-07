package cn.oyzh.fx.gui.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 配置文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileConfigSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileConfigSVGGlyph() {
        super("/fx-svg/file/c/file-config.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileConfigSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
