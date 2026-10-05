package cn.oyzh.fx.db.data.handler;


import cn.oyzh.fx.db.data.DataBatchInsertable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public abstract class DBDataImportHandler<D> extends DataImportHandler implements DataBatchInsertable<D> {

    /**
     * 名称
     */
    protected String name;

    /**
     * 文件类型
     * sql
     * xml
     * csv
     * excel
     */
    protected String fileType;

    /**
     * 插入限制，insertLimit/batchLimit=连接数，mysql默认是151，尽量不要超过连接数
     */
    protected int insertLimit = 5000;

    /**
     * 读取限制，readLimit/batchLimit=连接数，mysql默认是151，尽量不要超过连接数
     */
    protected int readLimit = 5000;

    /**
     * 批量处理限制
     */
    protected int batchLimit = 50;

    public DBDataImportHandler(String name) {
        this.name = name;
    }

    /**
     * 是否sql类型
     *
     * @return 结果
     */
    public boolean isSqlType() {
        return "sql".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否xml类型
     *
     * @return 结果
     */
    public boolean isXmlType() {
        return "xml".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否csv类型
     *
     * @return 结果
     */
    public boolean isCsvType() {
        return "csv".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否xls类型
     *
     * @return 结果
     */
    public boolean isExcelType() {
        return "excel".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否json类型
     *
     * @return 结果
     */
    public boolean isJsonType() {
        return "json".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否txt类型
     *
     * @return 结果
     */
    public boolean isTxtType() {
        return "txt".equalsIgnoreCase(this.fileType);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getReadLimit() {
        return readLimit;
    }

    public void setReadLimit(int readLimit) {
        this.readLimit = readLimit;
    }

    @Override
    public int getBatchLimit() {
        return batchLimit;
    }

    public void setBatchLimit(int batchLimit) {
        this.batchLimit = batchLimit;
    }

    @Override
    public int getInsertLimit() {
        return insertLimit;
    }

    public void setInsertLimit(int insertLimit) {
        this.insertLimit = insertLimit;
    }

    /**
     * 执行导入
     *
     * @throws Exception 异常
     */
    public abstract void doImport() throws Exception ;
}

