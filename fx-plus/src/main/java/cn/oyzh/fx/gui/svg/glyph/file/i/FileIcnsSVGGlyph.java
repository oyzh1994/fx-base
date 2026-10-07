package cn.oyzh.fx.gui.svg.glyph.file.i;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * macOS 图标文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileIcnsSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileIcnsSVGGlyph() {
        super("/fx-svg/file/i/file-icns.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileIcnsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
