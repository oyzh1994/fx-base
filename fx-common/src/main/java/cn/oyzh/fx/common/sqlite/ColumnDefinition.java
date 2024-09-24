package cn.oyzh.fx.common.sqlite;

import lombok.Data;

@Data
public class ColumnDefinition {

    private String fieldName;

    private String columnName;

    private String columnType;

    private boolean primaryKey;

    private boolean autoGeneration;
}
