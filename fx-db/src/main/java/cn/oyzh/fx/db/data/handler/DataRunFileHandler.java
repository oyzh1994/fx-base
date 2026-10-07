package cn.oyzh.fx.db.data.handler;

/**
 * 文件运行处理器抽象基类，定义文件执行入口
 *
 * @author oyzh
 * @since 2024/09/10
 */
public abstract class DataRunFileHandler extends DataHandler {

    /**
     * 运行文件
     *
     * @throws Exception 异常
     */
    public abstract void runFile() throws Exception ;
}

