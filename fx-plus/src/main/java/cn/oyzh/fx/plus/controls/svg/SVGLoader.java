package cn.oyzh.fx.plus.controls.svg;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.plus.util.ResourceUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.net.URL;
import java.util.Iterator;

/**
 * svg加载器
 *
 * @author oyzh
 * @since 2022/5/30
 */
public class SVGLoader {

    /**
     * 当前实例
     */
    public final static SVGLoader INSTANCE = new SVGLoader();

    /**
     * 缓存
     */
    private final TimedCache<String, String> cache = CacheUtil.newTimedCache(60 * 1000L);

    /**
     * 加载svg内容
     *
     * @param svgPath svg路径
     * @return 结果
     */
    public String load(String svgPath) {
        if (svgPath == null) {
            return null;
        }
        svgPath = svgPath.trim();
        // 缓存
        if (this.cache.containsKey(svgPath)) {
            return this.cache.get(svgPath);
        }
        // 获取路径
        URL url = ResourceUtil.getResource(svgPath);
        if (url == null) {
            StaticLog.warn("svgPath: {} is not found.", svgPath);
            return null;
        }
        String svg = null;
        try {
            // 解析内容
            SAXReader reader = new SAXReader();
            Document document = reader.read(url.openStream());
            Element root = document.getRootElement();
            Iterator<Element> iterator = root.elementIterator("path");
            if (iterator == null || !iterator.hasNext()) {
                Element element = root.element("g");
                if (element != null) {
                    iterator = element.elementIterator("path");
                }
            }
            if (iterator != null) {
                StringBuilder data = new StringBuilder();
                while (iterator.hasNext()) {
                    Element element = iterator.next();
                    String d = element.attributeValue("d");
                    if (StrUtil.isNotBlank(d)) {
                        data.append(d);
                    }
                }
                svg = data.toString().trim();
            }
            // 添加到缓存
            this.cache.put(svgPath, svg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return svg;
    }
}
