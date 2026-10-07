package cn.oyzh.fx.db.data.handler;

import cn.oyzh.common.file.FastFileWriter;
import cn.oyzh.fx.db.DBDialect;

import java.io.File;
import java.io.IOException;

/**
 * 数据库数据转储处理器抽象基类，负责将库或表的数据和结构转储到文件
 *
 * @author oyzh
 * @since 2024-08-22
 */
public abstract class DBDataDumpHandler extends DataDumpHandler {

    /**
     * 数据类型
     * 0 数据和结构
     * 1 仅结构
     */
    protected Byte dataType;

    /**
     * 库名称
     */
    protected String dbName;

    /**
     * 转储文件
     */
    protected File dumpFile;

    /**
     * 文件写入器
     */
    protected FastFileWriter fileWriter;

    /**
     * 1. 库
     * 2. 表
     */
    protected Byte dumpType;

    /**
     * 表名称
     */
    protected String tableName;

    /**
     * 查询限制
     */
    protected int queryLimit = 1000;

    /**
     * 方言
     */
    protected DBDialect dialect;

    /**
     * 构造数据库数据转储处理器
     *
     * @param dbName  库名称
     * @param dialect 数据库方言
     */
    public DBDataDumpHandler(String dbName,DBDialect dialect) {
        this.dbName = dbName;
        this.dialect = dialect;
    }

    /**
     * 设置转储文件
     *
     * @param dumpFile 转储文件
     * @return 当前对象
     */
    public DBDataDumpHandler dumpFile(File dumpFile) throws IOException {
        this.dumpFile = dumpFile;
        if (this.fileWriter != null) {
            this.fileWriter.close();
        }
        this.fileWriter = new FastFileWriter(dumpFile);
        return this;
    }

    /**
     * 执行转储
     *
     * @throws Exception 异常
     */
    public abstract void doDump() throws Exception;

    /**
     * 写入头部
     */
    protected abstract void writeHeader() throws IOException;

    /**
     * 写入尾部
     */
    protected abstract void writeTail() throws IOException;

    /**
     * 是否导出数据
     *
     * @return 结果
     */
    public boolean isDumpRecord() {
        return this.dataType == 0;
    }

    /**
     * 获取数据类型。
     *
     * @return 数据类型
     */
    public Byte getDataType() {
        return dataType;
    }

    /**
     * 设置数据类型。
     *
     * @param dataType 数据类型
     */
    public void setDataType(Byte dataType) {
        this.dataType = dataType;
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
     * 获取转储文件。
     *
     * @return 转储文件
     */
    public File getDumpFile() {
        return dumpFile;
    }

    /**
     * 设置转储文件。
     *
     * @param dumpFile 转储文件
     */
    public void setDumpFile(File dumpFile) {
        this.dumpFile = dumpFile;
    }

    /**
     * 获取文件写出器。
     *
     * @return 文件写出器
     */
    public FastFileWriter getFileWriter() {
        return fileWriter;
    }

    /**
     * 设置文件写出器。
     *
     * @param fileWriter 文件写出器
     */
    public void setFileWriter(FastFileWriter fileWriter) {
        this.fileWriter = fileWriter;
    }

    /**
     * 获取转储类型。
     *
     * @return 转储类型
     */
    public Byte getDumpType() {
        return dumpType;
    }

    /**
     * 设置转储类型。
     *
     * @param dumpType 转储类型
     * @return 转储类型
     */
    public DBDataDumpHandler setDumpType(Byte dumpType) {
        this.dumpType = dumpType;
        return this;
    }

    /**
     * 获取表名称。
     *
     * @return 表名称
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 设置表名称。
     *
     * @param tableName 表名称
     * @return 表名称
     */
    public DBDataDumpHandler setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * 获取查询限制。
     *
     * @return 查询限制
     */
    public int getQueryLimit() {
        return queryLimit;
    }

    /**
     * 设置查询限制。
     *
     * @param queryLimit 查询限制
     * @return 查询限制
     */
    public DBDataDumpHandler setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
        return this;
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
}

