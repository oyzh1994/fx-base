package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 主题切换标签
 *
 * @author oyzh
 * @since 2026/06/19
 */
public class ThemeSVGLabel extends SVGLabel {

    public ThemeSVGLabel() {
        this.setGraphic(new SVGGlyph("/fx-svg/contrast.svg"));
    }

    public ThemeSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }
}
