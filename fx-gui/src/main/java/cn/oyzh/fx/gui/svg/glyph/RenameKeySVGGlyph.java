package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class RenameKeySVGGlyph extends RenameSVGGlyph {

    public RenameKeySVGGlyph() {
        super();
    }

    public RenameKeySVGGlyph(String size) {
        super(size);
    }

    @Override
    public void initNode() {
        super.initNode();
        this.setTipText(I18nHelper.renameKey());
    }
}
