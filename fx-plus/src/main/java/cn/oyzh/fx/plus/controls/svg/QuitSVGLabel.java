package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class QuitSVGLabel extends SVGLabel {

    public QuitSVGLabel() {
        this.setGraphic(new QuitSVGGlyph());
    }

    public QuitSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.quit());
        // this.setTipText(I18nResourceBundle.i18nString("base.quit"));
        super.initNode();
    }
}
