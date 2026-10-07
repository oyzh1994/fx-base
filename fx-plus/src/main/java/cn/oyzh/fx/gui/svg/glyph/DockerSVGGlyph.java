package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Docker SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class DockerSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public DockerSVGGlyph() {
        super("/fx-svg/docker.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public DockerSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
