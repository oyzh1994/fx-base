package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.plus.LimitLenControl;
import cn.oyzh.fx.plus.controls.text.field.FlexTextField;
import cn.oyzh.fx.plus.operator.LimitOperator;
import javafx.scene.control.TextFormatter;
import lombok.Getter;

/**
 * 限制文本输入框
 *
 * @author oyzh
 * @since 2023/08/29
 */
public class LimitTextField extends FlexTextField implements LimitLenControl {

    /**
     * 最大长度
     */
    @Getter
    protected Long maxLen;

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
}
