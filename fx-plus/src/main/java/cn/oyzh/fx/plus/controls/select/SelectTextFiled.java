package cn.oyzh.fx.plus.controls.select;

import cn.oyzh.fx.plus.controls.textfield.LimitTextField;
import cn.oyzh.fx.plus.skin.SelectTextFiledSkin;
import javafx.beans.value.ChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/07/12
 */
public class SelectTextFiled extends LimitTextField {

    {
        this.setSkin(new SelectTextFiledSkin(this));
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public SelectTextFiledSkin skin() {
        return (SelectTextFiledSkin) this.getSkin();
    }

    public void clearData( ) {
        if (this.getItemList() != null) {
            this.getItemList().clear();
        }
    }

    public void addData(String data) {
        if (this.getItemList() == null) {
            this.setItemList(new ArrayList<>());
        }
        this.getItemList().add(data);
    }

    public void setItemList(List<String> itemList) {
        this.skin().setItemList(itemList);
    }

    public List<String> getItemList() {
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
        this.skin().setSelectIndexChanged(listener);
    }

    public void selectItem(String item) {
        this.skin().selectItem(item);
    }

    public void selectIndex(int index) {
        this.skin().selectIndex(index);
    }
}
