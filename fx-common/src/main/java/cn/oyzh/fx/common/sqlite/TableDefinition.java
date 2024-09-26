package cn.oyzh.fx.common.sqlite;


import cn.oyzh.fx.common.util.ReflectUtil;
import lombok.Data;

import java.lang.reflect.Field;
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

    public ColumnDefinition primaryKey() {
        for (ColumnDefinition columnDefinition : columns) {
            if (columnDefinition.isPrimaryKey()) {
                return columnDefinition;
            }
        }
        return null;
    }

    public String primaryKeyName() {
        ColumnDefinition primaryKeyColumn = this.primaryKey();
        return primaryKeyColumn == null ? null : primaryKeyColumn.getColumnName();
    }

    public PrimaryKeyColumn primaryKeyColumn(Object model) throws IllegalAccessException {
        ColumnDefinition column = this.primaryKey();
        if (column != null) {
            Field field = ReflectUtil.getField(model.getClass(), column.getFieldName(), true);
            field.setAccessible(true);
            Object primaryVal = field.get(model);
            return new PrimaryKeyColumn(column.getColumnName(), primaryVal);
        }
        return null;
    }

    public void handlePrimaryKeyValue(Object model) throws IllegalAccessException {
        ColumnDefinition columnDefinition = this.primaryKey();
        if (columnDefinition != null) {
            Field field = ReflectUtil.getField(model.getClass(), columnDefinition.getFieldName(), true);
            field.setAccessible(true);
            Object primaryVal = field.get(model);
            if (primaryVal == null) {
                if (columnDefinition.isAutoGeneration()) {
                    primaryVal = KeyGenerator.generatorKey(columnDefinition.getColumnType());
                }
                if (primaryVal != null) {
                    field.set(model, primaryVal);
                }
            }
        }
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
            Field[] fields = ReflectUtil.getFields(clazz, true);
            for (Field field : fields) {
                ColumnDefinition columnDefinition = ColumnDefinition.ofField(field);
                if (columnDefinition != null) {
                    definition.addColumn(columnDefinition);
                }
            }
            return definition;
        }
        return null;
    }
}
