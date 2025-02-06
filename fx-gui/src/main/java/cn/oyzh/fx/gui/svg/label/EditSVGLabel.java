package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.EditSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class EditSVGLabel extends SVGLabel {

    public EditSVGLabel() {
        this.setGraphic(new EditSVGGlyph());
    }

    public EditSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.edit());
//        this.setTipText(I18nHelper.edit());
        super.initNode();
    }
}
