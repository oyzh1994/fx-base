package cn.oyzh.fx.common.jdbc;

import cn.oyzh.fx.common.h2.H2Util;
import cn.oyzh.fx.common.sqlite.SqlLiteUtil;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Data
public class ColumnDefinition {

    private String fieldName;

    private String columnName;

    private String columnType;

    private boolean primaryKey;

    private boolean autoGeneration;

    public static ColumnDefinition ofField(Field field) {
        if (field != null) {
            int modifiers = field.getModifiers();
            if (!Modifier.isPrivate(modifiers) || Modifier.isStatic(modifiers)) {
                return null;
            }
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                return null;
            }
            ColumnDefinition columnDefinition = new ColumnDefinition();
            if (column.value().isEmpty()) {
                columnDefinition.setColumnName(field.getName());
            } else {
                columnDefinition.setColumnName(column.value());
            }
            if (column.type().isEmpty()) {
                String columnType = "";
                if (JdbcManager.dialect == JdbcDialect.H2) {
                    columnType = H2Util.toSqlType(field.getType());
                } else if (JdbcManager.dialect == JdbcDialect.SQLITE) {
                    columnType = SqlLiteUtil.toSqlType(field.getType());
                }
                columnDefinition.setColumnType(columnType);
            } else {
                columnDefinition.setColumnType(column.type());
            }
            columnDefinition.setFieldName(field.getName());
            PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
            if (primaryKey != null) {
                columnDefinition.setPrimaryKey(true);
                columnDefinition.setAutoGeneration(primaryKey.autoGeneration());
            }
            return columnDefinition;
        }
        return null;
    }

    public String getColumnName() {
        if (JdbcManager.dialect == JdbcDialect.H2) {
            return this.columnName.toUpperCase();
        }
        return this.columnName;
    }
}
