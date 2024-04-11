package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class DeleteSVGLabel extends SVGLabel {

    public DeleteSVGLabel() {
        super("/fx-plus/font/delete.svg");
    }

    public DeleteSVGLabel(String size) {
        this();
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("delete"));
        this.setTipText(BaseResourceBundle.getBaseString("delete"));
        super.initNode();
    }
}
