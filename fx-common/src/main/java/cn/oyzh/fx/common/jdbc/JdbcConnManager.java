package cn.oyzh.fx.common.jdbc;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2024-09-25
 */
@UtilityClass
public class JdbcConnManager {

    /**
     * db文件
     */
    @Getter
    private static String dbFile;

    /**
     * 连接列表
     */
    private static final List<JdbcConn> CONNECTIONS = new CopyOnWriteArrayList<>();

    public static void initDb(String dbFile) throws Exception {
        JdbcConnManager.dbFile = dbFile;
        takeoff();
    }

    /**
     * 获取连接
     *
     * @return Connection
     * @throws SQLException 异常
     */
    public static Connection takeoff() throws SQLException {
        for (JdbcConn connection : CONNECTIONS) {
            if (connection.isUsable()) {
                return connection.takeoff();
            }
        }
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
        CONNECTIONS.add(new JdbcConn(connection));
        return connection;
    }

    /**
     * 归还连接
     *
     * @param connection 连接
     */
    public static void giveback(Connection connection) {
        if (connection != null) {
            List<JdbcConn> invalid = null;
            for (JdbcConn sqlConnection : CONNECTIONS) {
                if (sqlConnection.isInvalid()) {
                    if (invalid == null) {
                        invalid = new ArrayList<>();
                    }
                    invalid.add(sqlConnection);
                }
                if (sqlConnection.getConnection() == connection) {
                    sqlConnection.giveback();
                    break;
                }
            }
            if (CollUtil.isNotEmpty(invalid)) {
                CONNECTIONS.removeAll(invalid);
            }
        }
    }
}
