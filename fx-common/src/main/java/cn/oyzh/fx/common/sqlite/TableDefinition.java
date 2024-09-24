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

    private List<ColumnDefinition> columnDefinitions;

    public void addColumnDefinition(ColumnDefinition columnDefinition) {
        if (this.columnDefinitions == null) {
            this.columnDefinitions = new ArrayList<>();
        }
        this.columnDefinitions.add(columnDefinition);
    }

    public ColumnDefinition primaryKeyColumn() {
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            if (columnDefinition.isPrimaryKey()) {
                return columnDefinition;
            }
        }
        return null;
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
                int modifiers = field.getModifiers();
                if (!Modifier.isPrivate(modifiers)) {
                    continue;
                }
                Column column = field.getAnnotation(Column.class);
                if (column == null) {
                    continue;
                }
                ColumnDefinition columnDefinition = new ColumnDefinition();
                if (column.value().isEmpty()) {
                    columnDefinition.setColumnName(field.getName());
                } else {
                    columnDefinition.setColumnName(column.value());
                }
                if (column.type().isEmpty()) {
                    columnDefinition.setColumnType(SqlLiteUtil.toSqlType(field));
                } else {
                    columnDefinition.setColumnType(column.type());
                }
                columnDefinition.setFieldName(field.getName());
                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                if (primaryKey != null) {
                    columnDefinition.setPrimaryKey(true);
                    columnDefinition.setAutoGeneration(primaryKey.autoGeneration());
                }
                definition.addColumnDefinition(columnDefinition);
            }
            return definition;
        }
        return null;
    }
}
