package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ConnectionSVGGlyph extends SVGGlyph {

    public ConnectionSVGGlyph() {
        this.setUrl("/fx-plus/font/connections.svg");
    }

    public ConnectionSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("connection"));
        super.initNode();
    }
}
