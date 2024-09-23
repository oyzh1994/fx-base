package cn.oyzh.fx.plus.store;

import cn.oyzh.fx.common.store.SqliteStore;
import cn.oyzh.fx.plus.domain.TreeGroup;

import java.util.HashMap;
import java.util.Map;


/**
 * @author oyzh
 * @since 2024/09/23
 */
public abstract class GroupStore<E extends TreeGroup> extends SqliteStore<E> {

    @Override
    protected TableDefinition getTableDefinition() {
        TableDefinition definition = new TableDefinition();
        definition.setTableName("t_group");
        ColumnDefinition gid = new ColumnDefinition();
        gid.setColumnName("gid");
        gid.setColumnType("text");
        gid.setPrimaryKey(true);
        ColumnDefinition name = new ColumnDefinition();
        name.setColumnName("name");
        name.setColumnType("text");
        ColumnDefinition extend = new ColumnDefinition();
        extend.setColumnName("extend");
        extend.setColumnType("integer");
        definition.addColumnDefinition(gid);
        definition.addColumnDefinition(name);
        definition.addColumnDefinition(extend);
        return definition;
    }

    @Override
    protected Map<String, Object> toRecord(E model) {
        Map<String, Object> record = new HashMap<>();
        record.put("gid", model.getGid());
        record.put("name", model.getName());
        record.put("extend", model.getExpand());
        return record;
    }

    @Override
    protected E toModel(Map<String, Object> record) {
        E model = (E) new TreeGroup();
        model.setGid((String) record.get("gid"));
        model.setName((String) record.get("name"));
        Object extend = record.get("extend");
        if (extend instanceof Number b) {
            model.setExpand(b.intValue() == 1);
        }
        return model;
    }
}
