package cn.oyzh.fx.db.data.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.DBName;
import cn.oyzh.fx.db.DBRoutineSchema;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;

import java.util.List;

/**
 * 数据传输名称列表视图，用于展示并勾选待传输的名称集合
 *
 * @author oyzh
 * @since 2026-09-07
 */
public class DBDataTransportNameListView extends DBDataTransportObjectListView {

    /**
     * 根据名称集合初始化列表
     *
     * @param names 名称集合
     */
    public void of(List<? extends DBName> names) {
        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
        for (DBName name : names) {
            DBDataTransportObject obj = new DBDataTransportObject();
            obj.setName(name.getName());
            list.add(obj);
        }
        this.init(list);
    }
}
