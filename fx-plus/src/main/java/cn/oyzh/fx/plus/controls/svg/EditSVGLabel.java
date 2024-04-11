package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class EditSVGLabel extends SVGLabel {

    public EditSVGLabel() {
        super("/fx-plus/font/edit.svg");
    }

    public EditSVGLabel(String size) {
        this();
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("edit"));
        this.setTipText(BaseResourceBundle.getBaseString("edit"));
        super.initNode();
    }
}
