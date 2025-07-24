package cn.oyzh.fx.gui.text.field;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.fx.gui.skin.SelectTextFiledSkin;
import javafx.beans.value.ChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/07/12
 */
public class SelectTextFiled<T> extends LimitTextField {

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public SelectTextFiledSkin<T> skin() {
        if (this.getSkin() == null) {
            this.setSkin(this.createDefaultSkin());
        }
        return (SelectTextFiledSkin) this.getSkin();
    }

    @Override
    protected SelectTextFiledSkin<T> createDefaultSkin() {
        return (SelectTextFiledSkin<T>) super.createDefaultSkin();
    }

    public void clearItem() {
        if (this.getItemList() != null) {
            this.getItemList().clear();
        }
    }

    public void addItem(T item) {
        if (this.getItemList() == null) {
            this.setItemList(new ArrayList<>());
        }
        this.getItemList().add(item);
    }

    public void setItemList(List<T> itemList) {
        this.skin().setItemList(itemList);
    }

    public List<T> getItemList() {
        return this.skin().getItemList();
    }

    public int getItemSize() {
        return this.skin().getItemSize();
    }

    public void setLineHeight(double lineHeight) {
        this.skin().setLineHeight(lineHeight);
    }

    public double getLineHeight() {
        return this.skin().getLineHeight();
    }

    public void selectIndexChanged(ChangeListener<Number> listener) {
        this.skin().selectIndexChanged(listener);
    }

    public void selectItem(T item) {
        this.skin().selectItem(item);
        this.skin().setTexting();
        this.text(item.toString());
        this.skin().clearTexting();
    }

    public void selectIndex(int index) {
        this.skin().selectIndex(index);
    }

    public T getSelectedItem() {
        return this.skin().getSelectedItem();
    }

    public void selectedItemChanged(ChangeListener<T> listener) {
        this.skin().selectItemChanged(listener);
    }

    /**
     * 文本变更事件
     *
     * @param newValue 新文本
     */
    protected void onTextChanged(String newValue) {
        if (this.skin().isTexting()) {
            this.skin().clearTexting();
            return;
        }
        if (!this.isFocused()) {
            return;
        }
    }

    @Override
    public void initNode() {
        super.initNode();
        this.addTextChangeListener((observableValue, s, t1) -> {
            TaskManager.startDelay(this.hashCode() + ":text:changed", () -> this.onTextChanged(t1), 10);
        });
    }
}
