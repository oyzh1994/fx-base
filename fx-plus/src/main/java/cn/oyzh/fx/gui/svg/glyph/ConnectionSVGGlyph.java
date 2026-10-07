package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 连接 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class ConnectionSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public ConnectionSVGGlyph() {
        super("/fx-svg/connections.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ConnectionSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.connection());
//        super.initNode();
//    }
}
