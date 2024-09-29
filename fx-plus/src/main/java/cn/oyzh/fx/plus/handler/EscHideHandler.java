package cn.oyzh.fx.plus.handler;

import cn.oyzh.fx.common.cache.CacheUtil;
import cn.oyzh.fx.common.cache.WeakCache;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import javafx.event.EventTarget;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import lombok.NonNull;

/**
 * esc按键隐藏处理器
 *
 * @author oyzh
 * @since 2023/4/24
 */
public class EscHideHandler {

    /**
     * 缓存列表
     */
    private static final WeakCache<EventTarget, EscHideHandler> CACHE = CacheUtil.newWeakCache();

    /**
     * 执行初始化
     *
     * @param stage 舞台
     * @return 处理器
     */
    public static EscHideHandler init(@NonNull Window stage) {
        EscHideHandler handler = new EscHideHandler(stage);
        CACHE.put(stage, handler);
        return handler;
    }

    /**
     * 是否存在处理器
     *
     * @param stage 舞台
     * @return 结果
     */
    public static boolean exists(Window stage) {
        if (stage != null) {
            return CACHE.containsKey(stage);
        }
        return false;
    }

    /**
     * 执行销毁
     *
     * @param stage 舞台
     */
    public static void destroy(Window stage) {
        if (stage != null) {
            EscHideHandler handler = CACHE.get(stage);
            if (handler != null) {
                CACHE.remove(stage);
                handler.destroy();
            }
        }
    }

    /**
     * 根节点
     */
    private Window stage;

    public EscHideHandler(@NonNull Window stage) {
        this.stage = stage;
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {
        KeyListener.listenReleased(this.stage, KeyCode.ESCAPE, this::quit);
    }

    /**
     * 销毁
     */
    private void destroy() {
        KeyListener.unListenReleased(this.stage, KeyCode.ESCAPE);
    }

    /**
     * 跳转到下一个节点
     *
     * @param event 事件
     */
    private void quit(KeyEvent event) {
        try {
            this.stage.hide();
            this.stage = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
