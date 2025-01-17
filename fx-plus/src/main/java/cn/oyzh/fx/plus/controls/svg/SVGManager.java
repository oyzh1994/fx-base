package cn.oyzh.fx.plus.controls.svg;

import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.shape.SVGPath;
import lombok.experimental.UtilityClass;

import java.lang.ref.WeakReference;

/**
 * svg管理器
 *
 * @author oyzh
 * @since 2023/9/15
 */
@UtilityClass
public class SVGManager {

    /**
     * loading的svg路径引用
     */
    private static WeakReference<FXSVGPath> loadingSVGPathRef;

    /**
     * 加载svg路径
     *
     * @param url 地址
     * @return svg路径
     */
    public static FXSVGPath load(String url) {
        return SVGLoader.INSTANCE.load(url);
    }

    /**
     * 加载svg内容
     *
     * @param url 地址
     * @return svg路径
     */
    public static String loadContent(String url) {
        return SVGLoader.INSTANCE.loadContent(url);
    }

//    /**
//     * 是否loading的svg路径
//     *
//     * @param image svg路径
//     * @return 结果
//     */
//    public static boolean isLoading(Image image) {
////        return image != null && image.getUrl().contains("/fx-svg/loading.svg");
//        return false;
//    }

    /**
     * 是否loading的svg路径
     *
     * @param svgPath svg路径
     * @return 结果
     */
    public static boolean isLoading(SVGPath svgPath) {
        return loadingSVGPathRef != null && svgPath != null && svgPath == loadingSVGPathRef.get();
    }

    /**
     * 获取loading的svg路径
     *
     * @return loading的svg路径
     */
    public static FXSVGPath getLoading() {
        if (loadingSVGPathRef == null || loadingSVGPathRef.get() == null) {
            FXSVGPath svgPath = SVGManager.load("/fx-svg/loading.svg");
            svgPath.setCursor(Cursor.NONE);
            loadingSVGPathRef = new WeakReference<>(svgPath);
        }
        return loadingSVGPathRef.get();
    }
}
