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
import javafx.scene.text.FontWeight;
import lombok.NonNull;

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
        if (!this.getData().isEmpty()) {
            return this.getData().get(index);
        }
        return null;
    }

//    @Override
//    public void setTipText(String tipText) {
//        TipAdapter.super.tipText(tipText);
//    }
//
//    @Override
//    public String getTipText() {
//        return TipAdapter.super.tipText();
//    }
//
//    @Override
//    public String getFlexWidth() {
//        return FlexAdapter.super.flexWidth();
//    }
//
//    @Override
//    public void setFlexWidth(String flexWidth) {
//        FlexAdapter.super.flexWidth(flexWidth);
//    }
//
//    public String getFlexHeight() {
//        return FlexAdapter.super.flexHeight();
//    }
//
//    @Override
//    public void setFlexHeight(String flexHeight) {
//        FlexAdapter.super.flexHeight(flexHeight);
//    }
//
//    @Override
//    public String getFlexX() {
//        return FlexAdapter.super.flexX();
//    }
//
//    @Override
//    public void setFlexX(String flexX) {
//        FlexAdapter.super.flexX(flexX);
//    }
//
//    @Override
//    public String getFlexY() {
//        return FlexAdapter.super.flexY();
//    }
//
//    @Override
//    public void setFlexY(String flexY) {
//        FlexAdapter.super.flexY(flexY);
//    }

    @Override
    public void setFontSize(double fontSize) {
        FontAdapter.super.fontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return FontAdapter.super.fontSize();
    }

    @Override
    public String getFontFamily() {
        return FontAdapter.super.fontFamily();
    }

    @Override
    public void setFontFamily(@NonNull String fontFamily) {
        FontAdapter.super.fontFamily(fontFamily);
    }

    @Override
    public void setFontWeight(FontWeight fontWeight) {
        FontAdapter.super.fontWeight(fontWeight);
    }

    @Override
    public FontWeight getFontWeight() {
        return FontAdapter.super.fontWeight();
    }

    @Override
    public double getRealWidth() {
        return FlexAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        FlexAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return FlexAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        FlexAdapter.super.realHeight(height);
    }

    // @Override
    // public void setStateManager(StateManager manager) {
    //     FlexAdapter.super.stateManager(manager);
    // }
    //
    // @Override
    // public StateManager getStateManager() {
    //     return FlexAdapter.super.stateManager();
    // }

    @Override
    public void initNode() {

    }
}
