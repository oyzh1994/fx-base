package cn.oyzh.fx.gui.svg.label;

import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.i18n.I18nHelper;

/**
 * 搜索标签
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class SearchSVGLabel extends SVGLabel {

    /**
     * 构造搜索标签
     */
    public SearchSVGLabel() {
        super("/fx-svg/search.svg");
    }

    /**
     * 构造搜索标签
     *
     * @param size 图标尺寸
     */
    public SearchSVGLabel(String size) {
        this();
        this.graphic().setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setText(I18nHelper.search());
        // this.setTipText(I18nResourceBundle.i18nString("base.search"));
        super.initNode();
    }
}
