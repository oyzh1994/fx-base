package cn.oyzh.fx.gui.svg.glyph.page;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class PageLastSVGGlyph extends SVGGlyph {

    public PageLastSVGGlyph() {
        this.setUrl("/fx-plus/font/page/page-last.svg");
    }

    public PageLastSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.lastPage());
        super.initNode();
    }
}
