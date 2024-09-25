package cn.oyzh.fx.common.sqlite;

import cn.hutool.core.util.ReflectUtil;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@Data
public class TableDefinition {

    private String tableName;

    private List<ColumnDefinition> columns;

    public void addColumn(ColumnDefinition columnDefinition) {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        this.columns.add(columnDefinition);
    }

    public ColumnDefinition primaryKeyColumn() {
        for (ColumnDefinition columnDefinition : columns) {
            if (columnDefinition.isPrimaryKey()) {
                return columnDefinition;
            }
        }
        return null;
    }

    public String primaryKeyName() {
        ColumnDefinition primaryKeyColumn = this.primaryKeyColumn();
        return primaryKeyColumn == null ? null : primaryKeyColumn.getColumnName();
    }

    public static TableDefinition ofClass(Class<?> clazz) {
        if (clazz != null) {
            TableDefinition definition = new TableDefinition();
            Table table = clazz.getAnnotation(Table.class);
            if (table != null) {
                if (table.value().isEmpty()) {
                    definition.tableName = clazz.getSimpleName().toLowerCase();
                } else {
                    definition.tableName = table.value();
                }
            }
            Field[] fields = ReflectUtil.getFieldsDirectly(clazz, true);
            for (Field field : fields) {
                ColumnDefinition columnDefinition =ColumnDefinition.ofField(field);
                if (columnDefinition!=null){
                    definition.addColumn(columnDefinition);
                }
            }
            return definition;
        }
        return null;
    }
}
