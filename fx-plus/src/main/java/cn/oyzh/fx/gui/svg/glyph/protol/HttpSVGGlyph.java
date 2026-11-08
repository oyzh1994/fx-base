package cn.oyzh.fx.gui.svg.glyph.protol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * HTTP 协议 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class HttpSVGGlyph extends SVGGlyph {

    /**
     * 构造 HTTP 协议 SVG 图标控件
     */
    public HttpSVGGlyph() {
        super("/fx-svg/protol/http.svg");
    }

    /**
     * 构造指定尺寸的 HTTP 协议 SVG 图标控件
     *
     * @param size 尺寸
     */
    public HttpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
