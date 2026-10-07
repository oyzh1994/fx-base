package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.font.FontManager;

/**
 * 数据库事件 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-09-05
 */
public class EventSVGGlyph extends SVGGlyph {

    /**
     * 构造数据库事件 SVG 图标控件
     */
    public EventSVGGlyph() {
        super("/fx-svg/database/event.svg");
    }

    /**
     * 构造指定尺寸的数据库事件 SVG 图标控件
     *
     * @param size 尺寸
     */
    public EventSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
