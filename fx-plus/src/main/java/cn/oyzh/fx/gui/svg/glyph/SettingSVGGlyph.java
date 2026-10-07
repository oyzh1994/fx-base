package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 设置 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class SettingSVGGlyph extends SVGGlyph {

    /**
     * 构造设置 SVG 图标控件
     */
    public SettingSVGGlyph() {
        super("/fx-svg/setting.svg");
    }

    /**
     * 构造指定尺寸的设置 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public SettingSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.setting());
//        super.initNode();
//    }
}
