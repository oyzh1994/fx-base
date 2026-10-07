package cn.oyzh.fx.plus.handler;

import cn.oyzh.fx.plus.keyboard.KeyListener;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;

import java.lang.ref.WeakReference;

/**
 * esc按键隐藏处理器
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class EscHideHandler {

    /**
     * 窗口弱引用
     */
    private WeakReference<Window> windowRef;

    /**
     * 构造Esc隐藏处理器对象。
     *
     * @param window 窗口
     */
    public EscHideHandler(Window window) {
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
     * 处理 esc 按键事件，隐藏窗口
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

    /**
     * 是否已失效
     *
     * @return 是否已失效
     */
    protected boolean isInvalid() {
        return this.windowRef == null || this.windowRef.get() == null;
    }

    /**
     * 获取窗口
     *
     * @return 窗口
     */
    protected Window window() {
        return this.windowRef == null ? null : this.windowRef.get();
    }
}
