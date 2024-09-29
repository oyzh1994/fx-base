package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class BatchOptSVGGlyph extends SVGGlyph {

    public BatchOptSVGGlyph() {
        this.setUrl("/fx-plus/font/mml-batch-command-16.svg");
    }

    public BatchOptSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.batchOpt());
        super.initNode();
    }
}
