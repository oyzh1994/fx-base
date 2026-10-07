package cn.oyzh.fx.gui.svg.glyph.file.y;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * YAML 配置文件 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FileYmlSVGGlyph extends SVGGlyph {

    /**
     * 构造器，加载默认尺寸的图标。
     */
    public FileYmlSVGGlyph() {
        super("/fx-svg/file/y/file-yml.svg");
    }

    /**
     * 构造器，加载指定尺寸的图标。
     *
     * @param size 图标尺寸
     */
    public FileYmlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
