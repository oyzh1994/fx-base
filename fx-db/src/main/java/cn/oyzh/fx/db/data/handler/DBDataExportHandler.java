package cn.oyzh.fx.db.data.handler;


/**
 * 数据库数据导出处理器抽象基类，负责按指定文件类型导出数据
 *
 * @author oyzh
 * @since 2024-08-27
 */
public abstract class DBDataExportHandler extends DataExportHandler {

    /**
     * 名称
     */
    protected String name;

    /**
     * 文件类型
     * sql
     * json
     */
    protected String fileType;

    /**
     * 查询限制
     */
    protected int queryLimit = 1000;

    /**
     * 构造数据库数据导出处理器
     *
     * @param name 名称
     */
    public DBDataExportHandler( String name) {
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
     * 是否html类型
     *
     * @return 结果
     */
    public boolean isHtmlType() {
        return "html".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否xls类型
     *
     * @return 结果
     */
    public boolean isXlsType() {
        return "xls".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否xlsx类型
     *
     * @return 结果
     */
    public boolean isXlsxType() {
        return "xlsx".equalsIgnoreCase(this.fileType);
    }

    /**
     * 是否excel类型
     *
     * @return 结果
     */
    public boolean isExcelType() {
        return this.isXlsType() || this.isXlsxType();
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
     * 是否js类型
     *
     * @return 结果
     */
    public boolean isJsType() {
        return "js".equalsIgnoreCase(this.fileType);
    }

    /**
     * 获取名称。
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称。
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取文件类型。
     *
     * @return 文件类型
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * 设置文件类型。
     *
     * @param fileType 文件类型
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
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
     */
    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
    }
}

