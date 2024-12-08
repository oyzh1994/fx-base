package cn.oyzh.fx.gui.table;

import cn.oyzh.fx.plus.controls.table.FlexTableView;

/**
 * @author oyzh
 * @since 2022/1/18
 */
public class DynamicTableView<S> extends FlexTableView<S> {

    @Override
    public void initNode() {
        super.initNode();
        this.setTableMenuButtonVisible(true);
    }
}
