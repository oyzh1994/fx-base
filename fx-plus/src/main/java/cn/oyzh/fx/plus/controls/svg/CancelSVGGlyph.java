package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class CancelSVGGlyph extends CloseSVGGlyph {

    public CancelSVGGlyph(String size) {
        super();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(BaseResourceBundle.getBaseString("base.cancel"));
        super.initNode();
    }
}
