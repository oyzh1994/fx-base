// package cn.oyzh.fx.plus.controls.select;
//
// import cn.hutool.core.util.NumberUtil;
// import cn.hutool.core.util.StrUtil;
// import cn.oyzh.fx.plus.controls.textfield.LimitTextField;
// import cn.oyzh.fx.plus.converter.DigitalFormatStringConverter;
// import cn.oyzh.fx.plus.skin.DigitalTextFieldSkin;
// import javafx.scene.control.TextFormatter;
// import lombok.Getter;
// import lombok.Setter;
//
// import java.util.function.UnaryOperator;
//
// /**
//  * 数字文本输入框
//  *
//  * @author oyzh
//  * @since 2023/12/27
//  */
// public abstract class DigitalSelectTextField extends SelectTextFiled {
//
//     /**
//      * 最大值
//      */
//     @Setter
//     @Getter
//     protected Number maxVal;
//
//     /**
//      * 最小值
//      */
//     @Setter
//     @Getter
//     protected Number minVal;
//
//     /**
//      * 递进值
//      */
//     @Getter
//     @Setter
//     protected Number step = 1L;
//
//     /**
//      * 无符号模式
//      */
//     @Getter
//     @Setter
//     private boolean unsigned;
//
//     /**
//      * 文本格式器
//      */
//     protected final TextFormatter<String> textFormatter;
//
//     public DigitalSelectTextField(boolean unsigned, Long maxLen) {
//         // 创建文本格式化器
//         this.textFormatter = new TextFormatter<>(this.getConverter(), null, this.createFilter());
//         // 将TextFormatter对象设置到文本字段中
//         this.setTextFormatter(this.textFormatter);
//         this.setMaxLen(maxLen);
//         this.setUnsigned(unsigned);
//     }
//
//     protected abstract DigitalFormatStringConverter getConverter();
//
//     /**
//      * 获取byte值
//      *
//      * @return byte值
//      */
//     public Byte getByteValue() {
//         Number val = this.value();
//         return val == null ? null : val.byteValue();
//     }
//
//     /**
//      * 获取short值
//      *
//      * @return short值
//      */
//     public Short getShortValue() {
//         Number val = this.value();
//         return val == null ? null : val.shortValue();
//     }
//
//     /**
//      * 获取int值
//      *
//      * @return int值
//      */
//     public Integer getIntValue() {
//         Number val = this.value();
//         return val == null ? null : val.intValue();
//     }
//
//     /**
//      * 获取long值
//      *
//      * @return long值
//      */
//     public Long getLongValue() {
//         Number val = this.value();
//         return val == null ? null : val.longValue();
//     }
//
//     /**
//      * 获取float值
//      *
//      * @return float值
//      */
//     public Float getFloatValue() {
//         Number val = this.value();
//         return val == null ? null : val.floatValue();
//     }
//
//     /**
//      * 获取double值
//      *
//      * @return double值
//      */
//     public Double getDoubleValue() {
//         Number val = this.value();
//         return val == null ? null : val.doubleValue();
//     }
//
//     /**
//      * 创建一个过滤器，用于限制文本输入
//      *
//      * @return 过滤器
//      */
//     protected abstract UnaryOperator<TextFormatter.Change> createFilter();
//
//     /**
//      * 获取值
//      *
//      * @return 值
//      */
//     protected Number value() {
//         // 否则，将文本转为Double类型并返回
//         return NumberUtil.parseNumber(this.getText());
//     }
//
//     /**
//      * 设置值，如果超出最大值或最小值，将设置为最大值或最小值
//      *
//      * @param value 值
//      */
//     protected void value(Number value) {
//         if (value != null) {
//             if (this.maxVal != null && value.doubleValue() > this.maxVal.doubleValue()) {
//                 value = this.maxVal;
//             } else if (this.minVal != null && value.doubleValue() < this.minVal.doubleValue()) {
//                 value = this.minVal;
//             }
//             this.textFormatter.setValue(value.toString());
//         }
//     }
//
//     public void setValue(Object value) {
//         if (value != null) {
//             if (value instanceof Number number) {
//                 this.value(number);
//             } else if (value instanceof CharSequence sequence) {
//                 this.value(NumberUtil.parseNumber(sequence.toString()));
//             }
//         }
//     }
// }
