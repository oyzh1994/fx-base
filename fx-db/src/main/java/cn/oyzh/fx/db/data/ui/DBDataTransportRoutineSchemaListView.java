package cn.oyzh.fx.db.data.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.db.DBRoutineSchema;
import cn.oyzh.fx.db.data.dto.DBDataTransportObject;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class DBDataTransportRoutineSchemaListView extends DBDataTransportObjectListView {

    public void of(List<? extends DBRoutineSchema> routineSchemas) {
        List<DBDataTransportObject> list = CollectionUtil.newArrayList();
        for (DBRoutineSchema function : routineSchemas) {
            DBDataTransportObject obj = new DBDataTransportObject();
            obj.setName(function.getName());
            list.add(obj);
        }
        this.init(list);
    }
}
