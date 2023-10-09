package cn.oyzh.fx.plus.rich;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import javafx.beans.DefaultProperty;
import javafx.beans.NamedArg;
import javafx.scene.layout.Region;
import lombok.NonNull;
import org.fxmisc.flowless.Virtualized;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 * @author oyzh
 * @since 2023/10/7
 */
@DefaultProperty("content")
public class FlexVirtualizedScrollPane<V extends Region & Virtualized> extends VirtualizedScrollPane<V> implements FlexAdapter {

    public FlexVirtualizedScrollPane(@NamedArg("content") V content) {
        super(content);
        content.prefWidthProperty().bind(this.widthProperty());
        content.prefHeightProperty().bind(this.heightProperty());
    }

    @Override
    public void resize(double width, double height) {
        double computeWidth = this.computeWidth(width);
        double computeHeight = this.computeHeight(height);
        super.resize(computeWidth, computeHeight);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        this.resizeNode();
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
