package cn.oyzh.fx.db.data;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public abstract class DataExportHandler extends DataHandler {

    /**
     * 执行导出
     *
     * @throws Exception 异常
     */
    public abstract void doExport() throws Exception ;
}

