package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;

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
        this.setUrl("/fx-svg/contrast.svg");
        //super.setText("\u200B");
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
