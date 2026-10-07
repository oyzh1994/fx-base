package cn.oyzh.fx.db.data.handler;

/**
 * 数据导入处理器抽象基类，定义导入执行入口
 *
 * @author oyzh
 * @since 2026-09-01
 */
public abstract class DataImportHandler extends DataHandler {

    /**
     * 执行导入
     *
     * @throws Exception 异常
     */
    public abstract void doImport() throws Exception ;
}

