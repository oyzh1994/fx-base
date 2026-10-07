package cn.oyzh.fx.db.data.handler;

/**
 * 数据转储处理器抽象基类，定义转储执行入口
 *
 * @author oyzh
 * @since 2024-08-27
 */
public abstract class DataDumpHandler extends DataHandler {

    /**
     * 执行存储
     *
     * @throws Exception 异常
     */
    public abstract void doDump() throws Exception ;
}

