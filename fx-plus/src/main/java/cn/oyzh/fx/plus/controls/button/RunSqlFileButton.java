package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.controls.svg.database.RunSqlFileSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/29
 */
public class RunSqlFileButton extends IconButton{

    @Override
    public void initNode() {
        this.setPrefHeight(25);
        this.setText(I18nHelper.runSqlFile());
        this.setTipText(I18nHelper.runSqlFile());
        this.init(new RunSqlFileSVGGlyph(), 0.9);
        super.initNode();
    }
}
