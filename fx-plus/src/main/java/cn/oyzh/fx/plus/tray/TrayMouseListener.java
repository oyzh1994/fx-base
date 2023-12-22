package cn.oyzh.fx.plus.tray;

import lombok.Getter;
import lombok.Setter;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.util.function.Consumer;

/**
 * 系统托盘鼠标监听器
 *
 * @author oyzh
 * @since 2022/8/24
 */
public class TrayMouseListener extends MouseAdapter {

    /**
     * 鼠标监听器
     */
    @Getter
    @Setter
    private MouseListener mouseListener;

    /**
     * 鼠标移动事件
     */
    @Getter
    @Setter
    private Consumer<MouseEvent> mouseMoved;

    /**
     * 鼠标离开事件
     */
    @Getter
    @Setter
    private Consumer<MouseEvent> mouseExited;

    /**
     * 鼠标点击事件
     */
    @Getter
    @Setter
    private Consumer<MouseEvent> mouseClicked;

    /**
     * 鼠标拖拽事件
     */
    @Getter
    @Setter
    private Consumer<MouseEvent> mouseDragged;

    /**
     * 鼠标进入事件
     */
    @Getter
    @Setter
    private Consumer<MouseEvent> mouseEntered;

    /**
     * 鼠标按下事件
     */
    @Getter
    @Setter
    private Consumer<MouseEvent> mousePressed;

    /**
     * 鼠标释放事件
     */
    @Getter
    @Setter
    private Consumer<MouseEvent> mouseReleased;

    /**
     * 鼠标滚轮滚动事件
     */
    @Getter
    @Setter
    private Consumer<MouseWheelEvent> mouseWheelMoved;

    @Override
    public void mouseMoved(MouseEvent e) {
        if (this.mouseMoved != null) {
            this.mouseMoved.accept(e);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (this.mouseExited != null) {
            this.mouseExited.accept(e);
        }
        if (this.mouseListener != null) {
            this.mouseListener.mouseExited(e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (this.mouseClicked != null) {
            this.mouseClicked.accept(e);
        }
        if (this.mouseListener != null) {
            this.mouseListener.mouseClicked(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.mouseDragged != null) {
            this.mouseDragged.accept(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (this.mouseEntered != null) {
            this.mouseEntered.accept(e);
        }
        if (this.mouseListener != null) {
            this.mouseListener.mouseEntered(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (this.mousePressed != null) {
            this.mousePressed.accept(e);
        }
        if (this.mouseListener != null) {
            this.mouseListener.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (this.mouseReleased != null) {
            this.mouseReleased.accept(e);
        }
        if (this.mouseListener != null) {
            this.mouseListener.mouseReleased(e);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (this.mouseWheelMoved != null) {
            this.mouseWheelMoved.accept(e);
        }
    }
}
