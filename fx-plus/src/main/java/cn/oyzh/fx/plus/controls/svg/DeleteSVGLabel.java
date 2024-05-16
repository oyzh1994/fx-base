package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class DeleteSVGLabel extends SVGLabel {

    public DeleteSVGLabel() {
        this.setGraphic(new DeleteSVGGlyph());
    }

    public DeleteSVGLabel(String size) {
        this();
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.delete());
        this.setTipText(I18nHelper.delete());
        super.initNode();
    }
}
