package cn.oyzh.fx.plus.handler;

import cn.oyzh.fx.plus.keyboard.KeyListener;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import lombok.NonNull;

import java.lang.ref.WeakReference;

/**
 * esc按键隐藏处理器
 *
 * @author oyzh
 * @since 2023/4/24
 */
public class EscHideHandler {

//    /**
//     * 缓存列表
//     */
//    private static final WeakCache<EventTarget, EscHideHandler> CACHE = CacheUtil.newWeakCache();
//
//    /**
//     * 执行初始化
//     *
//     * @param stage 舞台
//     * @return 处理器
//     */
//    public static EscHideHandler init(@NonNull Window stage) {
//        EscHideHandler handler = new EscHideHandler(stage);
//        CACHE.put(stage, handler);
//        return handler;
//    }
//
//    /**
//     * 是否存在处理器
//     *
//     * @param stage 舞台
//     * @return 结果
//     */
//    public static boolean exists(Window stage) {
//        if (stage != null) {
//            return CACHE.containsKey(stage);
//        }
//        return false;
//    }
//
//    /**
//     * 执行销毁
//     *
//     * @param stage 舞台
//     */
//    public static void destroy(Window stage) {
//        if (stage != null) {
//            EscHideHandler handler = CACHE.get(stage);
//            if (handler != null) {
//                CACHE.remove(stage);
//                handler.destroy();
//            }
//        }
//    }

    /**
     * 根节点
     */
    private WeakReference<Window> windowRef;

    public EscHideHandler(@NonNull Window window) {
        this.windowRef = new WeakReference<>(window);
        this.init();
    }

    /**
     * 初始化
     */
    public void init() {
        if (!this.isInvalid()) {
            KeyListener.listenReleased(this.window(), KeyCode.ESCAPE, this::quit);
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (!this.isInvalid()) {
            KeyListener.unListenReleased(this.window(), KeyCode.ESCAPE);
        }
        this.windowRef = null;
    }

    /**
     * 跳转到下一个节点
     *
     * @param event 事件
     */
    public void quit(KeyEvent event) {
        try {
            if (!this.isInvalid()) {
                this.window().hide();
                this.windowRef = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected boolean isInvalid() {
        return this.windowRef == null || this.windowRef.get() == null;
    }

    protected Window window() {
        return this.windowRef == null ? null : this.windowRef.get();
    }
}
