package cn.oyzh.fx.common.jdbc;

import lombok.Getter;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author oyzh
 * @since 2024-09-25
 */
public class JdbcConn {

    /**
     * 0 正常
     * 1 使用中
     * 2 已关闭
     */
    private final AtomicInteger status;

    /**
     * 连接
     */
    @Getter
    private final Connection connection;

    public JdbcConn(@NonNull Connection connection) {
        this.connection = connection;
        this.status = new AtomicInteger(0);
    }

    /**
     * 是否可用
     *
     * @return 结果
     */
    public boolean isUsable() {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                this.status.set(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.status.get() == 1;
    }

    /**
     * 获取连接
     *
     * @return Connection
     */
    public Connection takeoff() {
        this.status.set(1);
        return this.connection;
    }

    /**
     * 归还连接
     */
    public void giveback() {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                this.status.set(2);
            } else {
                this.status.set(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 是否失效
     *
     * @return 结果
     */
    public boolean isInvalid() {
        return this.status.get() == 2;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return this.connection.prepareStatement(sql);
    }

    public Statement createStatement() throws SQLException {
        return this.connection.createStatement();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return this.connection.getMetaData();
    }

    public ResultSet getColumns(String tableNamePattern, String columnNamePattern) throws SQLException {
        return this.getColumns(null, null, tableNamePattern, columnNamePattern);
    }

    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        if (JdbcManager.isH2Dialect()) {
            if (tableNamePattern != null) {
                tableNamePattern = tableNamePattern.toUpperCase();
            }
            if (columnNamePattern != null) {
                columnNamePattern = columnNamePattern.toUpperCase();
            }
        }
        return this.getMetaData().getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
    }

    public ResultSet getTables(String tableNamePattern) throws SQLException {
        return this.getTables(null, null, tableNamePattern, null);
    }

    public ResultSet getTables(String tableNamePattern, String[] types) throws SQLException {
        return this.getTables(null, null, tableNamePattern, types);
    }

    public ResultSet getTables(String catalog, String schemaPattern,String tableNamePattern, String[] types) throws SQLException {
        if (JdbcManager.isH2Dialect()) {
            if (tableNamePattern != null) {
                tableNamePattern = tableNamePattern.toUpperCase();
            }
        }
        return this.getMetaData().getTables(catalog, schemaPattern, tableNamePattern, types);
    }
}
