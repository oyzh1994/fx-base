package cn.oyzh.fx.plus.tabs;
import cn.oyzh.fx.plus.controls.combo.FlexComboBox;

/**
 * @author oyzh
 * @since 2023/12/11
 */
public class DynamicTabStrategyComboBox extends FlexComboBox<String> {

    {
        this.getItems().add("所有连接");
        this.getItems().add("单个连接");
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
}
