package cn.oyzh.fx.db;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL生成器基类，负责收集与拼接SQL语句
 *
 * @author oyzh
 * @since 2026-09-01
 */
public class DBSqlGenerator {

    /**
     * SQL列表
     */
    protected List<String> sqlList = new ArrayList<>();

    /**
     * SQL构建器
     */
    protected StringBuilder sqlBuilder = new StringBuilder();

    /**
     * 构建SQL列表，并将构建器中的内容置于列表首位
     *
     * @return SQL列表
     */
    protected List<String> buildSql() {
        if (this.sqlBuilder != null && !this.sqlBuilder.isEmpty()) {
            this.sqlList.addFirst(this.sqlBuilder.toString().trim());
        }
        return this.sqlList;
    }

    /**
     * 构建单条SQL，将构建器内容与列表中已有的SQL拼接
     *
     * @return 单条SQL
     */
    protected String buildSqlSingle() {
        StringBuilder builder = new StringBuilder();
        if (this.sqlBuilder != null && !this.sqlBuilder.isEmpty()) {
            builder.append(this.sqlBuilder.toString().trim());
        }
        for (String sql : this.sqlList) {
            builder.append("\n").append(sql);
        }
        return builder.toString().trim();
    }
}

