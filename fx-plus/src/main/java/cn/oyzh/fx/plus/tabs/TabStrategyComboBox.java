package cn.oyzh.fx.plus.tabs;

import cn.oyzh.fx.plus.controls.combo.FlexComboBox;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;
import cn.oyzh.fx.plus.node.NodeManager;

import java.util.List;
import java.util.Locale;

/**
 * @author oyzh
 * @since 2023/12/11
 */
public class TabStrategyComboBox extends FlexComboBox<String> implements I18nSelectAdapter<String> {

    {
        NodeManager.init(this);
    }

    /**
     * 获取策略
     *
     * @return 策略
     */
    public String getStrategy() {
        if (this.getSelectedIndex() == 0) {
            return "ALL_CONNECT";
        }
        return "SINGLE_CONNECT";
    }

    @Override
    public void select(String strategy) {
        if ("SINGLE_CONNECT".equals(strategy)) {
            this.select(1);
        } else {
            this.select(0);
        }
    }

    @Override
    public List<String> values(Locale locale) {
        this.clearItems();
        if (locale == Locale.TRADITIONAL_CHINESE) {
            this.addItem("所有連接");
            this.addItem("單個連接");
        } else if (locale == Locale.SIMPLIFIED_CHINESE) {
            this.addItem("所有连接");
            this.addItem("单个连接");
        } else {
            this.addItem("All connections");
            this.addItem("Single connection");
        }
        return this.getItems();
    }
}
