package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.SnippetSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 代码片段标签
 *
 * @author oyzh
 * @since 2024-04-08
 */
public class SnippetSVGLabel extends SVGLabel {

    /**
     * 构造代码片段标签
     */
    public SnippetSVGLabel() {
        this.setGraphic(new SnippetSVGGlyph());
    }

    /**
     * 构造代码片段标签
     *
     * @param size 图标尺寸
     */
    public SnippetSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.snippet());
        super.initNode();
    }
}
