package cn.oyzh.fx.plus.controls.chart;

import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.NamedArg;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;

/**
 * @author oyzh
 * @since 2023/8/2
 */
public class FlexLineChart<X, Y> extends LineChart<X, Y> implements FlexAdapter, TipAdapter, FontAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    public FlexLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }

    public FlexLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis, @NamedArg("data") ObservableList<Series<X, Y>> data) {
        super(xAxis, yAxis, data);
    }

    public void addChartData(Series<X, Y> series) {
        FXUtil.runWait(() -> this.getData().add(series));
    }

    public Series<X, Y> getChartData(int index) {
        if (!this.getData().isEmpty() && this.getData().size() > index) {
            return this.getData().get(index);
        }
        return null;
    }

    @Override
    public void initNode() {

    }
}
