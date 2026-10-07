package cn.oyzh.fx.tty;

import javafx.scene.input.KeyEvent;

/**
 * 终端按键监听器，用于接收字符输入、按键按下与释放事件。
 *
 * @author oyzh
 * @since 2025-07-29
 */
public interface TtyKeyListener {

    /**
     * 字符输入事件
     *
     * @param e 按键事件
     */
    void keyTyped(KeyEvent e);

    /**
     * 按键按下事件
     *
     * @param e 按键事件
     */
    void keyPressed(KeyEvent e);

    /**
     * 按键释放事件
     *
     * @param e 按键事件
     */
    void keyReleased(KeyEvent e);
}
