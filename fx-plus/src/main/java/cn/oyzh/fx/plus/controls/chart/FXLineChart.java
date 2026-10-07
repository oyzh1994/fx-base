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
 * 折线图控件，继承自 LineChart，支持主题、字体、提示等适配
 *
 * @author oyzh
 * @since 2023/8/2
 */
public class FXLineChart<X, Y> extends LineChart<X, Y> implements FlexAdapter, TipAdapter, FontAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 构造行图表对象。
     *
     * @param xAxis xAxis
     * @param yAxis yAxis
     */
    public FXLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }

    /**
     * 构造行图表对象。
     *
     * @param xAxis xAxis
     * @param yAxis yAxis
     * @param data 数据
     */
    public FXLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis, @NamedArg("data") ObservableList<Series<X, Y>> data) {
        super(xAxis, yAxis, data);
    }

    /**
     * 添加图表数据系列（在 FX 线程中执行）
     *
     * @param series 数据系列
     */
    public void addChartData(Series<X, Y> series) {
        FXUtil.runWait(() -> this.getData().add(series));
    }

    /**
     * 设置图表数据系列（在 FX 线程中执行）
     *
     * @param series 数据系列集合
     */
    public void setChartData(Collection<Series<X, Y>> series) {
        FXUtil.runWait(() -> this.getData().setAll(series));
    }

    /**
     * 获取指定索引的图表数据系列
     *
     * @param index 索引
     * @return 数据系列，不存在时返回 null
     */
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
