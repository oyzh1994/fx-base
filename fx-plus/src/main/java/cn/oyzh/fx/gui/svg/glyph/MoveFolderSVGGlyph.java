package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 移动文件夹 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class MoveFolderSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public MoveFolderSVGGlyph() {
        super("/fx-svg/move-folder.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public MoveFolderSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
