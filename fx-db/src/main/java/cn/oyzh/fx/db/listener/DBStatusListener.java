package cn.oyzh.fx.db.listener;

import cn.oyzh.common.object.Destroyable;
import javafx.beans.value.ChangeListener;

import java.util.UUID;

/**
 * 数据库对象状态监听器抽象基类，维护监听器键值并在销毁时自动从管理器中移除
 *
 * @author oyzh
 * @since 2026-09-01
 */
public abstract class DBStatusListener implements ChangeListener<Object>, Destroyable {

    /**
     * 监听器键值
     */
    private final String key;

    /**
     * 无参构造器，使用随机 UUID 作为监听器键值。
     */
    public DBStatusListener() {
        this(UUID.randomUUID().toString());
    }

    /**
     * 以指定键值创建监听器并注册到管理器
     *
     * @param key 监听器键值
     */
    public DBStatusListener(String key) {
        this.key = key;
        DBStatusListenerManager.addListener(this);
    }

    /**
     * 以库名、表名作为键值创建监听器
     *
     * @param dbName    库名
     * @param tableName 表名
     */
    public DBStatusListener(String dbName, String tableName) {
        this(dbName + ":" + tableName);
    }

    /**
     * 以库名、模式名、表名作为键值创建监听器
     *
     * @param dbName    库名
     * @param schema    模式名
     * @param tableName 表名
     */
    public DBStatusListener(String dbName, String schema, String tableName) {
        this(dbName + ":" + schema + ":" + tableName);
    }

    //    @Override
    //    protected void finalize() throws Throwable {
    //        super.finalize();
    //        DBStatusListenerManager.removeListener(this);
    //    }

    @Override
    public void destroy() {
        DBStatusListenerManager.removeListener(this);
    }

    /**
     * 获取监听器键值
     *
     * @return 监听器键值
     */
    public String getKey() {
        return key;
    }
}
