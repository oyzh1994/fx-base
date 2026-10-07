package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 新增文档 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class AddDocumentSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public AddDocumentSVGGlyph() {
        super("/fx-svg/add-document.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public AddDocumentSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double widthScaling() {
        return 0.9;
    }
}

