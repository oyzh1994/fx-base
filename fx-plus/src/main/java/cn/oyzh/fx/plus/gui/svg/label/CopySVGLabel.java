package cn.oyzh.fx.plus.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.gui.svg.glyph.CopySVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class CopySVGLabel extends SVGLabel {

    public CopySVGLabel() {
        this.setGraphic(new CopySVGGlyph());
    }

    public CopySVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.copy());
        this.setTipText(I18nHelper.copy());
        super.initNode();
    }
}
