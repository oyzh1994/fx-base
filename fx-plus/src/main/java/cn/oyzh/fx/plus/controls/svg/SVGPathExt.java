package cn.oyzh.fx.plus.controls.svg;

import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import lombok.Getter;
import lombok.NonNull;

/**
 * svg路径扩展
 *
 * @author oyzh
 * @since 2023/10/24
 */
public class SVGPathExt extends SVGPath {

    /**
     * svg地址
     */
    @Getter
    private String url;

    public SVGPathExt() {
        super();
    }

    public SVGPathExt(String url) {
        super();
        this.setUrl(url);
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
        super.setContent(SVGLoader.INSTANCE.load(url));
    }

    public void setColor(@NonNull Paint paint) {
        this.setFill(paint);
        this.setStroke(paint);
    }

    public Paint getColor() {
        return this.getFill();
    }
}
