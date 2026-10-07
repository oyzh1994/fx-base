package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.util.TextUtil;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

/**
 * 位文本输入框
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class BitTextField extends LimitTextField {

    /**
     * 文本格式器
     */
    protected final TextFormatter<Number> textFormatter;

    /**
     * 构造位文本输入框
     */
    public BitTextField() {
        this(null);
    }

    /**
     * 构造位文本输入框
     *
     * @param maxLen 最大长度
     */
    public BitTextField(Long maxLen) {
        // 长度
        this.setMaxLen(maxLen);
        // 创建文本格式化器
        this.textFormatter = new TextFormatter<>(this.createFilter());
        // 将TextFormatter对象设置到文本字段中
        this.setTextFormatter(this.textFormatter);
    }

    /**
     * 创建过滤器，仅允许输入由 0、1 组成的文本。
     *
     * @return 文本变更过滤器
     */
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
    public void formatValue() {
        this.setText(format(super.value()));
    }

    /**
     * 将字节数据格式化为二进制字符串
     *
     * @param val 值
     * @return 二进制字符串
     */
    public static String format(Object val) {
        if (val instanceof byte[] bytes) {
            return TextUtil.byteToBitStr(bytes);
        }
        if (val instanceof Byte b) {
            return TextUtil.byteToBitStr(new byte[]{b});
        }
        if (val == null) {
            return null;
        }
        return val.toString();
    }
}
