package cn.oyzh.fx.db.condition;

import cn.oyzh.fx.db.DBDialect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库条件管理器，负责按数据库方言注册、初始化并获取条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DBConditionManager {

    /**
     * 各数据库方言对应的条件初始化器
     */
    private static final Map<DBDialect, Runnable> INITIALIZERS = new ConcurrentHashMap<>();

    /**
     * 各数据库方言对应的条件列表
     */
    private static final Map<DBDialect, List<DBCondition>> CONDITIONS = new ConcurrentHashMap<>();

    /**
     * 注册条件初始化器
     *
     * @param dialect 数据库方言
     * @param func    初始化器
     */
    public static void registerInitializer(DBDialect dialect, Runnable func) {
        INITIALIZERS.put(dialect, func);
    }

    /**
     * 添加条件
     *
     * @param dialect   数据库方言
     * @param condition 条件
     * @throws NullPointerException 方言或条件为空时抛出
     */
    public static void putCondition(DBDialect dialect, DBCondition condition) {
        if (dialect == null) {
            throw new NullPointerException("dialect");
        }
        if (condition == null) {
            throw new NullPointerException("condition");
        }
        List<DBCondition> list = CONDITIONS.get(dialect);
        if (list == null) {
            list = new ArrayList<>();
            list.add(condition);
            CONDITIONS.put(dialect, list);
        } else {
            list.add(condition);
        }
    }

    /**
     * 获取条件列表，首次访问时触发该方言的初始化器
     *
     * @param dialect 数据库方言
     * @return 条件列表
     */
    public static List<DBCondition> conditions(DBDialect dialect) {
        synchronized (CONDITIONS) {
            if (!CONDITIONS.containsKey(dialect)) {
                Runnable func = INITIALIZERS.remove(dialect);
                if (func != null) {
                    func.run();
                }
            }
        }
        List<DBCondition> list = CONDITIONS.get(dialect);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }
}
