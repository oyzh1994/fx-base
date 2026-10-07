package cn.oyzh.fx.db.data.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据传输对象列表视图，以复选框形式展示数据传输对象并支持勾选
 *
 * @author oyzh
 * @since 2024-09-05
 */
public class DBDataTransportObjectListView extends FXListView<FXCheckBox> {

    /**
     * 选中项变更回调
     */
    protected Runnable selectedChanged;

    /**
     * 初始化列表数据
     *
     * @param events 数据传输对象集合
     */
    public void init(List<DBDataTransportObject> events) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(events)) {
            for (DBDataTransportObject event : events) {
                FXCheckBox checkBox = new FXCheckBox();
                checkBox.setText(event.getName());
                checkBox.setSelected(event.isSelected());
                checkBox.setProp("data", event);
                checkBox.selectedChanged((observable, oldValue, newValue) -> {
                    event.setSelected(newValue);
                    if (this.selectedChanged != null) {
                        this.selectedChanged.run();
                    }
                });
                ListViewUtil.selectRowOnMouseClicked(checkBox);
                this.addItem(checkBox);
            }
        }
        if (this.selectedChanged != null) {
            this.selectedChanged.run();
        }
    }

    /**
     * 获取已选中的数据传输对象
     *
     * @return 已选中的数据传输对象集合
     */
    public List<DBDataTransportObject> getSelectedObjects() {
        List<DBDataTransportObject> list = new ArrayList<>();
        for (FXCheckBox item : this.getItems()) {
            if (item.isSelected()) {
                list.add(item.getProp("data"));
            }
        }
        return list;
    }

    /**
     * 获取已选中的数量
     *
     * @return 已选中数量
     */
    public int getSelectedSize() {
        int size = 0;
        for (FXCheckBox item : this.getItems()) {
            if (item.isSelected()) {
                size++;
            }
        }
        return size;
    }

    /**
     * 获取选中变更。
     *
     * @return 选中变更
     */
    public Runnable getSelectedChanged() {
        return selectedChanged;
    }

    /**
     * 设置选中变更。
     *
     * @param selectedChanged 选中变更
     */
    public void setSelectedChanged(Runnable selectedChanged) {
        this.selectedChanged = selectedChanged;
    }
}
