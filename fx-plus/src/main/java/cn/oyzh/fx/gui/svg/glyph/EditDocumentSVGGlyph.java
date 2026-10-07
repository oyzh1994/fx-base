package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 编辑文档 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class EditDocumentSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public EditDocumentSVGGlyph() {
        super("/fx-svg/edit_document.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public EditDocumentSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.edit());
//        super.initNode();
//    }
}
