package cn.oyzh.fx.plus.util;

import cn.oyzh.common.cache.CacheUtil;
import cn.oyzh.common.cache.TimedCache;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.ResourceUtil;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 图标工具类
 *
 * @author oyzh
 * @since 2023/4/4
 */
public class IconUtil {

    /**
     * 图标缓存
     */
    private static final TimedCache<String, WeakReference<byte[]>> ICON_CACHE = CacheUtil.newTimedCache(60 * 1000L);

    /**
     * 获取图标
     *
     * @param iconUrls 图标列表地址
     * @return 图标列表
     */
    public static List<Image> getIcons( String[] iconUrls) {
        return getIcons(Arrays.asList(iconUrls));
    }

    /**
     * 获取图标
     *
     * @param iconUrls 图标列表地址
     * @return 图标列表
     */
    public static List<Image> getIcons( List<String> iconUrls) {
        List<Image> icons = new ArrayList<>(iconUrls.size());
        for (String iconUrl : iconUrls) {
            Image icon = getIcon(iconUrl);
            if (icon != null) {
                icons.add(icon);
            }
        }
        return icons;
    }

    /**
     * 获取图标
     *
     * @param iconUrl 图标地址
     * @return 图标
     */
    public static Image getIcon( String iconUrl) {
        InputStream stream;
        WeakReference<byte[]> reference = ICON_CACHE.get(iconUrl);
        if (reference == null || reference.get() == null) {
            stream = ResourceUtil.getResourceAsStream(iconUrl);
            if (stream != null) {
                byte[] bytes = IOUtil.readBytes(stream);
                ICON_CACHE.put(iconUrl, new WeakReference<>(bytes));
                stream = IOUtil.toStream(bytes);
            }
            if (JulLog.isInfoEnabled()) {
                JulLog.info("load icon form file.");
            }
        } else {
            stream = IOUtil.toStream(reference.get());
            if (JulLog.isInfoEnabled()) {
                JulLog.info("load icon form cache.");
            }
        }
        return stream == null ? null : new Image(stream);
    }
}
