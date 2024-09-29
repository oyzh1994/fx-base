package cn.oyzh.fx.plus.util;

import cn.oyzh.fx.common.cache.CacheUtil;
import cn.oyzh.fx.common.cache.TimedCache;
import cn.oyzh.fx.common.log.JulLog;
import cn.oyzh.fx.common.util.ResourceUtil;
import cn.oyzh.fx.common.util.IOUtil;
import javafx.scene.image.Image;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

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
@UtilityClass
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
    public static List<Image> getIcons(@NonNull String[] iconUrls) {
        return getIcons(Arrays.asList(iconUrls));
    }

    /**
     * 获取图标
     *
     * @param iconUrls 图标列表地址
     * @return 图标列表
     */
    public static List<Image> getIcons(@NonNull List<String> iconUrls) {
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
    public static Image getIcon(@NonNull String iconUrl) {
        InputStream stream;
        WeakReference<byte[]> reference = ICON_CACHE.get(iconUrl);
        if (reference == null || reference.get() == null) {
            stream = ResourceUtil.getResourceAsStream(iconUrl);
            if (stream != null) {
                byte[] bytes = IOUtil.readBytes(stream);
                ICON_CACHE.put(iconUrl, new WeakReference<>(bytes));
                stream = IOUtil.toStream(bytes);
            }
        } else {
            stream = IOUtil.toStream(reference.get());
            JulLog.info("load icon form cache.");
        }
        return stream == null ? null : new Image(stream);
    }
}
