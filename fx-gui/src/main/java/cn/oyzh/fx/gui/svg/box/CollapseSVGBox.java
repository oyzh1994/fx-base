package cn.oyzh.fx.gui.svg.box;

import cn.oyzh.fx.gui.svg.glyph.CollapseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGBox;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class CollapseSVGBox extends SVGBox {

    public CollapseSVGBox() {
        this.setGlyph(new CollapseSVGGlyph());
    }

    public CollapseSVGBox(String size) {
        this();
        this.setSize(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.collapse());
        this.setTipText(I18nHelper.collapse());
        super.initNode();
    }
}
