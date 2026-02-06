package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ConnectionSVGGlyph extends SVGGlyph {

    public ConnectionSVGGlyph() {
        this.setUrl("/fx-svg/connections.svg");
    }

    public ConnectionSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.connection());
//        super.initNode();
//    }
}
