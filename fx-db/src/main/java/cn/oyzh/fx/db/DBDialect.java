package cn.oyzh.fx.db;


import com.alibaba.druid.DbType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据库类型(方言)
 *
 * @author oyzh
 * @since 2024/2/20
 */
public enum DBDialect {
    MYSQL,
    MONGODB,
    DAMENG,
    ;

    public DbType dbType() {
        switch (this) {
            case MYSQL:
                return DbType.mysql;
            case DAMENG:
                return DbType.dm;
            default:
                return null;
        }
    }

    public static List<DBDialect> valueList() {
        List<DBDialect> list = new ArrayList<>();
        Collections.addAll(list, values());
        return list;
    }

}
