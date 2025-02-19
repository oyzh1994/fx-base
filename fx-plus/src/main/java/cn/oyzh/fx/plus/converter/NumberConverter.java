//package cn.oyzh.fx.plus.converter;
//
//import cn.oyzh.common.util.NumberUtil;
//import cn.oyzh.common.util.RegexUtil;
//import javafx.util.StringConverter;
//
///**
// * @author oyzh
// * @since 2023/12/28
// */
//public class NumberConverter extends StringConverter<Number> {
//
//    @Override
//    public String toString(Number var1) {
//        return var1 == null ? "" : var1.toString();
//    }
//
//    @Override
//    public Number fromString(String s) {
//        if (RegexUtil.isNumber(s) || RegexUtil.isDecimal(s)) {
//            return NumberUtil.parseNumber(s);
//        }
//        return null;
//    }
//}
