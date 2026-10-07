package cn.oyzh.fx.db.data.handler;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.DataBatchInsertable;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库数据传输处理器抽象基类，负责在来源库与目标库之间传输数据
 *
 * @author oyzh
 * @since 2026-09-01
 */
public abstract class DBDataTransportHandler<D> extends DataTransportHandler implements DataBatchInsertable<D> {

    /**
     * 来源库
     */
    protected String sourceDatabase;

    /**
     * 目标库
     */
    protected String targetDatabase;

    /**
     * 插入限制，insertLimit/batchLimit=连接数，mysql默认是151，尽量不要超过连接数
     */
    protected int insertLimit = 5000;

    /**
     * 查询限制，selectLimit/batchLimit=连接数，mysql默认是151，尽量不要超过连接数
     */
    protected int selectLimit = 5000;

    /**
     * 批量限制
     */
    protected int batchLimit = 50;

    /**
     * 方言
     */
    protected DBDialect dialect;

    /**
     * 插入集合
     */
    protected List<D> insertList;

    @Override
    public List<D> getInsertList() {
        if (this.insertList == null) {
            this.insertList = new ArrayList<>();
        }
        return this.insertList;
    }

    /**
     * 构造数据库数据传输处理器
     *
     * @param dialect 数据库方言
     */
    public DBDataTransportHandler(DBDialect dialect) {
        this.dialect = dialect;
    }

    /**
     * 获取源数据库。
     *
     * @return 源数据库
     */
    public String getSourceDatabase() {
        return sourceDatabase;
    }

    /**
     * 设置源数据库。
     *
     * @param sourceDatabase 源数据库
     */
    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    /**
     * 获取目标数据库。
     *
     * @return 目标数据库
     */
    public String getTargetDatabase() {
        return targetDatabase;
    }

    /**
     * 设置目标数据库。
     *
     * @param targetDatabase 目标数据库
     */
    public void setTargetDatabase(String targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    /**
     * 获取选取限制。
     *
     * @return 选取限制
     */
    public int getSelectLimit() {
        return selectLimit;
    }

    /**
     * 设置选取限制。
     *
     * @param selectLimit 查询限制
     */
    public void setSelectLimit(int selectLimit) {
        this.selectLimit = selectLimit;
    }

    @Override
    public int getInsertLimit() {
        return insertLimit;
    }

    /**
     * 设置插入限制。
     *
     * @param insertLimit 插入限制
     */
    public void setInsertLimit(int insertLimit) {
        this.insertLimit = insertLimit;
    }

    @Override
    public int getBatchLimit() {
        return batchLimit;
    }

    /**
     * 设置批量限制。
     *
     * @param batchLimit 批量限制
     */
    public void setBatchLimit(int batchLimit) {
        this.batchLimit = batchLimit;
    }

    /**
     * 获取方言。
     *
     * @return 方言
     */
    public DBDialect getDialect() {
        return dialect;
    }

    /**
     * 设置方言。
     *
     * @param dialect 方言
     */
    public void setDialect(DBDialect dialect) {
        this.dialect = dialect;
    }

    /**
     * 执行传输
     *
     * @throws Exception 异常
     */
    public abstract void doTransport() throws Exception;
}

