package cn.oyzh.fx.gui.button;

import cn.oyzh.fx.gui.svg.glyph.database.RunSqlFileSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/29
 */
public class RunSqlFileButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.setText(I18nHelper.runSqlFile());
        this.setTipText(I18nHelper.runSqlFile());
        this.init(new RunSqlFileSVGGlyph(), 0.9);
        super.initNode();
    }
}
