package cn.oyzh.fx.plus.handler;

import cn.oyzh.fx.plus.keyboard.KeyListener;
import javafx.event.EventTarget;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * esc按键隐藏处理器
 *
 * @author oyzh
 * @since 2023/4/24
 */
public class EscHideHandler {

    /**
     * 处理器列表
     */
    private static final Map<EventTarget, EscHideHandler> HANDLERS = new HashMap<>();

    /**
     * 执行初始化
     *
     * @param stage 舞台
     * @return 处理器
     */
    public static EscHideHandler init(@NonNull Stage stage) {
        EscHideHandler handler = new EscHideHandler(stage);
        HANDLERS.put(stage, handler);
        return handler;
    }

    /**
     * 是否存在处理器
     *
     * @param stage 舞台
     * @return 结果
     */
    public static boolean exists(Stage stage) {
        if (stage != null) {
            return HANDLERS.containsKey(stage);
        }
        return false;
    }

    /**
     * 执行销毁
     *
     * @param stage 舞台
     */
    public static void destroy(Stage stage) {
        if (stage != null) {
            EscHideHandler handler = HANDLERS.remove(stage);
            if (handler != null) {
                handler.destroy();
            }
        }
    }

    /**
     * 根节点
     */
    private Stage stage;

    public EscHideHandler(@NonNull Stage stage) {
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
            this.stage.close();
            this.stage = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
