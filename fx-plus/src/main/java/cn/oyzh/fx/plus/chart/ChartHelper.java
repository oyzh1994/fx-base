package cn.oyzh.fx.plus.chart;

import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.chart.XYChart;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * 图表辅助工具
 *
 * @author oyzh
 * @since 2023/8/2
 */
@UtilityClass
public class ChartHelper {

    /**
     * 添加数据
     *
     * @param series 数据组件
     * @param x      x轴数据
     * @param y      y轴数据
     * @param limit  限制数据量
     * @param <X>    泛型x
     * @param <Y>    泛型y
     */
    public static <X, Y> void addData(XYChart.Series<X, Y> series, X x, Y y, Integer limit) {
        FXUtil.runWait(() -> {
            series.getData().add(new XYChart.Data<>(x, y));
            clipData(series, limit);
        });
    }

    /**
     * 添加或者更新数据
     *
     * @param series 数据组件
     * @param x      x轴数据
     * @param y      y轴数据
     * @param limit  限制数据量
     * @param <X>    泛型x
     * @param <Y>    泛型y
     */
    public static <X, Y> void addOrUpdateData(XYChart.Series<X, Y> series, X x, Y y, Integer limit) {
        Optional<XYChart.Data<X, Y>> optional = series.getData().parallelStream().filter(p -> p.getXValue().equals(x)).findAny();
        if (optional.isEmpty()) {
            FXUtil.runWait(() -> {
                series.getData().add(new XYChart.Data<>(x, y));
                clipData(series, limit);
            });
        } else {
            FXUtil.runWait(() -> optional.get().setYValue(y));
        }
    }

    /**
     * 裁剪数据
     *
     * @param series 数据组件
     * @param limit  限制数据量
     * @param <X>    泛型x
     * @param <Y>    泛型y
     */
    private static <X, Y> void clipData(XYChart.Series<X, Y> series, Integer limit) {
        if (limit != null && series.getData().size() > limit) {
            int len = series.getData().size() - limit;
            for (int i = 0; i < len; i++) {
                series.getData().remove(i);
            }
        }
    }
}
