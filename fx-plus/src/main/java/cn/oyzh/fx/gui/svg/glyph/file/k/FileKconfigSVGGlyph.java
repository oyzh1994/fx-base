package cn.oyzh.fx.gui.svg.glyph.file.k;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Kconfig 配置文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileKconfigSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileKconfigSVGGlyph() {
        super("/fx-svg/file/k/file-kconfig.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileKconfigSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
