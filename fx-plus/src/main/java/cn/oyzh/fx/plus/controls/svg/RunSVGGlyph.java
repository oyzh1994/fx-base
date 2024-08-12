package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/12
 */
public class RunSVGGlyph extends SVGGlyph {

    public RunSVGGlyph() {
        this.setUrl("/fx-plus/font/run-solid.svg");
    }

    public RunSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.add());
        super.initNode();
    }
}
