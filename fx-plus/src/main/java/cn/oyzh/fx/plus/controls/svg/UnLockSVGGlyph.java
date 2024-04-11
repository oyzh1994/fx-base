package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class UnLockSVGGlyph extends SVGGlyph {

    public UnLockSVGGlyph() {
        super("/fx-plus/font/unlock.svg");
    }

    public UnLockSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
