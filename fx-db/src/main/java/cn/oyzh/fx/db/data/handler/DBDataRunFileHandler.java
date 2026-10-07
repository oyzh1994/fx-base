package cn.oyzh.fx.db.data.handler;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.data.DataBatchInsertable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库文件运行处理器抽象基类，负责执行 SQL 文件等
 *
 * @author oyzh
 * @since 2024-08-29
 */
public abstract class DBDataRunFileHandler<D> extends DataRunFileHandler implements DataBatchInsertable<D> {

    /**
     * 库名称
     */
    protected String dbName;

    /**
     * 文件
     */
    protected File file;

    /**
     * 插入限制，insertLimit/batchLimit=连接数，mysql默认是151，尽量不要超过连接数
     */
    protected int insertLimit = 5000;

    /**
     * 批量限制
     */
    protected int batchLimit = 50;

    /**
     * 遇到错误时继续
     */
    protected boolean continueWithErrors = true;

    /**
     * 方言
     */
    protected DBDialect dialect;

    /**
     * 构造数据库文件运行处理器
     *
     * @param dbName 库名称
     */
    public DBDataRunFileHandler(String dbName) {
        this.dbName = dbName;
    }

    /**
     * 设置文件
     *
     * @param file sql文件
     * @return 当前对象
     */
    public DBDataRunFileHandler<D> file(File file) {
        this.file = file;
        return this;
    }

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
     * 获取数据库名称。
     *
     * @return 数据库名称
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * 设置数据库名称。
     *
     * @param dbName 数据库名称
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * 获取文件。
     *
     * @return 文件
     */
    public File getFile() {
        return file;
    }

    /**
     * 设置文件。
     *
     * @param file 文件
     */
    public void setFile(File file) {
        this.file = file;
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

    /**
     * 是否继续错误集合。
     *
     * @return 继续错误集合
     */
    public boolean isContinueWithErrors() {
        return continueWithErrors;
    }

    /**
     * 设置继续错误集合。
     *
     * @param continueWithErrors 继续错误集合
     */
    public void setContinueWithErrors(boolean continueWithErrors) {
        this.continueWithErrors = continueWithErrors;
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
     * 运行文件
     *
     * @throws Exception 异常
     */
    public abstract void runFile() throws Exception ;
}

