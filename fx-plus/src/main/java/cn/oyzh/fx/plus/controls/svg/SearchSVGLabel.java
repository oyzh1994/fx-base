package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SearchSVGLabel extends SVGLabel {

    @Override
    public void initNode() {
        this.setUrl("/fx-plus/font/search.svg");
        this.setText(BaseResourceBundle.getBaseString("search"));
        this.setTipText(BaseResourceBundle.getBaseString("search"));
        super.initNode();
    }
}
