package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.plus.LimitLenControl;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.operator.LimitOperator;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import javafx.scene.control.TextFormatter;

/**
 * 限制文本输入框
 *
 * @author oyzh
 * @since 2023/08/29
 */
public class LimitTextField extends FXTextField implements LimitLenControl {

    /**
     * 最大长度
     */
    protected Long maxLen;

    @Override
    public Long getMaxLen() {
        return maxLen;
    }

    public LimitTextField() {
        super.setText("");
    }

    public LimitTextField(String text) {
        super.setText(text);
    }

    public LimitTextField(Long maxLen) {
        this.setMaxLen(maxLen);
    }

    public LimitTextField(String text, Long maxLen) {
        super.setText(text);
        this.setMaxLen(maxLen);
    }

    @Override
    public void setMaxLen(Long maxLen) {
        this.maxLen = maxLen;
        if (maxLen != null && this.getTextFormatter() == null) {
            this.setTextFormatter(new TextFormatter<>(new LimitOperator()));
        }
    }

    @Override
    public boolean validate() {
        if (!super.validate()) {
            return false;
        }
        if (this.maxLen != null && this.getText().length() > this.maxLen) {
            ValidatorUtil.validFail(this);
            return false;
        }
        return true;
    }
}
