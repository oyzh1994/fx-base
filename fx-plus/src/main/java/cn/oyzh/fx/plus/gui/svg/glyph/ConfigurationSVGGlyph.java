package cn.oyzh.fx.plus.gui.svg.glyph;


import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ConfigurationSVGGlyph extends SVGGlyph {

    public ConfigurationSVGGlyph() {
        this.setUrl("/fx-plus/font/configuration.svg");
    }

    public ConfigurationSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
