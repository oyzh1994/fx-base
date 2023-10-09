package cn.oyzh.fx.plus.ext;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.controls.BaseTextField;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.NumberStringConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.security.InvalidParameterException;
import java.util.function.UnaryOperator;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public abstract class BaseNumberTextField extends BaseTextField {

    /**
     * 最大值
     */
    protected Number max;

    /**
     * 最小值
     */
    protected Number min;

    /**
     * 递进值
     */
    protected Number step = 1;

    @Getter
    @Setter
    private boolean decimal;

    /**
     * 当前值
     */
    @Getter
    @Accessors(fluent = true)
    protected ObjectProperty<Number> valueProperty;

    /**
     * 数据转换器
     */
    protected final NumberStringConverter converter;

    /**
     * 文本格式器
     */
    protected final TextFormatter<Number> textFormatter;

    {
        this.setSkin(new NumberTextFieldSkin(this, this::incrValue, this::decrValue));
    }

    // 构造函数
    public BaseNumberTextField() {
        // 初始化转换器
        this.converter = new NumberStringConverterExt();
        // 创建TextFormatter对象
        this.textFormatter = new TextFormatter<>(this.converter, 0L, this.createFilter());
        // 将TextFormatter对象设置到文本字段中
        this.setTextFormatter(this.textFormatter);
        // 属性值
        this.valueProperty = this.textFormatter.valueProperty();
        // 监听值变化
        this.valueProperty.addListener((observableValue, number, t1) -> {
            if (this.min != null) {
                NumberTextFieldSkin skin = (NumberTextFieldSkin) this.getSkin();
                if (t1.doubleValue() <= this.min.doubleValue()) {
                    skin.disableDecrButton();
                } else {
                    skin.enableDecrButton();
                }
            }
            if (this.max != null) {
                NumberTextFieldSkin skin = (NumberTextFieldSkin) this.getSkin();
                if (t1.doubleValue() >= this.max.doubleValue()) {
                    skin.disableIncrButton();
                } else {
                    skin.enableIncrButton();
                }
            }
        });
    }

    /**
     * 创建一个过滤器，用于限制文本只能输入整数
     *
     * @return 过滤器
     */
    protected UnaryOperator<TextFormatter.Change> createFilter() {
        return change -> {
            if (change.isAdded() || change.isReplaced() || change.isContentChange()) {
                try {
                    String text = change.getControlNewText();
                    // 符号直接返回
                    if (text.equals("+") || text.equals("-")) {
                        return change;
                    }
                    // 判断数值是否合法
                    if (StrUtil.startWithAny(text, "+", "-") && text.charAt(1) == '0') {
                        return null;
                    }
                    if (text.charAt(0) == '0') {
                        return null;
                    }
                    // 判断是否数字
                    if (!NumberUtil.isNumber(text)) {
                        return null;
                    }
                    // 检查文本
                    if (this.min != null || this.max != null) {
                        try {
                            double val = NumberUtil.parseDouble(text);
                            // 判断最大值
                            if (this.max != null && val > this.max.doubleValue()) {
                                this.textFormatter.setValue(this.max);
                                return null;
                            }
                            // 判断最小值
                            if (this.min != null && val < this.min.doubleValue()) {
                                this.textFormatter.setValue(this.min);
                                return null;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception ignore) {
                    return null;
                }
            }
            return change;
        };
    }

    /**
     * 增加值
     */
    protected abstract void incrValue();

    /**
     * 减少值
     */
    protected abstract void decrValue();
}
