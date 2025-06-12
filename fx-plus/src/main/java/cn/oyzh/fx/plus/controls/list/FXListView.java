package cn.oyzh.fx.plus.controls.list;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.ListView;

/**
 * @author oyzh
 * @since 2023/4/24
 */
public class FXListView<T> extends ListView<T> implements FlexAdapter, NodeAdapter, TipAdapter, StateAdapter, ThemeAdapter, LayoutAdapter, FontAdapter, SelectAdapter<T> {

    {
        NodeManager.init(this);
    }

    @Override
    public void initNode() {
        this.setCursor(Cursor.HAND);
    }

    /**
     * 选中内容变化事件
     *
     * @param listener 监听器
     */
    public void selectedItemChanged(ChangeListener<T> listener) {
        this.getSelectionModel().selectedItemProperty().addListener((observableValue, t, t1) -> {
            if (!this.isIgnoreChanged()) {
                listener.changed(observableValue, t, t1);
            }
        });
    }

    public void selectIndex(int index) {
        this.getSelectionModel().select(index);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }
}
