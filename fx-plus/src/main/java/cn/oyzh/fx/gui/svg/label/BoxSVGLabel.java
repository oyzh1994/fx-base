package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.BoxSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;

/**
 * 盒状标签
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class BoxSVGLabel extends SVGLabel {

    /**
     * 构造盒状标签
     */
    public BoxSVGLabel() {
        this.setGraphic(new BoxSVGGlyph());
    }

    /**
     * 构造盒状标签
     *
     * @param size 图标尺寸
     */
    public BoxSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }

}
