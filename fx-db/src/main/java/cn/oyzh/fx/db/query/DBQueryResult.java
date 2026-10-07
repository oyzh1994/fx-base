package cn.oyzh.fx.db.query;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * 查询结果基类，封装查询内容、耗时、变更数量与执行状态等信息
 *
 * @author oyzh
 * @since 2024-08-19
 */
public abstract class DBQueryResult {

    /**
     * 内容
     */
    protected String content;

    /**
     * 耗时，纳秒
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
     * @param resultSet  结果集
     * @param connection 连接
     * @throws Exception 异常
     */
    public void parseResult(ResultSet resultSet, Connection connection) throws Exception {
        this.parseResult(resultSet, connection, true);
    }

    /**
     * 解析结果
     *
     * @param resultSet  结果集
     * @param connection 连接
     * @param readonly   只读模式
     * @throws Exception 异常
     */
    public abstract void parseResult(ResultSet resultSet, Connection connection, boolean readonly) throws Exception;

    /**
     * 获取耗时
     *
     * @return 耗时，毫秒
     */
    public long getUsedMs() {
        return this.used / 1_000_000L;
    }

    /**
     * 获取内容。
     *
     * @return 内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置内容。
     *
     * @param content 内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取已用。
     *
     * @return 已用
     */
    public long getUsed() {
        return used;
    }

    /**
     * 设置已用。
     *
     * @param used 已用
     */
    public void setUsed(long used) {
        this.used = used;
    }

    /**
     * 获取消息。
     *
     * @return 消息
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置消息。
     *
     * @param msg 消息
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 获取更新数量。
     *
     * @return 更新数量
     */
    public long getUpdateCount() {
        return updateCount;
    }

    /**
     * 设置更新数量。
     *
     * @param updateCount 更新数量
     */
    public void setUpdateCount(long updateCount) {
        this.updateCount = updateCount;
    }

    /**
     * 是否成功。
     *
     * @return 成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置成功。
     *
     * @param success 成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
