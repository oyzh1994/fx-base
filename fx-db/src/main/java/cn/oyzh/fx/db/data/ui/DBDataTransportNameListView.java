package cn.oyzh.fx.db.data.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.DBName;
import cn.oyzh.fx.db.DBRoutineSchema;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class DBDataTransportNameListView extends DBDataTransportObjectListView {

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
