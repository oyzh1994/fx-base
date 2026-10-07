package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.MigrationSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 迁移标签
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class MigrationSVGLabel extends SVGLabel {

    /**
     * 构造迁移标签
     */
    public MigrationSVGLabel() {
        this.setGraphic(new MigrationSVGGlyph());
    }

    /**
     * 构造迁移标签
     *
     * @param size 图标尺寸
     */
    public MigrationSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.migration());
//        this.setTipText(I18nHelper.migration());
        super.initNode();
    }
}
