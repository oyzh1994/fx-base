package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.gui.svg.glyph.layout.Layout1SVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.layout.Layout2SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;

/**
 * 布局切换图标面板
 *
 * @author oyzh
 * @since 2025-04-02
 */
public class LayoutSVGLabel extends SVGLabel {

    /**
     * 构造布局切换图标面板，默认布局2状态。
     */
    public LayoutSVGLabel() {
        this.layout2();
    }

    /**
     * 切换为布局1图标
     */
    public void layout1() {
        super.graphic(new Layout1SVGGlyph());
        //super.setText("\u200B");
    }

    /**
     * 切换为布局2图标
     */
    public void layout2() {
        super.graphic(new Layout2SVGGlyph());
        //super.setText("\u200B");
    }

    /**
     * 是否为布局1状态
     *
     * @return 是否为布局1状态
     */
    public boolean isLayout1() {
        return this.getUrl().contains("layout1.svg");
    }
}
