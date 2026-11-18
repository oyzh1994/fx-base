package cn.oyzh.fx.db.sql;

import cn.oyzh.common.db.OracleSqlDialect;
import cn.oyzh.common.db.SqlDialects;

/**
 * Oracle分页SQL生成器，兼容11g的ROWNUM写法及12c+的OFFSET/FETCH写法。
 *
 * @author oyzh
 * @since 2026-11-18
 */
public final class DBOraclePagination {

    /** 11g兼容分页产生的伪列名称。 */
    public static final String ROW_NUMBER_ALIAS = OracleSqlDialect.PAGE_ROWNUM_ALIAS;

    private DBOraclePagination() {
    }

    /**
     * 生成Oracle分页SQL。
     *
     * @param sql           原始查询
     * @param offset        跳过的行数
     * @param limit         返回的行数
     * @param productVersion Oracle产品版本
     * @return 分页SQL
     */
    public static String paginate(String sql, long offset, long limit, String productVersion) {
        OracleSqlDialect dialect = (OracleSqlDialect) SqlDialects.get(cn.oyzh.common.db.SqlDatabase.ORACLE);
        return dialect.paginate(sql, offset, limit, dialect.supportsOffsetFetch(productVersion));
    }

    /**
     * 判断产品版本是否支持OFFSET/FETCH。
     *
     * @param productVersion Oracle产品版本
     * @return 是否支持
     */
    public static boolean supportsOffsetFetch(String productVersion) {
        return ((OracleSqlDialect) SqlDialects.get(cn.oyzh.common.db.SqlDatabase.ORACLE))
                .supportsOffsetFetch(productVersion);
    }
}
