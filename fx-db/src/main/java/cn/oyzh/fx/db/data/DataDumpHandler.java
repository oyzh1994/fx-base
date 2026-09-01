package cn.oyzh.fx.db.data;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public abstract class DataDumpHandler extends DataHandler {

    /**
     * 执行存储
     *
     * @throws Exception 异常
     */
    public abstract void doDump() throws Exception ;
}

