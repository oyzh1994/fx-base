package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ProcedureSVGGlyph extends SVGGlyph {

    public ProcedureSVGGlyph() {
        this.setUrl("/fx-plus/font/procedure.svg");
    }

    public ProcedureSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.procedure());
        super.initNode();
    }
}
