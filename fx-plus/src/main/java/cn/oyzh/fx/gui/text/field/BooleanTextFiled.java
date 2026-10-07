package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.util.BooleanUtil;

/**
 * 布尔文本输入框
 *
 * @author oyzh
 * @since 2026-06-05
 */
public class BooleanTextFiled extends SelectTextFiled<String> {

    @Override
    public Boolean getValue() {
        String text = this.getSelectedItem();
        return text.equals("true");
    }

    @Override
    public void formatValue() {
        String item = format(super.value());
        if (item != null) {
            super.selectItem(item);
        }
    }

    @Override
    public void initNode() {
        super.addItem("true");
        super.addItem("false");
        this.setEditable(false);
        super.initNode();
    }

    /**
     * 将任意值格式化为 "true" 或 "false"
     *
     * @param o 值
     * @return 格式化结果
     */
    public static String format(Object o) {
        if (o instanceof CharSequence s) {
            if (s.equals("1") || s.toString().equalsIgnoreCase("true")) {
                return "true";
            }
            return "false";
        }
        if (o instanceof Boolean b) {
            if (BooleanUtil.isTrue(b)) {
                return "true";
            }
            return "false";
        }
        if (o instanceof Number n) {
            if (n.doubleValue() == 1) {
                return "true";
            }
            return "false";
        }
        if (o != null) {
            return "false";
        }
        return null;
    }
}
