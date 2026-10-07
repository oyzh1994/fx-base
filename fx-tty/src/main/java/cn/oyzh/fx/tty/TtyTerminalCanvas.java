package cn.oyzh.fx.tty;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;


/**
 * 终端渲染组件
 *
 * @author oyzh
 * @since 2026-07-06
 */
public class TtyTerminalCanvas extends FXPane implements Destroyable {

    /**
     * 构造终端渲染组件，创建并绑定内部画布。
     */
    public TtyTerminalCanvas() {
        Canvas canvas = new Canvas();
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());
        this.addChild(canvas);
    }

    /**
     * 获取内部画布。
     *
     * @return 画布
     */
    public Canvas getCanvas() {
        return (Canvas) this.getFirstChild();
    }

    /**
     * 获取画布的 2D 图形上下文。
     *
     * @return 2D 图形上下文
     */
    public GraphicsContext getGraphicsContext2D() {
        return this.getCanvas().getGraphicsContext2D();
    }

    @Override
    public void destroy() {
        Canvas canvas = this.getCanvas();
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        NodeDestroyUtil.destroyObject(this);
    }
}