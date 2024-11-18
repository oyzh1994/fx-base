package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class CancelSVGGlyph extends CloseSVGGlyph {

    public CancelSVGGlyph( ) {
        super();
    }

    public CancelSVGGlyph(String size) {
        super();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.cancel());
        super.initNode();

    }
}
