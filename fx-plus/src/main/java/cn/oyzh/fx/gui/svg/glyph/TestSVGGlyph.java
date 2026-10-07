package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 测试 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class TestSVGGlyph extends SVGGlyph {

    /**
     * 构造测试 SVG 图标控件
     */
    public TestSVGGlyph() {
        super("/fx-svg/link.svg");
    }

    /**
     * 构造指定尺寸的测试 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public TestSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.test());
//        super.initNode();
//    }
}
