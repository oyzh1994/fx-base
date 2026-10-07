package cn.oyzh.fx.gui.svg.glyph.key;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 生成密钥 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-04-03
 */
public class GenerateKeySVGGlyph extends SVGGlyph {

    /**
     * 构造生成密钥 SVG 图标控件
     */
    public GenerateKeySVGGlyph() {
        super("/fx-svg/key/generate-key.svg");
    }

    /**
     * 构造指定尺寸的生成密钥 SVG 图标控件
     *
     * @param size 尺寸
     */
    public GenerateKeySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
