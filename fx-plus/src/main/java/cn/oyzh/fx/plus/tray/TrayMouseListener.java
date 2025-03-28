package cn.oyzh.fx.plus.tray;


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
    private MouseListener mouseListener;

    /**
     * 鼠标移动事件
     */
    private Consumer<MouseEvent> mouseMoved;

    /**
     * 鼠标离开事件
     */
    private Consumer<MouseEvent> mouseExited;

    /**
     * 鼠标点击事件
     */
    private Consumer<MouseEvent> mouseClicked;

    /**
     * 鼠标拖拽事件
     */
    private Consumer<MouseEvent> mouseDragged;

    /**
     * 鼠标进入事件
     */
    private Consumer<MouseEvent> mouseEntered;

    /**
     * 鼠标按下事件
     */
    private Consumer<MouseEvent> mousePressed;

    /**
     * 鼠标释放事件
     */
    private Consumer<MouseEvent> mouseReleased;

    /**
     * 鼠标滚轮滚动事件
     */
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

    public MouseListener getMouseListener() {
        return mouseListener;
    }

    public void setMouseListener(MouseListener mouseListener) {
        this.mouseListener = mouseListener;
    }

    public Consumer<MouseEvent> getMouseMoved() {
        return mouseMoved;
    }

    public void setMouseMoved(Consumer<MouseEvent> mouseMoved) {
        this.mouseMoved = mouseMoved;
    }

    public Consumer<MouseEvent> getMouseExited() {
        return mouseExited;
    }

    public void setMouseExited(Consumer<MouseEvent> mouseExited) {
        this.mouseExited = mouseExited;
    }

    public Consumer<MouseEvent> getMouseClicked() {
        return mouseClicked;
    }

    public void setMouseClicked(Consumer<MouseEvent> mouseClicked) {
        this.mouseClicked = mouseClicked;
    }

    public Consumer<MouseEvent> getMouseDragged() {
        return mouseDragged;
    }

    public void setMouseDragged(Consumer<MouseEvent> mouseDragged) {
        this.mouseDragged = mouseDragged;
    }

    public Consumer<MouseEvent> getMouseEntered() {
        return mouseEntered;
    }

    public void setMouseEntered(Consumer<MouseEvent> mouseEntered) {
        this.mouseEntered = mouseEntered;
    }

    public Consumer<MouseEvent> getMousePressed() {
        return mousePressed;
    }

    public void setMousePressed(Consumer<MouseEvent> mousePressed) {
        this.mousePressed = mousePressed;
    }

    public Consumer<MouseEvent> getMouseReleased() {
        return mouseReleased;
    }

    public void setMouseReleased(Consumer<MouseEvent> mouseReleased) {
        this.mouseReleased = mouseReleased;
    }

    public Consumer<MouseWheelEvent> getMouseWheelMoved() {
        return mouseWheelMoved;
    }

    public void setMouseWheelMoved(Consumer<MouseWheelEvent> mouseWheelMoved) {
        this.mouseWheelMoved = mouseWheelMoved;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (this.mouseWheelMoved != null) {
            this.mouseWheelMoved.accept(e);
        }
    }
}
