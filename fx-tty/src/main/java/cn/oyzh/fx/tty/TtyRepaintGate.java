package cn.oyzh.fx.tty;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 终端读取与重绘之间的同步开关。
 * <p>
 * 终端读取到一个数据块后暂停重绘，等该块全部解析完成再恢复，
 * 避免一次输出被拆成多个中间帧。
 *
 * @author oyzh
 * @since 2026-10-24
 */
public class TtyRepaintGate {

    /**
     * 是否暂停重绘
     */
    private final AtomicBoolean paused = new AtomicBoolean(false);

    /**
     * 暂停重绘
     */
    public void pause() {
        this.paused.set(true);
    }

    /**
     * 恢复重绘
     */
    public void resume() {
        this.paused.set(false);
    }

    /**
     * 是否已暂停重绘
     *
     * @return 是否已暂停
     */
    public boolean isPaused() {
        return this.paused.get();
    }
}
