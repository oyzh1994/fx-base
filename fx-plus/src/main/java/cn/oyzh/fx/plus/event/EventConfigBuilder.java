package cn.oyzh.fx.plus.event;

/**
 * 事件
 *
 * @author oyzh
 * @since 2023/4/10
 */
public class EventConfigBuilder {

    private int delay;

    private boolean async;

    private boolean verbose;

    private boolean fxThread;

    public EventConfigBuilder async(boolean async) {
        this.async = async;
        return this;
    }

    public EventConfigBuilder delay(int delay) {
        this.delay = delay;
        return this;
    }

    public EventConfigBuilder verbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public EventConfigBuilder fxThread(boolean fxThread) {
        this.fxThread = fxThread;
        return this;
    }

    public EventConfig build() {
        EventConfig config = new EventConfig();
        config.setDelay(delay);
        config.setAsync(async);
        config.setVerbose(verbose);
        config.setFxThread(fxThread);
        return config;
    }

    public static EventConfigBuilder newBuilder() {
        return new EventConfigBuilder();
    }
}
