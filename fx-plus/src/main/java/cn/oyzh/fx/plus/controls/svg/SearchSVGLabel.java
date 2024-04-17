package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SearchSVGLabel extends SVGLabel {

    public SearchSVGLabel() {
        this.setUrl("/fx-plus/font/search.svg");
    }

    public SearchSVGLabel(String size) {
        this();
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(BaseResourceBundle.getBaseString("base.search"));
        this.setTipText(BaseResourceBundle.getBaseString("base.search"));
        super.initNode();
    }
}
