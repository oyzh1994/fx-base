package cn.oyzh.fx.db.query;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * @author oyzh
 * @since 2024/08/19
 */
public abstract class DBQueryResult {

    /**
     * 内容
     */
    protected String content;

    /**
     * 耗时，微妙
     */
    protected long used;

    /**
     * 消息
     */
    protected String msg;

    /**
     * 变更总数
     */
    protected long updateCount;

    /**
     * 是否成功
     */
    protected boolean success;

    /**
     * 获取数量
     *
     * @return 结果
     */
    public abstract int getCount();

    /**
     * 是否有结果
     *
     * @return 结果
     */
    public boolean hasResult() {
        return this.updateCount <= 0 && this.getCount() > 0;
    }

    /**
     * 解析结果
     *
     * @param resultSet  resultSet
     * @param connection 连接
     * @throws Exception 异常
     */
    public void parseResult(ResultSet resultSet, Connection connection) throws Exception {
        this.parseResult(resultSet, connection, true);
    }

    /**
     * 解析结果
     *
     * @param resultSet  resultSet
     * @param connection 连接
     * @param readonly   只读模式
     * @throws Exception 异常
     */
    public abstract void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception;

    public long getUsedMs() {
        return this.used / 1_000_000L;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(long updateCount) {
        this.updateCount = updateCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
