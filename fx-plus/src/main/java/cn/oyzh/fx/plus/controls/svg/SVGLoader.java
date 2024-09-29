package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.common.cache.CacheUtil;
import cn.oyzh.fx.common.cache.TimedCache;
import cn.oyzh.fx.common.log.JulLog;
import cn.oyzh.fx.common.util.ResourceUtil;
import cn.oyzh.fx.common.util.StringUtil;
import javafx.scene.shape.SVGPath;
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
    private final TimedCache<String, SVGPath> cache = CacheUtil.newTimedCache(60 * 1000L);

    /**
     * 加载svg内容
     *
     * @param url 路径
     * @return 结果
     */
    public SVGPath load(String url) {
        if (url == null) {
            return null;
        }
        // 返回常量池地址
        url = url.trim().intern();
        // 缓存
        if (this.cache.containsKey(url)) {
            return this.cache.get(url);
        }
        // 获取路径
        URL u = ResourceUtil.getResource(url);
        if (u == null) {
            JulLog.warn("svg file: {} is not found.", url);
            return null;
        }
        SVGPath svgPath = null;
        try {
            // 解析内容
            SAXReader reader = new SAXReader();
            Document document = reader.read(u.openStream());
            Element root = document.getRootElement();
            Iterator<Element> iterator = root.elementIterator("path");
            if (iterator == null || !iterator.hasNext()) {
                Element element = root.element("g");
                if (element != null) {
                    iterator = element.elementIterator("path");
                }
            }
            StringBuilder content = new StringBuilder();
            if (iterator != null) {
                boolean first = true;
                while (iterator.hasNext()) {
                    Element element = iterator.next();
                    String d = element.attributeValue("d");
                    if (StringUtil.isNotBlank(d)) {
                        if (first) {
                            first = false;
                            content.append(d);
                        } else {
                            content.append(" ").append(d);
                        }
                    }
                }
            }
            if (content.isEmpty()) {
                return null;
            }
            svgPath = new SVGPath();
            svgPath.setContent(content.toString());
            // 添加到缓存
            this.cache.put(url, svgPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return svgPath;
    }
}
