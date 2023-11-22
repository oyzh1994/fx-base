package cn.oyzh.fx.plus.property;

import cn.hutool.core.util.NumberUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * double属性工厂
 *
 * @author oyzh
 * @since 2023/11/22
 */
public class DoublePropertyValueFactory<S> extends PropertyValueFactory<S, Double> {

    private final Integer scaleLen;

    public DoublePropertyValueFactory(String property, int scaleLen) {
        super(property);
        this.scaleLen = scaleLen;
    }

    @Override
    public ObservableValue<Double> call(TableColumn.CellDataFeatures<S, Double> param) {
        if (this.scaleLen == null) {
            return super.call(param);
        }
        // 调用父类的call方法，获取原始的属性值
        ObservableValue<Double> originalValue = super.call(param);
        // 判断原始的属性值是否为空
        if (originalValue == null) {
            // 如果为空，直接返回null
            return null;
        }
        SimpleObjectProperty<Double> property = new SimpleObjectProperty<>() {
            @Override
            public Double get() {
                Double d = super.get();
                if (d != null) {
                    return NumberUtil.round(d, scaleLen).doubleValue();
                }
                return d;
            }
        };
        property.bind(originalValue);
        return property;

    }


}
