//package cn.oyzh.fx.plus.property;
//
//import cn.oyzh.common.util.NumberUtil;
//import javafx.beans.property.SimpleObjectProperty;
//import javafx.beans.value.ObservableValue;
//import javafx.scene.control.TableColumn;
//import javafx.scene.control.cell.PropertyValueFactory;
//
///**
// * double属性工厂
// *
// * @author oyzh
// * @since 2023/11/22
// */
//public class ScaleDoublePropertyValueFactory<S> extends PropertyValueFactory<S, Double> {
//
//    /**
//     * 保留小数位数
//     */
//    private final int scaleLen;
//
//    public ScaleDoublePropertyValueFactory(String property, int scaleLen) {
//        super(property);
//        this.scaleLen = scaleLen;
//    }
//
//    @Override
//    public ObservableValue<Double> call(TableColumn.CellDataFeatures<S, Double> param) {
//        // 调用父类的call方法，获取原始的属性值
//        ObservableValue<Double> originalValue = super.call(param);
//        // 判断原始的属性值是否为空
//        if (originalValue != null) {
//            SimpleObjectProperty<Double> property = new SimpleObjectProperty<>() {
//                @Override
//                public Double get() {
//                    Double d = super.get();
//                    if (d != null) {
//                        return NumberUtil.round(d, scaleLen);
//                    }
//                    return null;
//                }
//            };
//            property.bind(originalValue);
//            return property;
//        }
//        return null;
//    }
//}
