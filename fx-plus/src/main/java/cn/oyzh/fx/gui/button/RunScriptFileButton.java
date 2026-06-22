package cn.oyzh.fx.gui.button;

import cn.oyzh.fx.gui.svg.glyph.database.RunFileSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.RunSqlFileSVGGlyph;
import cn.oyzh.fx.plus.controls.button.IconButton;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/29
 */
public class RunScriptFileButton extends IconButton {

    @Override
    public void initNode() {
        this.setRealHeight(30);
        this.setText(I18nHelper.runScriptFile());
        this.setTipText(I18nHelper.runScriptFile());
        this.init(new RunFileSVGGlyph());
        super.initNode();
    }
}
