package cn.oyzh.fx.plus.controls.textfield;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.converter.LongConverter;
import cn.oyzh.fx.plus.skin.NumberTextFieldSkin;
import javafx.scene.control.TextFormatter;
import lombok.Getter;
import lombok.Setter;

import java.util.function.UnaryOperator;

/**
 * 整数文本域
 *
 * @author oyzh
 * @since 2020/10/29
 */
public class NumberTextField extends FlexTextField {

    /**
     * 最大值
     */
    @Getter
    @Setter
    protected Long max;

    /**
     * 最小值
     */
    @Getter
    @Setter
    protected Long min;

    /**
     * 递进值
     */
    @Getter
    @Setter
    protected Long step = 1L;

    /**
     * 数据转换器
     */
    protected final LongConverter converter = new LongConverter();

    /**
     * 文本格式器
     */
    protected final TextFormatter<Long> textFormatter = new TextFormatter<>(this.converter, 0L, this.createFilter());

    {
        this.setSkin(new NumberTextFieldSkin(this, this::incrValue, this::decrValue));
    }

    /**
     * 增加值
     */
    protected void incrValue() {
        // 设置值
        if (this.step != null) {
            this.setValue(this.getValue() + this.step);
        }
    }

    /**
     * 减少值
     */
    protected void decrValue() {
        // 设置值
        if (this.step != null) {
            this.setValue(this.getValue() - this.step);
        }
    }

    public NumberTextField() {
        // 将TextFormatter对象设置到文本字段中
        this.setTextFormatter(this.textFormatter);
        // 监听值变化
        this.textFormatter.valueProperty().addListener((observableValue, number, t1) -> this.valueChanged(t1));
    }

    /**
     * 创建一个过滤器，用于限制文本输入
     *
     * @return 过滤器
     */
    protected UnaryOperator<TextFormatter.Change> createFilter() {
        return change -> {
            if (change.isAdded() || change.isReplaced() || change.isContentChange()) {
                try {
                    String text = change.getControlNewText();
                    if (StrUtil.isEmpty(text) || text.equals("-") || text.equals("+")) {
                        // this.setValue(0L);
                        return change;
                    }
                    // 判断数字
                    Long l = this.converter.fromString(text);
                    if (l == null) {
                        return null;
                    }
                    // 判断最大值
                    if (this.max != null && l > this.max) {
                        this.setValue(this.max);
                        return null;
                    }
                    // 判断最小值
                    if (this.min != null && l < this.min) {
                        this.setValue(this.min);
                        return null;
                    }
                } catch (Exception ignored) {
                }
            }
            return change;
        };
    }

    /**
     * 值变化
     *
     * @param newVal 新值
     */
    protected void valueChanged(Long newVal) {
        if (newVal != null) {
            NumberTextFieldSkin skin = (NumberTextFieldSkin) this.getSkin();
            if (this.min != null && newVal <= this.min) {
                skin.disableDecrButton();
            } else {
                skin.enableDecrButton();
            }
            if (this.max != null && newVal >= this.max) {
                skin.disableIncrButton();
            } else {
                skin.enableIncrButton();
            }
        }
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public Long getValue() {
        String text = this.getText();
        if (StrUtil.isEmpty(text) || "-".equals(text) || "+".equals(text)) {
            return 0L;
        }
        return NumberUtil.parseLong(text);
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(Long value) {
        if (value != null) {
            if (this.max != null && value > this.max) {
                value = this.max;
            } else if (this.min != null && value < this.min) {
                value = this.min;
            }
            this.textFormatter.setValue(value);
        }
    }

    /**
     * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
     *
     * @param value 值
     */
    public void setValue(long value) {
        this.setValue((Long) value);
    }
}
