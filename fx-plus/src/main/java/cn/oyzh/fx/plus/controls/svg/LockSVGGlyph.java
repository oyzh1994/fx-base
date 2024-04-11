package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class LockSVGGlyph extends SVGGlyph {

    public LockSVGGlyph() {
        super("/fx-plus/font/lock.svg");
    }

    public LockSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
