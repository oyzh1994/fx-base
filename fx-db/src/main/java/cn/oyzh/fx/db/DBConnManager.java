package cn.oyzh.fx.db;


import cn.oyzh.common.log.JulLog;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 数据库连接管理器，负责维护与获取各类数据库连接
 *
 * @author oyzh
 * @since 2026-09-01
 */
public abstract class DBConnManager implements AutoCloseable {

    /**
     * 连接配置
     */
    protected DBConnConfig config;

    /**
     * 库连接
     */
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();

    /**
     * 添加连接
     *
     * @param dbName     数据库
     * @param connection 连接
     */
    public void addConnection(String dbName, Connection connection) {
        this.connections.put("db_connection_" + dbName, connection);
    }

    /**
     * 添加函数连接
     *
     * @param dbName     数据库
     * @param connection 连接
     */
    public void addFunctionConnection(String dbName, Connection connection) {
        this.connections.put("function_connection_" + dbName, connection);
    }

    /**
     * 添加过程连接
     *
     * @param dbName     数据库
     * @param connection 连接
     */
    public void addProcedureConnection(String dbName, Connection connection) {
        this.connections.put("procedure_connection_" + dbName, connection);
    }

    /**
     * 获取连接
     *
     * @param dbName 数据库
     * @return 结果
     */
    public Connection getConnection(String dbName) {
        return this.connections.get("db_connection_" + dbName);
    }

    /**
     * 获取函数连接
     *
     * @param dbName 数据库
     * @return 结果
     */
    public Connection getFunctionConnection(String dbName) {
        return this.connections.get("function_connection_" + dbName);
    }

    /**
     * 获取过程连接
     *
     * @param dbName 数据库
     * @return 结果
     */
    public Connection getProcedureConnection(String dbName) {
        return this.connections.get("procedure_connection_" + dbName);
    }

    @Override
    public void close() {
        for (Connection connection : this.connections.values()) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
        this.connections.clear();
    }

    /**
     * 获取服务连接
     *
     * @return 服务连接
     */
    public Connection getServerConnection() {
        return this.connections.get("server_connection");
    }

    /**
     * 设置服务连接
     *
     * @param serverConnection 服务连接
     */
    public void setServerConnection(Connection serverConnection) {
        this.connections.put("server_connection", serverConnection);
    }

    /**
     * 获取连接列表
     *
     * @return 连接列表
     */
    public Map<String, Connection> getConnections() {
        return connections;
    }

    /**
     * 是否有效
     *
     * @param connection 连接
     * @return 结果
     * @throws SQLException 异常
     */
    public boolean isValid(Connection connection) throws SQLException {
        if (connection == null || connection.isClosed()) {
            return false;
        }
        return connection.isValid(this.getConnectTimeout() / 1000);
    }

    /**
     * 执行连接
     *
     * @return 结果
     * @throws SQLException           异常
     * @throws ClassNotFoundException 异常
     */
    public Connection connection() throws Exception {
        if (this.config == null) {
            return null;
        }
        Connection connection = this.getServerConnection();
        if (!this.isValid(connection)) {
            connection = this.initConnection(null, this.config.getUser(), this.config.getPassword());
            this.setServerConnection(connection);
        }
        return connection;
    }

    /**
     * 执行连接
     *
     * @param name 名称
     * @return 结果
     * @throws SQLException           异常
     * @throws ClassNotFoundException 异常
     */
    public Connection connection(String name) throws Exception {
        Connection connection = this.getConnection(name);
        if (!this.isValid(connection)) {
            connection = this.initConnection(name, this.config.getUser(), this.config.getPassword());
            this.addConnection(name, connection);
            JulLog.info("initConnection for {}", name);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    /**
     * 执行函数连接
     *
     * @param dbName 数据库
     * @return 结果
     * @throws Exception 异常
     */
    public Connection functionConnection(String dbName) throws Exception {
        Connection connection = this.getFunctionConnection(dbName);
        if (!this.isValid(connection)) {
            connection = this.initConnection(dbName, this.config.getUser(), this.config.getPassword());
            this.addFunctionConnection(dbName, connection);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    /**
     * 执行过程连接
     *
     * @param name 名称
     * @return 结果
     * @throws Exception 异常
     */
    public Connection procedureConnection(String name) throws Exception {
        Connection connection = this.getProcedureConnection(name);
        if (!this.isValid(connection)) {
            connection = this.initConnection(name, this.config.getUser(), this.config.getPassword());
            this.addProcedureConnection(name, connection);
        }
        connection.setAutoCommit(true);
        return connection;
    }

    /**
     * 执行新连接
     *
     * @param name 名称
     * @return 结果
     * @throws Exception 异常
     */
    public Connection newConnection(String name) throws Exception {
        Connection connection = this.initConnection(name, this.config.getUser(), this.config.getPassword());
        connection.setAutoCommit(true);
        return connection;
    }

    /**
     * 初始化连接
     *
     * @param name     名称
     * @param user     用户名
     * @param password 密码
     * @return 结果
     * @throws Exception 异常
     */
    public abstract Connection initConnection(String name, String user, String password) throws Exception;

    /**
     * 获取连接字符串
     *
     * @return 结果
     */
    public abstract String getConnectionString();

    /**
     * 获取配置。
     *
     * @return 配置
     */
    public DBConnConfig getConfig() {
        return config;
    }

    /**
     * 设置配置。
     *
     * @param config 配置
     */
    public void setConfig(DBConnConfig config) {
        this.config = config;
    }

    /**
     * 获取连接超时。
     *
     * @return 连接超时
     */
    public int getConnectTimeout() {
        return this.config.getConnectTimeout();
    }

    /**
     * 设置连接超时。
     *
     * @param connectTimeout 连接超时
     */
    public void setConnectTimeout(int connectTimeout) {
        this.config.setConnectTimeout(connectTimeout);
    }

}
