package cn.oyzh.fx.plus.controls.textfield;

import cn.oyzh.fx.common.util.NumUtil;
import javafx.scene.control.TextFormatter;
import lombok.Getter;

import java.security.InvalidParameterException;
import java.util.function.UnaryOperator;

/**
 * 限制文本输入框
 *
 * @author oyzh
 * @since 2023/08/29
 */
public class LimitTextField extends FlexTextField {

    /**
     * 最小长度
     */
    @Getter
    private Number minLen;

    /**
     * 最大长度
     */
    @Getter
    private Number maxLen;

    {
        // 创建TextFormatter对象
        this.setTextFormatter(new TextFormatter<>(this.createFilter()));
    }

    public LimitTextField() {
        super.setText("");
    }

    public LimitTextField(String text) {
        super.setText(text);
    }

    /**
     * 创建一个过滤器，用于限制文本只能输入整数
     *
     * @return 过滤器
     */
    protected UnaryOperator<TextFormatter.Change> createFilter() {
        return change -> {
            if (this.maxLen != null || this.minLen != null) {
                try {
                    String text = change.getControlNewText();
                    if (change.isAdded() && NumUtil.isGTEq(text.length(), this.maxLen)) {
                        return null;
                    }
                    if (change.isReplaced() || change.isDeleted()) {
                        if (NumUtil.isGTEq(text.length(), this.maxLen)) {
                            return null;
                        }
                        if (NumUtil.isLTEq(text.length(), this.minLen)) {
                            return null;
                        }
                    }
                    return change;
                } catch (Exception ignore) {
                }
            }
            return change;
        };
    }

    public void setMaxLen(Number maxLen) {
        if (this.minLen != null && maxLen.longValue() < this.minLen.longValue()) {
            throw new InvalidParameterException("maxLen不能小于minLen！");
        }
        this.maxLen = maxLen.longValue();
    }

    public void setMinLen(Number minLen) {
        if (this.maxLen != null && minLen.longValue() > this.maxLen.longValue()) {
            throw new InvalidParameterException("minLen不能大于maxLen！");
        }
        this.minLen = minLen.longValue();
    }
}
