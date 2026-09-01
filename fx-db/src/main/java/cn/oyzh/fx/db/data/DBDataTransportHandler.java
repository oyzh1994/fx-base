package cn.oyzh.fx.db.data;

import cn.oyzh.fx.db.DBDialect;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/06
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

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    public String getTargetDatabase() {
        return targetDatabase;
    }

    public void setTargetDatabase(String targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    public int getSelectLimit() {
        return selectLimit;
    }

    public void setSelectLimit(int selectLimit) {
        this.selectLimit = selectLimit;
    }

    @Override
    public int getInsertLimit() {
        return insertLimit;
    }

    public void setInsertLimit(int insertLimit) {
        this.insertLimit = insertLimit;
    }

    @Override
    public int getBatchLimit() {
        return batchLimit;
    }

    public void setBatchLimit(int batchLimit) {
        this.batchLimit = batchLimit;
    }

    public DBDialect getDialect() {
        return dialect;
    }

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

