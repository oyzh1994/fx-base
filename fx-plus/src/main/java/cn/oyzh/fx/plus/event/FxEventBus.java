package cn.oyzh.fx.plus.event;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.event.EventBus;
import cn.oyzh.event.EventConfig;
import cn.oyzh.fx.plus.util.FXUtil;

/**
 * @author oyzh
 * @since 2024-11-14
 */
public class FxEventBus extends EventBus {

    @Override
    public <C extends EventConfig> void post(Object event, C config, Integer delayMillis) {
        if (config instanceof FxEventConfig fxConfig) {
            boolean verbose = config.isVerbose();
            boolean delay = delayMillis != null && delayMillis > 0;
            boolean async = config.isAsync();
            boolean fxThread = fxConfig.isFxThread();
            Runnable func = () -> this.doEventPost(event, verbose);
            // 延迟、异步、fx线程
            if (delay && async && fxThread) {
                TaskManager.startDelay(() -> FXUtil.runWait(func), delayMillis);
            } else if (delay && async) {// 延迟、异步
                TaskManager.startDelay(func, delayMillis);
            } else if (delay && fxThread) {// 延迟、fx线程
                TaskManager.startDelay(() -> FXUtil.runWait(func), delayMillis);
            } else if (async && fxThread) {// 异步、fx线程
                TaskManager.startSync(() -> FXUtil.runWait(func));
            } else if (delay) {// 延迟
                TaskManager.startDelay(func, delayMillis);
            } else if (async) {// 异步
                TaskManager.startSync(func);
            } else if (fxThread) {// fx线程
                FXUtil.runWait(func);
            } else {// 正常执行
                func.run();
            }
        } else {
            super.post(event, config, delayMillis);
        }
    }
}
