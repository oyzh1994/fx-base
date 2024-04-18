package cn.oyzh.fx.plus.controls.area;

import cn.oyzh.fx.plus.LimitControl;
import javafx.scene.control.TextFormatter;
import lombok.Getter;

import java.security.InvalidParameterException;
import java.util.function.UnaryOperator;

/**
 * 限制文本域
 *
 * @author oyzh
 * @since 2023/09/22
 */
public class LimitTextArea extends FlexTextArea implements LimitControl {

    /**
     * 最小长度
     */
    @Getter
    private Integer minLen;

    /**
     * 最大长度
     */
    @Getter
    private Integer maxLen;

    {
        // 创建TextFormatter对象
        this.setTextFormatter(new TextFormatter<>(this.createFilter()));
    }

    public LimitTextArea() {
        super.setText("");
    }

    public LimitTextArea(String text) {
        super.setText(text);
    }

    public LimitTextArea(Integer maxLen) {
        this.maxLen = maxLen;
    }

    public LimitTextArea(Integer minLen, Integer maxLen) {
        this.minLen = minLen;
        this.maxLen = maxLen;
    }

    /**
     * 创建一个过滤器，用于限制文本只能输入整数
     *
     * @return 过滤器
     */
    protected UnaryOperator<TextFormatter.Change> createFilter() {
        return change -> {
            if (this.checkLimit(change)) {
                return change;
            }
            return null;
        };
    }

    @Override
    public void setMaxLen(Integer maxLen) {
        if (this.minLen != null && maxLen.longValue() < this.minLen.longValue()) {
            throw new InvalidParameterException("maxLen cannot be less than minLen！");
        }
        this.maxLen = maxLen;
    }

    @Override
    public void setMinLen(Integer minLen) {
        if (this.maxLen != null && minLen.longValue() > this.maxLen.longValue()) {
            throw new InvalidParameterException("minLen cannot be greater than maxLen！");
        }
        this.minLen = minLen;
    }
}
