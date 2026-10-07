package cn.oyzh.fx.gui.svg.glyph.key;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 密钥 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-10-16
 */
public class KeySVGGlyph extends SVGGlyph {

    /**
     * 构造密钥 SVG 图标控件
     */
    public KeySVGGlyph() {
        super("/fx-svg/key/key.svg");
    }

    /**
     * 构造指定尺寸的密钥 SVG 图标控件
     *
     * @param size 尺寸
     */
    public KeySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
