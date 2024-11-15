package cn.oyzh.fx.plus.gui.svg.glyph.page;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class PageSettingSVGGlyph extends SVGGlyph {

    public PageSettingSVGGlyph() {
        this.setUrl("/fx-plus/font/page/page-setting.svg");
    }

    public PageSettingSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.setting());
        super.initNode();
    }
}
