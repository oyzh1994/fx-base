package cn.oyzh.fx.plus.event;

import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2024/3/29
 */
@Setter
public class EventConfig {

    private Boolean async;

    @Getter
    private Integer delay;

    private Boolean verbose;

    private Boolean fxThread;

    public boolean isAsync() {
        return async != null && async;
    }

    public boolean isDelay() {
        return delay != null && delay > 0;
    }

    public boolean isVerbose() {
        return verbose != null && verbose;
    }

    public boolean isFxThread() {
        return fxThread != null && fxThread;
    }

    public static EventConfig DEFAULT = new EventConfig();

    static {
        DEFAULT.async = true;
        DEFAULT.verbose = true;
        DEFAULT.fxThread = true;
    }
}
