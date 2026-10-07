package cn.oyzh.fx.db.query;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询结果集合，聚合同一次执行的多个查询结果及错误信息
 *
 * @author oyzh
 * @since 2024-02-19
 */
public class DBQueryResults<R extends DBQueryResult> {

    /**
     * 错误信息
     */
    private String errMsg;

    /**
     * 查询结果列表
     */
    private List<R> results;

    /**
     * 添加查询结果
     *
     * @param result 查询结果
     */
    public void addResult(R result) {
        if (this.results == null) {
            this.results = new ArrayList<>();
        }
        this.results.add(result);
    }

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return CollectionUtil.isEmpty(this.results);
    }

    /**
     * 是否执行成功
     *
     * @return 结果
     */
    public boolean isSuccess() {
        return StringUtil.isEmpty(this.errMsg);
    }

    /**
     * 解析错误信息
     *
     * @param ex 异常
     */
    public void parseError(Exception ex) {
        this.errMsg = ex.getMessage();
    }

    /**
     * 获取错误消息。
     *
     * @return 错误消息
     */
    public String getErrMsg() {
        return errMsg;
    }

    /**
     * 设置错误消息。
     *
     * @param errMsg 错误消息
     */
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    /**
     * 获取结果集合。
     *
     * @return 结果集合
     */
    public List<R> getResults() {
        return results;
    }

    /**
     * 设置结果集合。
     *
     * @param results 结果集合
     */
    public void setResults(List<R> results) {
        this.results = results;
    }
}
