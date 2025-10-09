package cn.oyzh.fx.plus.controls.svg;

/**
 * svg管理器
 *
 * @author oyzh
 * @since 2023/9/15
 */

public class SVGManager {

//    /**
//     * loading的svg路径引用
//     */
//    private static WeakReference<FXSVGPath> loadingSVGPathRef;

    /**
     * 加载svg路径
     *
     * @param url 地址
     * @return svg路径
     */
    public static FXSVGPath load(String url) {
        return SVGLoader.INSTANCE.load(url);
    }

    // /**
    //  * 加载svg内容
    //  *
    //  * @param url 地址
    //  * @return svg路径
    //  */
    // public static String loadContent(String url) {
    //     return SVGLoader.INSTANCE.loadContent(url);
    // }

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

//    /**
//     * 是否loading的svg路径
//     *
//     * @param svgPath svg路径
//     * @return 结果
//     */
//    public static boolean isLoading(SVGPath svgPath) {
////        return loadingSVGPathRef != null && svgPath != null && svgPath == loadingSVGPathRef.get();
//        return svgPath.hasProperties() && svgPath.getProperties().containsKey("loading");
//    }

//    /**
//     * 获取loading的svg路径
//     *
//     * @return loading的svg路径
//     */
//    public static FXSVGPath getLoading() {
////        if (loadingSVGPathRef == null || loadingSVGPathRef.get() == null) {
//        FXSVGPath svgPath = SVGManager.load("/fx-svg/loading.svg");
//        svgPath.setProp("loading", true);
//        svgPath.setCursor(Cursor.NONE);
////            loadingSVGPathRef = new WeakReference<>(svgPath);
////        }
////        return loadingSVGPathRef.get();
//        return svgPath;
//    }
}
