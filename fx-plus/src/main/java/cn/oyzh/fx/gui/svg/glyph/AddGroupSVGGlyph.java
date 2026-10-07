package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 新增分组 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class AddGroupSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public AddGroupSVGGlyph() {
        super("/fx-svg/addGroup.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public AddGroupSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.addGroup());
//        super.initNode();
//    }
}
