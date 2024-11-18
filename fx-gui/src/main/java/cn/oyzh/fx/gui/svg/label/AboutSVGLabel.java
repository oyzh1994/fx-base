package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class AboutSVGLabel extends SVGLabel {

    public AboutSVGLabel() {
        this.setUrl("/fx-plus/font/info-circle.svg");
    }

    public AboutSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.about());
        this.setTipText(I18nHelper.about());
        super.initNode();
    }
}
