package cn.oyzh.fx.plus.controls.svg;

import javafx.scene.Cursor;
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
    private static WeakReference<SVGPath> loadingSvgPathReference;

    /**
     * 加载svg路径
     *
     * @param url 地址
     * @return svg路径
     */
    public static SVGPath load(String url) {
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

    /**
     * 是否loading的svg路径
     *
     * @param svgPath svg路径
     * @return 结果
     */
    public static boolean isLoadingSvgPath(SVGPath svgPath) {
        return loadingSvgPathReference != null && svgPath != null && svgPath == loadingSvgPathReference.get();
    }

    /**
     * 获取loading的svg路径
     *
     * @return loading的svg路径
     */
    public static SVGPath getLoadingSvgPath() {
        if (loadingSvgPathReference == null || loadingSvgPathReference.get() == null) {
            SVGPath svgPath = SVGManager.load("/fx-gui/font/loading.svg");
            svgPath.setCursor(Cursor.NONE);
            loadingSvgPathReference = new WeakReference<>(svgPath);
        }
        return loadingSvgPathReference.get();
    }
}
