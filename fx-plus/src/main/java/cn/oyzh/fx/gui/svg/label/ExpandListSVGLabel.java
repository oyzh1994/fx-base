package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;

/**
 * 展开列表svg面板
 *
 * @author oyzh
 * @since 2025-01-04
 */
public class ExpandListSVGLabel extends SVGLabel {

    /**
     * 构造展开列表svg面板
     */
    public ExpandListSVGLabel() {
        this.collapse();
    }

    /**
     * 展开
     */
    public void expand() {
        this.setUrl("/fx-svg/arrow-down-double-line.svg");
        //super.setText("\u200B");
    }

    /**
     * 收起
     */
    public void collapse() {
        this.setUrl("/fx-svg/arrow-up-double-line.svg");
        //super.setText("\u200B");
    }

    /**
     * 是否收起
     *
     * @return 是否收起
     */
    public boolean isCollapse() {
        return this.getUrl().contains("arrow-up-double-line.svg");
    }

    /**
     * 设置收起
     *
     * @param collapse 待设置的收起
     */
    public void setCollapse(boolean collapse) {
        if (collapse) {
            this.collapse();
        } else {
            this.expand();
        }
    }
}
