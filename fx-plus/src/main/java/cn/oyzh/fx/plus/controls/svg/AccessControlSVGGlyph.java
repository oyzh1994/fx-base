package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class AccessControlSVGGlyph extends SVGGlyph {

    public AccessControlSVGGlyph() {
        this.setUrl("/fx-plus/font/access-control.svg");
    }

    public AccessControlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
