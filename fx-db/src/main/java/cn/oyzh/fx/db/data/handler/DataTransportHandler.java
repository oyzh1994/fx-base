package cn.oyzh.fx.db.data.handler;

/**
 * 数据传输处理器抽象基类，定义传输执行入口
 *
 * @author oyzh
 * @since 2026-09-01
 */
public abstract class DataTransportHandler extends DataHandler {

    /**
     * 执行传输
     *
     * @throws Exception 异常
     */
    public abstract void doTransport() throws Exception;
}

