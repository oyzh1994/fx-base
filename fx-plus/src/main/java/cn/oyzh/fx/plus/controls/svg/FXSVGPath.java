package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Bounds;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

import java.util.concurrent.atomic.AtomicReference;

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
        AtomicReference<WritableImage> writableImage1=new AtomicReference<>() ;
        SnapshotParameters snapshotParameters = new SnapshotParameters();
//        snapshotParameters.setViewport(new Rectangle2D(x, y, 100, 100));
        snapshotParameters.setTransform(new Scale(0.001, 0.001));
        FXUtil.runWait(()-> {
            writableImage1.set(this.snapshot(new SnapshotParameters(), writableImage));
        });
        return writableImage1.get();
    }
}
