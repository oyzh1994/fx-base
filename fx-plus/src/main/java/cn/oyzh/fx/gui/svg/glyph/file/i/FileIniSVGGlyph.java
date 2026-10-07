package cn.oyzh.fx.gui.svg.glyph.file.i;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * INI 配置文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class FileIniSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileIniSVGGlyph() {
        super("/fx-svg/file/i/file-ini.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileIniSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
