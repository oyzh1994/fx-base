package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.common.cache.CacheUtil;
import cn.oyzh.common.cache.TimedCache;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.xml.XMLDocument;
import cn.oyzh.common.xml.XMLElement;
import cn.oyzh.common.xml.XMLReader;

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
     * 加载svg路径
     *
     * @param url 路径
     * @return 结果
     */
    public FXSVGPath load(String url) {
        String content = this.loadContent(url);
        if (content != null) {
            return new FXSVGPath(content);
        }
        return null;
    }

    /**
     * 加载svg内容
     *
     * @param url 路径
     * @return 结果
     */
    public String loadContent(String url) {
        if (url == null) {
            return null;
        }
        // 返回常量池地址
        url = url.trim();
        // url = url.trim().intern();
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
        String svgPath = null;
        try (XMLReader reader = new XMLReader()) {
            // 解析内容
            XMLDocument document = reader.read(u.openStream());
            XMLElement root = document.getRootElement();
            Iterator<XMLElement> iterator = root.elementIterator("path");
            if (iterator == null || !iterator.hasNext()) {
                XMLElement element = root.element("g");
                if (element != null) {
                    iterator = element.elementIterator("path");
                }
            }
            StringBuilder content = new StringBuilder();
            if (iterator != null) {
                boolean first = true;
                while (iterator.hasNext()) {
                    XMLElement element = iterator.next();
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
            svgPath = content.toString();
            // 添加到缓存
            this.cache.put(url, svgPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return svgPath;
    }
}
