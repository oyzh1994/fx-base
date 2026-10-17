package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import javafx.scene.text.Font;

/**
 * 排序切换图标面板
 *
 * @author oyzh
 * @since 2024-12-13
 */
public class SortSVGLabel extends SVGLabel {

    /**
     * 构造排序切换图标面板，默认降序状态。
     */
    public SortSVGLabel() {
        this.desc();
    }

    /**
     * 切换为降序图标
     */
    public void desc() {
        this.setUrl("/fx-svg/sort-ascending.svg");
        //super.setText("\u200B");
    }

    /**
     * 切换为升序图标
     */
    public void asc() {
        this.setUrl("/fx-svg/sort-descending.svg");
        //super.setText("\u200B");
    }

    /**
     * 是否为升序状态
     *
     * @return 是否为升序状态
     */
    public boolean isAsc() {
        return this.getUrl().contains("sort-descending.svg");
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
