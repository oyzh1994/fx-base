package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class PositioningSVGGlyph  extends SVGGlyph {

    public PositioningSVGGlyph() {
        this.setUrl("/fx-plus/font/positioning.svg");
    }

    public PositioningSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.positioning());
        super.initNode();
    }
}
