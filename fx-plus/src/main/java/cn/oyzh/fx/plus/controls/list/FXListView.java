package cn.oyzh.fx.plus.controls.list;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.ListView;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2023/4/24
 */
public class FXListView<T> extends ListView<T> implements NodeAdapter, TipAdapter, StateAdapter, ThemeAdapter, LayoutAdapter, FontAdapter, SelectAdapter<T> {

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
    public void selectedItemChanged(@NonNull ChangeListener<T> listener) {
        this.getSelectionModel().selectedItemProperty().addListener((observableValue, t, t1) -> {
            if (!this.isIgnoreChanged()) {
                listener.changed(observableValue, t, t1);
            }
        });
    }
}
