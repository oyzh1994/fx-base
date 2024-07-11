package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.TableViewUtil;
import javafx.scene.control.TableView;

/**
 * @author oyzh
 * @since 2022/1/18
 */
public class FXTableView<S> extends TableView<S> implements NodeAdapter, ThemeAdapter, SelectAdapter<S> {

    {
        NodeManager.init(this);
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
    public void setInitIndex(int initIndex) {
        SelectAdapter.super.setInitIndex(initIndex);
    }

    @Override
    public int getInitIndex() {
        return SelectAdapter.super.getInitIndex();
    }

    @Override
    public void initNode() {
        this.setFixedCellSize(35.f);
        this.setFocusTraversable(false);
        this.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }
}
