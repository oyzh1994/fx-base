package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import javafx.beans.value.ChangeListener;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FlexCheckBox extends FXCheckBox implements FlexAdapter {

    public FlexCheckBox() {
        super();
    }

    public FlexCheckBox(String text) {
        super(text);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    /**
     * 选中变更事件
     *
     * @param listener 监听器
     */
    public void selectedChanged(@NonNull ChangeListener<Boolean> listener) {
        this.selectedProperty().addListener(listener);
    }

    @Override
    public String getFlexWidth() {
        return FlexAdapter.super.flexWidth();
    }

    @Override
    public void setFlexWidth(String flexWidth) {
        FlexAdapter.super.flexWidth(flexWidth);
    }

    public String getFlexHeight() {
        return FlexAdapter.super.flexHeight();
    }

    @Override
    public void setFlexHeight(String flexHeight) {
        FlexAdapter.super.flexHeight(flexHeight);
    }

    @Override
    public String getFlexX() {
        return FlexAdapter.super.flexX();
    }

    @Override
    public void setFlexX(String flexX) {
        FlexAdapter.super.flexX(flexX);
    }

    @Override
    public String getFlexY() {
        return FlexAdapter.super.flexY();
    }

    @Override
    public void setFlexY(String flexY) {
        FlexAdapter.super.flexY(flexY);
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
}
