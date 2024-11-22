package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SearchSVGLabel extends SVGLabel {

    public SearchSVGLabel() {
        this.setUrl("/fx-svg/search.svg");
    }

    public SearchSVGLabel(String size) {
        this();
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.search());
        // this.setTipText(I18nResourceBundle.i18nString("base.search"));
        super.initNode();
    }
}
