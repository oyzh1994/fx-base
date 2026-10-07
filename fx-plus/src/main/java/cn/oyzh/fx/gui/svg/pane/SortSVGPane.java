package cn.oyzh.fx.gui.svg.pane;

import cn.oyzh.fx.gui.svg.glyph.SortAscSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SortDescSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGPane;
import javafx.scene.text.Font;

/**
 * 排序切换图标面板
 *
 * @author oyzh
 * @since 2024-12-09
 */
public class SortSVGPane extends SVGPane {

    /**
     * 构造排序切换图标面板，默认降序状态。
     */
    public SortSVGPane() {
        this.desc();
    }

    /**
     * 切换为降序图标
     */
    public void desc() {
        this.setChild(new SortAscSVGGlyph(this.getSize()));
    }

    /**
     * 切换为升序图标
     */
    public void asc() {
        this.setChild(new SortDescSVGGlyph(this.getSize()));
    }

    /**
     * 是否为升序状态
     *
     * @return 是否为升序状态
     */
    public boolean isAsc() {
        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
        return svgGlyph.getUrl().contains("sort-descending.svg");
    }

    /**
     * 设置升序状态
     *
     * @param asc 是否升序
     */
    public void setAsc(boolean asc) {
        if (asc) {
            this.asc();
        } else {
            this.desc();
        }
    }

    @Override
    public void changeFont(Font font) {
        if (!this.isChildEmpty() && this.isEnableFont()) {
            if (this.isAsc()) {
                this.asc();
            } else {
                this.desc();
            }
        }
        super.changeFont(font);
    }
}
