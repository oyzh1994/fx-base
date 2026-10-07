package cn.oyzh.fx.gui.button;

import cn.oyzh.fx.gui.svg.glyph.MigrationSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * 迁移按钮
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class MigrationButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.setText(I18nHelper.migration());
        this.setTipText(I18nHelper.migration());
        this.init(new MigrationSVGGlyph());
        super.initNode();
    }
}
