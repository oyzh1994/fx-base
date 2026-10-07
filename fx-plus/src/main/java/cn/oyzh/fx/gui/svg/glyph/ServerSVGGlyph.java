package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 服务器 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-12
 */
public class ServerSVGGlyph extends SVGGlyph {

    /**
     * 构造服务器 SVG 图标控件
     */
    public ServerSVGGlyph() {
        super("/fx-svg/sever.svg");
    }

    /**
     * 构造指定尺寸的服务器 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public ServerSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
