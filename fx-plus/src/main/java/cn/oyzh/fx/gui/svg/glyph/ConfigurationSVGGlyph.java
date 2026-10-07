package cn.oyzh.fx.gui.svg.glyph;


import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 配置 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ConfigurationSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public ConfigurationSVGGlyph() {
        super("/fx-svg/configuration.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ConfigurationSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
