//
//package cn.oyzh.fx.plus.property;
//
//import cn.oyzh.fx.plus.format.DigitalFormat;
//import javafx.beans.property.ObjectPropertyBase;
//
//import java.math.BigDecimal;
//
//
///**
// *
// * @author oyzh
// * @since 2023/11/22
// */
//public class DigitalDecimalProperty extends ObjectPropertyBase<BigDecimal> {
//
//    /**
//     * 保留小数位数
//     */
//    private final int scaleLen;
//
//    public DigitalDecimalProperty(int scaleLen) {
//        this.scaleLen = scaleLen;
//    }
//
//    @Override
//    public Object getBean() {
//        return DigitalDecimalProperty.this;
//    }
//
//    public void setValue(Number n) {
//        if (n != null) {
//            super.setValue(this.format(n.doubleValue()));
//        }
//    }
//
//    public void setValue(Double d) {
//        if (d != null) {
//            super.setValue(this.format(d));
//        }
//    }
//
//    public Double getDouble() {
//        BigDecimal decimal = this.get();
//        if (decimal != null) {
//            return decimal.doubleValue();
//        }
//        return null;
//    }
//
//    private BigDecimal format(double d) {
//        String text = this.format().format(d);
//        return new BigDecimal(text.replaceAll(",", ""));
//    }
//
//    private DigitalFormat format;
//
//    private DigitalFormat format() {
//        if (this.format == null) {
//            this.format = new DigitalFormat(this.scaleLen);
//        }
//        return this.format;
//    }
//
//    @Override
//    public String getName() {
//        return "BigDecimal";
//    }
//}
