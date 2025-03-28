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

import java.util.Collection;

/**
 * @author oyzh
 * @since 2023/8/2
 */
public class FXLineChart<X, Y> extends LineChart<X, Y> implements FlexAdapter, TipAdapter, FontAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    public FXLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }

    public FXLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis, @NamedArg("data") ObservableList<Series<X, Y>> data) {
        super(xAxis, yAxis, data);
    }

    public void addChartData(Series<X, Y> series) {
        FXUtil.runWait(() -> this.getData().add(series));
    }

    public void setChartData(Collection<Series<X, Y>> series) {
        FXUtil.runWait(() -> this.getData().setAll(series));
    }

    public Series<X, Y> getChartData(int index) {
        if (!this.getData().isEmpty() && this.getData().size() > index) {
            return this.getData().get(index);
        }
        return null;
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }
}
