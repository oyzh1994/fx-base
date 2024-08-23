package cn.oyzh.fx.plus.controls.textfield;

import cn.oyzh.fx.common.util.NumUtil;
import cn.oyzh.fx.common.util.TextUtil;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

/**
 * bit文本输入框
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class BitTextField extends ClearableTextField {

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

    /**
     * 获取值
     *
     * @return 值
     */
    public byte[] getValue() {
        String val = this.getText();
        if (val == null || val.isEmpty()) {
            return null;
        }
        return TextUtil.bitStrToByte(val);
    }

    /**
     * 设置值
     *
     * @param val 值
     */
    public void setValue(byte[] val) {
        this.setText(format(val));
    }

    @Override
    public void setValue(Object val) {
        if (val instanceof CharSequence sequence) {
            this.setText(sequence.toString());
        } else if (val instanceof byte[] bytes) {
            this.setValue(bytes);
        } else if (val instanceof Byte b) {
            this.setValue(new byte[]{b});
        }
    }

    public static String format(byte[] bytes) {
        return TextUtil.byteToBitStr(bytes);
    }
}
