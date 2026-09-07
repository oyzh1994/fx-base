package cn.oyzh.fx.db;

import cn.oyzh.common.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author oyzh
 * @since 2026-09-07
 */
public class DBRecordData extends HashMap<DBColumn, Object> {

    public Set<String> columns() {
        return super.keySet().parallelStream().map(DBColumn::getName).collect(Collectors.toSet());
    }

    public Set<String> notNullColumns() {
        Set<String> columns = this.columns();
        return columns.parallelStream().filter(this::hasValue).collect(Collectors.toSet());
    }

    public DBColumn column(String column) {
        for (DBColumn dbColumn : this.keySet()) {
            if (StringUtil.equalsAnyIgnoreCase(column, dbColumn.getName())) {
                return dbColumn;
            }
        }
        return null;
    }

    public boolean hasValue(String column) {
        return this.value(column) != null;
    }

    public Object value(String column) {
        for (Map.Entry<DBColumn, Object> entry : this.entrySet()) {
            if (StringUtil.equalsAnyIgnoreCase(column, entry.getKey().getName())) {
                return entry.getValue();
            }
        }
        return null;
    }


    public void remove(String column) {
        DBColumn dbColumn = this.column(column);
        if (dbColumn != null) {
            super.remove(dbColumn);
        }
    }

    public Set<Map.Entry<DBColumn, Object>> entries() {
        return super.entrySet();
    }

    public int columnSize() {
        return super.size();
    }

    public boolean notNull(String column) {
        return this.value(column) != null;
    }

    public boolean isTypeGeometry(String column) {
        DBColumn dbColumn = this.column(column);
        if (dbColumn == null) {
            return false;
        }
        return dbColumn.supportGeometry();
    }
}
