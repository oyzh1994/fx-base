package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class SettingSVGGlyph extends SVGGlyph {

    public SettingSVGGlyph() {
        this.setUrl("/fx-plus/font/setting.svg");
    }

    public SettingSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.setting());
        super.initNode();
    }
}
