package cn.oyzh.fx.plus.controls.tree.table;

import cn.oyzh.fx.plus.flex.FlexAdapter;

/**
 * @author oyzh
 * @since 2024-11-21
 */
public class FlexTreeTableColumn<S,T> extends FXTreeTableColumn<S,T> implements FlexAdapter {

    public FlexTreeTableColumn( ) {
        super();
    }

    public FlexTreeTableColumn(String text) {
        super(text);
    }

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
}
