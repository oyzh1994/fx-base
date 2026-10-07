package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 更新日志 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ChangelogSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public ChangelogSVGGlyph() {
        super("/fx-svg/changelog.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ChangelogSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.changelog());
//        super.initNode();
//    }
}
