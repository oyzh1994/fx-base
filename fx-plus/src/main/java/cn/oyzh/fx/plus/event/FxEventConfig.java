package cn.oyzh.fx.plus.event;

import cn.oyzh.event.EventConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2024/3/29
 */
@Setter
public class FxEventConfig extends EventConfig {

    private Boolean fxThread;

    public static FxEventConfig DEFAULT = new FxEventConfig();

    public boolean isFxThread() {
        return fxThread != null && fxThread;
    }

    static {
        DEFAULT.async = true;
        DEFAULT.verbose = true;
        DEFAULT.fxThread = true;
    }
}
