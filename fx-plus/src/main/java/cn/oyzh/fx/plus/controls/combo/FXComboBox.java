package cn.oyzh.fx.plus.controls.combo;

import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.validator.Verifiable;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Collection;

/**
 * @author oyzh
 * @since 2023/12/25
 */
public class FXComboBox<T> extends ComboBox<T> implements NodeGroup, NodeAdapter, ThemeAdapter, Verifiable, SelectAdapter<T>, TipAdapter, StateAdapter, FontAdapter, LayoutAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 是否必须
     */
    @Setter
    @Getter
    private boolean require;
//
//    @Getter
//    @Setter
//    private BaseValidator validator = new BaseValidator(this);
//
//    public void setRequire(Boolean require) {
//        this.require = require;
//        this.validator.addRequiredVerifier(require, Integer.MIN_VALUE);
//    }

    @Override
    public boolean validate() {
        if (this.require && this.getSelectedItem() == null) {
            this.requestFocus();
            return false;
        }
        return Verifiable.super.validate();
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

    /**
     * 是否包含数据
     *
     * @param item 数据
     */
    public boolean containsItem(@NonNull T item) {
        return this.getItems().contains(item);
    }

    /**
     * 添加多个数据
     *
     * @param collection 数据集合
     */
    public void addItems(@NonNull Collection<T> collection) {
        FXUtil.runWait(() -> this.getItems().addAll(collection));
    }

    /**
     * 添加多个数据
     *
     * @param items 数据数组
     */
    public void addItems(@NonNull T[] items) {
        FXUtil.runWait(() -> this.getItems().addAll(items));
    }

    @Override
    public void initNode() {
        this.setPickOnBounds(true);
        this.setCursor(Cursor.HAND);
//        this.setFocusTraversable(false);
    }
}
