package cn.oyzh.fx.gui.svg.glyph.page;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class PageNextSVGGlyph extends SVGGlyph {

    public PageNextSVGGlyph() {
        this.setUrl("/fx-gui/font/page/page-next.svg");
    }

    public PageNextSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.nextPage());
        super.initNode();
    }
}
