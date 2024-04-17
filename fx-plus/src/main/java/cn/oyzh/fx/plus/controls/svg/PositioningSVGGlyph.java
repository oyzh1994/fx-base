package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class PositioningSVGGlyph  extends SVGGlyph{

    public PositioningSVGGlyph() {
        this.setUrl("/fx-plus/font/positioning.svg");
    }

    public PositioningSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("base.positioning"));
        super.initNode();
    }
}
