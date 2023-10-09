package cn.oyzh.fx.plus.chart;

import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.NamedArg;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2023/8/2
 */
public class FlexLineChart<X, Y> extends LineChart<X, Y> implements FlexAdapter, TipAdapter, FontAdapter {

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
        if (!this.getData().isEmpty()) {
            return this.getData().get(index);
        }
        return null;
    }

    @Override
    public void setTipText(String tipTitle) {
        TipAdapter.super._setTipText(tipTitle);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super._getTipText();
    }

    @Override
    public String getFlexWidth() {
        return FlexAdapter.super._getFlexWidth();
    }

    @Override
    public void setFlexWidth(String flexWidth) {
        FlexAdapter.super._setFlexWidth(flexWidth);
    }

    public String getFlexHeight() {
        return FlexAdapter.super._getFlexHeight();
    }

    @Override
    public void setFlexHeight(String flexHeight) {
        FlexAdapter.super._setFlexHeight(flexHeight);
    }

    @Override
    public String getFlexX() {
        return FlexAdapter.super._getFlexX();
    }

    @Override
    public void setFlexX(String flexX) {
        FlexAdapter.super._setFlexX(flexX);
    }

    @Override
    public String getFlexY() {
        return FlexAdapter.super._getFlexY();
    }

    @Override
    public void setFlexY(String flexY) {
        FlexAdapter.super._setFlexY(flexY);
    }

    @Override
    public void setFontSize(double fontSize) {
        FontAdapter.super._setFontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return FontAdapter.super._getFontSize();
    }

    @Override
    public String getFontFamily() {
        return FontAdapter.super._getFontFamily();
    }

    @Override
    public void setFontFamily(@NonNull String fontFamily) {
        FontAdapter.super._setFontFamily(fontFamily);
    }

    @Override
    public double getRealWidth() {
        return FlexAdapter.super._getRealWidth();
    }

    @Override
    public void setRealWidth(double width) {
        FlexAdapter.super._setRealWidth(width);
    }

    @Override
    public double getRealHeight() {
        return FlexAdapter.super._getRealHeight();
    }

    @Override
    public void setRealHeight(double height) {
        FlexAdapter.super._setRealHeight(height);
    }
}
