package cn.oyzh.fx.plus.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.gui.svg.glyph.ExpendSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ExpandSVGLabel extends SVGLabel {

    public ExpandSVGLabel() {
        this.setGraphic(new ExpendSVGGlyph());
    }

    public ExpandSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.expand());
        // this.setTipText(I18nResourceBundle.i18nString("base.expand"));
        super.initNode();
    }
}
