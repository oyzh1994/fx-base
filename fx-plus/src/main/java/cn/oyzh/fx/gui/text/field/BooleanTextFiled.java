package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.util.BooleanUtil;

/**
 * @author oyzh
 * @since 2024/7/10
 */
public class BooleanTextFiled extends SelectTextFiled<String> {

    @Override
    public Boolean getValue() {
        String text = this.getSelectedItem();
        return text.equals("true");
    }

    @Override
    public void setValue(Object val) {
        super.setValue(val);
        String item = format(val);
        super.selectItem(item);
    }

    @Override
    public void initNode() {
        super.addItem("true");
        super.addItem("false");
        this.setEditable(false);
        super.initNode();
    }

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
        return "false";
    }
}
