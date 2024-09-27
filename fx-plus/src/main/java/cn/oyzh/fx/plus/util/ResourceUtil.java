package cn.oyzh.fx.plus.util;

import cn.hutool.log.StaticLog;
import cn.oyzh.fx.common.log.JulLog;
import javafx.scene.image.Image;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 资源工具类
 *
 * @author oyzh
 * @since 2023/02/28
 */
//@Slf4j
@UtilityClass
public class ResourceUtil {

    /**
     * 获取图片
     *
     * @param imgUrls 图片列表地址
     * @return 图片列表
     */
    public static List<Image> getImages(@NonNull String[] imgUrls) {
        return getImages(Arrays.asList(imgUrls));
    }

    /**
     * 获取图片
     *
     * @param imgUrls 图片列表地址
     * @return 图片列表
     */
    public static List<Image> getImages(@NonNull List<String> imgUrls) {
        List<Image> icons = new ArrayList<>(imgUrls.size());
        for (String url : imgUrls) {
            JulLog.info("load imgUrl:{}", url);
            InputStream stream = getResourceAsStream(url);
            if (stream == null) {
                JulLog.warn("img stream is null.");
            } else {
                icons.add(new Image(stream));
            }
        }
        return icons;
    }

    /**
     * 获取图片
     *
     * @param imgUrl 图片地址
     * @return 图片
     */
    public static Image getImage(@NonNull String imgUrl) {
        JulLog.info("load imgUrl:{}", imgUrl);
        InputStream stream = getResourceAsStream(imgUrl);
        if (stream == null) {
            JulLog.warn("img stream is null.");
            return null;
        }
        return new Image(stream);
    }

    /**
     * 获取资源
     *
     * @param url 地址
     * @return URL
     */
    public static URL getResource(@NonNull String url) {
        URL u = null;
        try {
            u = ResourceUtil.class.getResource(url);
            if (u == null) {
                u = ResourceUtil.class.getClassLoader().getResource(url);
            }
            if (u == null && url.startsWith("/")) {
                u = ResourceUtil.class.getClassLoader().getResource(url.substring(1));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return u;
    }

    /**
     * 获取资源流
     *
     * @param url 地址
     * @return InputStream
     */
    public static InputStream getResourceAsStream(@NonNull String url) {
        InputStream stream = null;
        try {
            stream = ResourceUtil.class.getResourceAsStream(url);
            if (stream == null) {
                stream = ResourceUtil.class.getClassLoader().getResourceAsStream(url);
            }
            if (stream == null && url.startsWith("/")) {
                stream = ResourceUtil.class.getClassLoader().getResourceAsStream(url.substring(1));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return stream;
    }


    /**
     * 转换为物理地址
     *
     * @param url 地址
     * @return 物理地址
     */
    public static String toExternalUrl(@NonNull String url) {
        JulLog.info("url:{}", url);
        URL u = getResource(url);
        return u == null ? null : u.toExternalForm();
    }

    /**
     * 转换为物理地址
     *
     * @param urls 地址列表
     * @return 物理地址列表
     */
    public static List<String> toExternalUrl(@NonNull String[] urls) {
        return toExternalUrl(Arrays.asList(urls));
    }

    /**
     * 转换为物理地址
     *
     * @param urls 地址列表
     * @return 物理地址列表
     */
    public static List<String> toExternalUrl(@NonNull List<String> urls) {
        List<String> urlList = new ArrayList<>(urls.size());
        for (String url : urls) {
            urlList.add(toExternalUrl(url));
        }
        return urlList;
    }
}
