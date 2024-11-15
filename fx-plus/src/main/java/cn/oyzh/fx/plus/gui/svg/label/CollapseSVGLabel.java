package cn.oyzh.fx.plus.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.gui.svg.glyph.CollapseSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class CollapseSVGLabel extends SVGLabel {

    public CollapseSVGLabel() {
        this.setGraphic(new CollapseSVGGlyph());
    }

    public CollapseSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.collapse());
        // this.setTipText(I18nResourceBundle.i18nString("base.collapse"));
        super.initNode();
    }
}
