package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

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
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("base.edit"));
        this.setTipText(BaseResourceBundle.getBaseString("base.edit"));
        super.initNode();
    }
}
