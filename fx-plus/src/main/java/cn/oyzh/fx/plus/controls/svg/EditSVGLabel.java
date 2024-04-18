package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

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
        this.setText(I18nResourceBundle.i18nString("base.edit"));
        this.setTipText(I18nResourceBundle.i18nString("base.edit"));
        super.initNode();
    }
}
