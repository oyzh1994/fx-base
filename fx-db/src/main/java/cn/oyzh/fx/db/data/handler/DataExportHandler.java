package cn.oyzh.fx.db.data.handler;

/**
 * 数据导出处理器抽象基类，定义导出执行入口
 *
 * @author oyzh
 * @since 2024-08-27
 */
public abstract class DataExportHandler extends DataHandler {

    /**
     * 执行导出
     *
     * @throws Exception 异常
     */
    public abstract void doExport() throws Exception ;
}

