package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.util.TextUtil;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

/**
 * bit文本输入框
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class BitTextField extends LimitTextField {

    /**
     * 文本格式器
     */
    protected final TextFormatter<Number> textFormatter;

    public BitTextField() {
        this(null);
    }

    public BitTextField(Long maxLen) {
        // 长度
        this.setMaxLen(maxLen);
        // 创建文本格式化器
        this.textFormatter = new TextFormatter<>(this.createFilter());
        // 将TextFormatter对象设置到文本字段中
        this.setTextFormatter(this.textFormatter);
    }

    protected UnaryOperator<TextFormatter.Change> createFilter() {
        return change -> {
            if (change.isAdded() || change.isReplaced() || change.isContentChange()) {
                try {
                    // 数据内容为空
                    String text = change.getControlNewText();
                    if (text.isEmpty()) {
                        return change;
                    }
                    if (!super.checkLenLimit(change)) {
                        return null;
                    }
                    if (!text.matches("^[01]+$")) {
                        return null;
                    }
                    return change;
                } catch (Exception ignored) {
                }
            }
            return change;
        };
    }

    @Override
    public byte[] getValue() {
        String val = this.getText();
        if (val == null || val.isEmpty()) {
            return null;
        }
        return TextUtil.bitStrToByte(val);
    }

    @Override
    public void setValue(Object val) {
        super.setValue(val);
        this.setText(format(val));
    }

    public static String format(Object val) {
        if (val instanceof byte[] bytes) {
            return TextUtil.byteToBitStr(bytes);
        }
        if (val instanceof Byte b) {
            return TextUtil.byteToBitStr(new byte[]{b});
        }
        return val.toString();

    }
}
