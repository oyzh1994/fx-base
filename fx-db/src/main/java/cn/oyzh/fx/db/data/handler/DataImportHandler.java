package cn.oyzh.fx.db.data.handler;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public abstract class DataImportHandler extends DataHandler {

    /**
     * 执行导入
     *
     * @throws Exception 异常
     */
    public abstract void doImport() throws Exception ;
}

