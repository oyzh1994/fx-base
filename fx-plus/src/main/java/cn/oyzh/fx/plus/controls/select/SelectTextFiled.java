package cn.oyzh.fx.plus.controls.select;

import cn.oyzh.fx.plus.controls.textfield.LimitTextField;
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

    public void addData(String data) {
        if (this.getDataList() == null) {
            this.setDataList(new ArrayList<>());
        }
        this.getDataList().add(data);
    }

    public void setDataList(List<String> dataList) {
        this.skin().setDataList(dataList);
    }

    public List<String> getDataList() {
        return this.skin().getDataList();
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

    public int selectedIndex() {
        return this.skin().getSelectedIndex();
    }
}
