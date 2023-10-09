package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.util.TableViewUtil;
import javafx.scene.CacheHint;
import javafx.scene.control.TableView;

/**
 * @author oyzh
 * @since 2022/1/18
 */
public class FlexTableView<S> extends TableView<S> implements FlexAdapter, SelectAdapter<S> {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
        this.setFixedCellSize(35.f);
        this.setFocusTraversable(false);
        this.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        // this.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    /**
     * 获取当前选中列的数据
     *
     * @return 数据
     */
    public Object getSelectCellData() {
        return TableViewUtil.getSelectCellData(this);
    }

    @Override
    public void resize(double width, double height) {
        double computeWidth = this.computeWidth(width);
        double computeHeight = this.computeHeight(height);
        super.resize(computeWidth, computeHeight);
//        this.resizeNode();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        this.resizeNode();
    }

    @Override
    public void setInitIndex(int initIndex) {
        SelectAdapter.super.setInitIndex(initIndex);
    }

    @Override
    public int getInitIndex() {
        return SelectAdapter.super.getInitIndex();
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
