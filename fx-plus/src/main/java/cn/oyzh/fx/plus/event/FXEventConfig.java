package cn.oyzh.fx.plus.event;

import cn.oyzh.event.EventConfig;

/**
 * @author oyzh
 * @since 2024/3/29
 */
public class FXEventConfig extends EventConfig {

    /**
     * 是否fx线程
     */
    private Boolean fxThread;

    public void setFxThread(Boolean fxThread) {
        this.fxThread = fxThread;
    }

    public boolean isFxThread() {
        return fxThread != null && fxThread;
    }

    /**
     * 同步配置
     */
    public static FXEventConfig SYNC = new FXEventConfig();

    /**
     * 异步配置
     */
    public static FXEventConfig ASYNC = new FXEventConfig();

    /**
     * 默认配置
     */
    public static FXEventConfig DEFAULT = new FXEventConfig();

    static {
        SYNC.async = false;
        SYNC.verbose = true;
        SYNC.fxThread = false;

        ASYNC.async = true;
        ASYNC.verbose = true;
        ASYNC.fxThread = true;

        DEFAULT.async = true;
        DEFAULT.verbose = true;
        DEFAULT.fxThread = true;
    }
}
