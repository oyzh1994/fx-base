package cn.oyzh.fx.db.condition;

import cn.oyzh.fx.db.DBDialect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 条件工具类
 *
 * @author oyzh
 * @since 2024/6/26
 */
public class DBConditionManager {

    private static final Map<DBDialect, Runnable> INITIALIZERS = new ConcurrentHashMap<>();

    private static final Map<DBDialect, List<DBCondition>> CONDITIONS = new ConcurrentHashMap<>();

    public static void registerInitializer(DBDialect dialect, Runnable func) {
        INITIALIZERS.put(dialect, func);
    }

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
     * 获取条件
     *
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
