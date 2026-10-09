package cn.oyzh.fx.db;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据库类型(方言)
 *
 * @author oyzh
 * @since 2026-09-01
 */
public enum DBDialect {
    MYSQL,
    MARIADB,
    MONGODB,
    DAMENG,
    ;

    /**
     * 获取所有方言列表
     *
     * @return 方言列表
     */
    public static List<DBDialect> valueList() {
        List<DBDialect> list = new ArrayList<>();
        Collections.addAll(list, values());
        return list;
    }

}
