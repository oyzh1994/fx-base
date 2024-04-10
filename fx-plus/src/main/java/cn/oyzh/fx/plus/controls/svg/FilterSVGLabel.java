package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class FilterSVGLabel extends SVGLabel{

    @Override
    public void initNode() {
        this.setUrl("/fx-plus/font/filter.svg");
        this.setText(BaseResourceBundle.getBaseString("filter"));
        this.setTipText(BaseResourceBundle.getBaseString("filter"));
        super.initNode();
    }
}
