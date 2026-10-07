package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 批量操作 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class BatchOptSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public BatchOptSVGGlyph() {
        super("/fx-svg/mml-batch-command-16.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public BatchOptSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.batchOpt());
//        super.initNode();
//    }
}
