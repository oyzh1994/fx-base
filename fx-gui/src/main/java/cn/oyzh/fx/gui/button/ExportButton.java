package cn.oyzh.fx.gui.button;

import cn.oyzh.fx.gui.svg.glyph.ExportSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ExportButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.setText(I18nHelper.export());
        this.setTipText(I18nHelper.export());
        this.init(new ExportSVGGlyph(), 0.7);
        super.initNode();
    }
}
