package cn.oyzh.fx.plus.event;

import cn.oyzh.event.EventConfig;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2024/3/29
 */
@Setter
public class FxEventConfig extends EventConfig {

    private Boolean fxThread;

    public static FxEventConfig SYNC = new FxEventConfig();

    public static FxEventConfig ASYNC = new FxEventConfig();

    public static FxEventConfig DEFAULT = new FxEventConfig();

    public boolean isFxThread() {
        return fxThread != null && fxThread;
    }

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
