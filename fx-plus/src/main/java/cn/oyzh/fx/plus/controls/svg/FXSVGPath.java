package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.SVGPath;

/**
 * @author oyzh
 * @since 2025/01/07
 */
public class FXSVGPath extends SVGPath implements PropAdapter {

    public FXSVGPath() {
        super();
    }

    public FXSVGPath(String content) {
        super();
        super.setContent(content);
    }

    public WritableImage snapshot() {
        Bounds bounds = this.getBoundsInLocal();
        int width = (int) Math.ceil(bounds.getWidth());
        int height = (int) Math.ceil(bounds.getHeight());
        int x = (int) Math.floor(bounds.getMinX());
        int y = (int) Math.floor(bounds.getMinY());
        WritableImage writableImage = new WritableImage(width, height);
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setViewport(new Rectangle2D(x, y, width, height));
        FXUtil.runWait(()-> this.snapshot(new SnapshotParameters(), writableImage));
        return writableImage;
    }
}
