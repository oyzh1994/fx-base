package cn.oyzh.fx.common.jdbc;

import cn.hutool.core.lang.Assert;
import lombok.Getter;

import java.sql.Connection;
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

    public JdbcConn(Connection connection) {
        Assert.notNull(connection);
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

}
