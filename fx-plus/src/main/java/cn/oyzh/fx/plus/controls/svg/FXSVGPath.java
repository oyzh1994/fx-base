package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;

/**
 * @author oyzh
 * @since 2025/01/07
 */
public class FXSVGPath extends SVGPath implements PropAdapter, Destroyable {

    public FXSVGPath() {
        super();
    }

    public FXSVGPath(String content) {
        super();
        super.setContent(content);
    }

    /**
     * 设置颜色
     *
     * @param color 颜色
     */
    public void setColor(Paint color) {
        this.setFill(color);
        this.setStroke(color);
    }

    // public WritableImage snapshot() {
    //     Bounds bounds = this.getBoundsInLocal();
    //     int width = (int) Math.ceil(bounds.getWidth());
    //     int height = (int) Math.ceil(bounds.getHeight());
    //     int x = (int) Math.floor(bounds.getMinX());
    //     int y = (int) Math.floor(bounds.getMinY());
    //     WritableImage writableImage = new WritableImage(width, height);
    //     SnapshotParameters snapshotParameters = new SnapshotParameters();
    //     snapshotParameters.setViewport(new Rectangle2D(x, y, width, height));
    //     FXUtil.runWait(()-> this.snapshot(new SnapshotParameters(), writableImage));
    //     return writableImage;
    // }

    @Override
    public void destroy() {
        this.fillProperty().unbind();
        this.fillRuleProperty().unbind();
        this.contentProperty().unbind();
    }
}
