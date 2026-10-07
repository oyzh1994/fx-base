package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 主题切换标签
 *
 * @author oyzh
 * @since 2026-06-19
 */
public class ThemeSVGLabel extends SVGLabel {

    /**
     * 构造主题切换标签
     */
    public ThemeSVGLabel() {
        this.setGraphic(new SVGGlyph("/fx-svg/contrast.svg"));
    }

    /**
     * 构造主题切换标签
     *
     * @param size 图标尺寸
     */
    public ThemeSVGLabel(String size) {
        this();
        this.setSizeStr(size);
    }
}
