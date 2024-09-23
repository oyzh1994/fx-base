package cn.oyzh.fx.common.store;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.log.StaticLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024-09-23
 */
public abstract class SqliteStore<M extends Serializable> {

    /**
     * 文件位置
     */
    @Getter
    protected static String filePath;

    public static void initStore(String filePath) throws Exception {
        SqliteStore.filePath = filePath;
        synchronized (SqliteStore.class) {
            Connection connection = getConnection();
            connection.close();
        }
    }

    protected static synchronized Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + filePath);
    }

    protected String uid() {
        return UUID.fastUUID().toString(true);
    }

    protected abstract TableDefinition getTableDefinition();

    protected abstract M toModel(Map<String, Object> record);

    protected abstract Map<String, Object> toRecord(M model);

    private PrimaryKeyColumn getPrimaryKeyColumn(Object primaryKey) {
        TableDefinition tableDefinition = this.getTableDefinition();
        String primaryKeyName = tableDefinition.primaryKeyColumn();
        if (primaryKeyName != null) {
            return new PrimaryKeyColumn(primaryKeyName, primaryKey);
        }
        return null;
    }

    /**
     * 执行初始化
     *
     * @return 结果
     */
    protected synchronized boolean init() throws Exception {
        try {
            TableDefinition definition = getTableDefinition();
            String tableName = definition.getTableName();
            Connection conn = this.getConnection();
            String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, tableName);
            ResultSet resultSet = statement.executeQuery();
            boolean exists = resultSet.next();
            resultSet.close();
            statement.close();
            StringBuilder sql1 = new StringBuilder();
            if (exists) {
                sql1.append("ALTER TABLE ").append(tableName);
                boolean changed = false;
                for (ColumnDefinition columnDefinition : definition.getColumnDefinitions()) {
                    ResultSet resultSet1 = conn.getMetaData().getColumns(null, null, tableName, columnDefinition.columnName);
                    if (!resultSet1.next()) {
                        sql1.append(" ADD COLUMN ")
                                .append(columnDefinition.columnName)
                                .append(" ")
                                .append(columnDefinition.columnType);
                        if (columnDefinition.primaryKey) {
                            sql1.append(" primary key");
                        }
                        sql1.append(",");
                        changed = true;
                    }
                    resultSet1.close();
                }
                if (changed) {
                    sql1.deleteCharAt(sql1.length() - 1);
                } else {
                    sql1.delete(0, sql1.length());
                }
            } else {
                sql1.append("CREATE TABLE ").append(tableName).append(" (");
                for (ColumnDefinition columnDefinition : definition.getColumnDefinitions()) {
                    sql1.append(columnDefinition.columnName)
                            .append(" ")
                            .append(columnDefinition.columnType);
                    if (columnDefinition.primaryKey) {
                        sql1.append(" primary key");
                    }
                    sql1.append(",");
                }
                sql1.deleteCharAt(sql1.length() - 1);
                sql1.append(")");
            }
            if (!sql1.isEmpty()) {
                StaticLog.info(sql1.toString());
                PreparedStatement statement1 = conn.prepareStatement(sql1.toString());
                statement1.executeUpdate();
                statement1.close();
            }
            conn.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public SqliteStore() {
        try {
            this.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int insert(Map<String, Object> record) throws Exception {
        TableDefinition tableDefinition = getTableDefinition();
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName);
        sql.append("(");
        for (String column : record.keySet()) {
            sql.append(column).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES(");
        sql.append("?,".repeat(record.size()));
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        StaticLog.info(sql.toString());
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql.toString());
        int index = 1;
        for (Object value : record.values()) {
            if (value == null) {
                statement.setNull(index++, JDBCType.NULL.ordinal());
            } else {
                statement.setObject(index++, value);
            }
        }
        StaticLog.info(sql.toString());
        int update = statement.executeUpdate();
        statement.close();
        connection.close();
        return update;
    }

    public int update(Map<String, Object> record, Object primaryKey) throws Exception {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn == null ? 0 : this.update(record, primaryKeyColumn);
    }

    public int update(Map<String, Object> record, PrimaryKeyColumn primaryKey) throws Exception {
        record.remove(primaryKey.columnName);
        TableDefinition tableDefinition = getTableDefinition();
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName);
        sql.append(" SET ");
        for (String column : record.keySet()) {
            sql.append(column).append("=?,");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" WHERE ");
        sql.append(primaryKey.columnName);
        sql.append("=?");
        StaticLog.info(sql.toString());
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql.toString());
            int index = 1;
            for (String key : record.keySet()) {
                Object value = record.get(key);
                if (value == null) {
                    statement.setNull(index++, JDBCType.NULL.ordinal());
                } else {
                    statement.setObject(index++, value);
                }
            }
            statement.setObject(index, primaryKey.columnData);
            int update = statement.executeUpdate();
            statement.close();
            connection.close();
            return update;
        } finally {
            IoUtil.close(connection);
            IoUtil.close(statement);
        }
    }

    public boolean exist(Object primaryKey) throws Exception {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn != null && this.exist(primaryKeyColumn);
    }

    public boolean exist(PrimaryKeyColumn primaryKey) throws Exception {
        TableDefinition tableDefinition = getTableDefinition();
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(primaryKey.columnName);
        sql.append("=?");
        StaticLog.info(sql.toString());
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setObject(1, primaryKey.columnData);
        ResultSet resultSet = statement.executeQuery();
        boolean exists = false;
        if (resultSet.next()) {
            exists = resultSet.getInt(1) > 0;
        }
        resultSet.close();
        statement.close();
        connection.close();
        return exists;
    }

    public boolean exist(Map<String, Object> params) throws SQLException {
        TableDefinition tableDefinition = getTableDefinition();
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableName);
        if (CollUtil.isEmpty(params)) {
            boolean first = true;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (first) {
                    first = false;
                    sql.append(" WHERE");
                } else {
                    sql.append(" AND ");
                }
                sql.append(entry.getKey());
                sql.append("=");
                sql.append(entry.getValue());
            }
        }
        StaticLog.info(sql.toString());
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql.toString());
        ResultSet resultSet = statement.executeQuery();
        boolean exists = false;
        if (resultSet.next()) {
            exists = resultSet.getInt(1) > 0;
        }
        resultSet.close();
        statement.close();
        connection.close();
        return exists;
    }

    public Map<String, Object> selectOne(Object primaryKey) throws Exception {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn == null ? null : this.selectOne(primaryKeyColumn);
    }

    public Map<String, Object> selectOne(PrimaryKeyColumn primaryKey) throws Exception {
        TableDefinition tableDefinition = getTableDefinition();
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(primaryKey.columnName);
        sql.append("=?");
        StaticLog.info(sql.toString());
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setObject(1, primaryKey.columnData);
        ResultSet resultSet = statement.executeQuery();
        Map<String, Object> record = new HashMap<>();
        while (resultSet.next()) {
            for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitions) {
                String columnName = columnDefinition.columnName;
                if (resultSet.findColumn(columnName) >= 0) {
                    record.put(columnName, resultSet.getObject(columnName));
                }
            }
        }
        resultSet.close();
        statement.close();
        connection.close();
        return record;
    }

    public List<Map<String, Object>> selectList(Map<String, Object> params) throws SQLException {
        TableDefinition tableDefinition = getTableDefinition();
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableName);
        if (CollUtil.isNotEmpty(params)) {
            boolean first = true;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (first) {
                    first = false;
                    sql.append(" WHERE");
                } else {
                    sql.append(" AND ");
                }
                sql.append(entry.getKey());
                sql.append("=");
                sql.append(entry.getValue());
            }
        }
        StaticLog.info(sql.toString());
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql.toString());
        ResultSet resultSet = statement.executeQuery();
        List<Map<String, Object>> records = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> record = new HashMap<>();
            for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitions) {
                String columnName = columnDefinition.columnName;
                if (resultSet.findColumn(columnName) >= 0) {
                    record.put(columnName, resultSet.getObject(columnName));
                }
            }
            records.add(record);
        }
        resultSet.close();
        statement.close();
        connection.close();
        return records;
    }

    public int delete(Object primaryKey) throws Exception {
        PrimaryKeyColumn primaryKeyColumn = this.getPrimaryKeyColumn(primaryKey);
        return primaryKeyColumn == null ? 0 : this.delete(primaryKeyColumn);
    }

    public int delete(PrimaryKeyColumn primaryKey) throws Exception {
        TableDefinition tableDefinition = getTableDefinition();
        String tableName = tableDefinition.getTableName();
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");
        sql.append(primaryKey.columnName);
        sql.append("=?");
        StaticLog.info(sql.toString());
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql.toString());
        statement.setObject(1, primaryKey.columnData);
        int update = statement.executeUpdate();
        statement.close();
        connection.close();
        return update;
    }

    @Data
    public static class TableDefinition {

        private String tableName;

        private List<ColumnDefinition> columnDefinitions;

        public void addColumnDefinition(ColumnDefinition columnDefinition) {
            if (this.columnDefinitions == null) {
                this.columnDefinitions = new ArrayList<>();
            }
            this.columnDefinitions.add(columnDefinition);
        }

        public String primaryKeyColumn() {
            for (ColumnDefinition columnDefinition : columnDefinitions) {
                if (columnDefinition.primaryKey) {
                    return columnDefinition.columnName;
                }
            }
            return null;
        }
    }

    @Data
    public static class ColumnDefinition {

        private String columnName;

        private String columnType;

        private boolean primaryKey;

        private boolean autoIncrement;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKeyColumn {

        private String columnName;

        private Object columnData;

    }
}
