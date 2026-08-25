package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.xml.XMLDocument;
import cn.oyzh.common.xml.XMLElement;
import cn.oyzh.common.xml.XMLReader;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

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

    ///**
    // * 缓存
    // */
    //private final TimedCache<String, String> cache = CacheUtil.newTimedCache(60 * 1000L);

    /**
     * 加载svg路径
     *
     * @param url 路径
     * @return 结果
     */
    public FXSVGPath load(String url) {
        if (url == null) {
            return null;
        }
        // 获取路径
        URL u = ResourceUtil.getResource(url);
        if (u == null) {
            JulLog.warn("svg file: {} is not found.", url);
            return null;
        }
        String svgPath = null;
        String svgFill = null;
        String svgStroke = null;
        String svgOpacity = null;
        String svgStrokeWidth = null;
//        String svgWidth = null;
//        String svgHeight = null;
        try (XMLReader reader = new XMLReader()) {
            // 解析内容
            XMLDocument document = reader.read(u.openStream());
            XMLElement root = document.getRootElement();
//            svgWidth = root.attributeValue("width");
//            svgHeight = root.attributeValue("height");
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
                    String fill = element.attributeValue("fill");
                    if (StringUtil.isNotBlank(fill)) {
                        svgFill = fill;
                    }
                    String stroke = element.attributeValue("stroke");
                    if (StringUtil.isNotBlank(stroke)) {
                        svgStroke = stroke;
                    }
                    String opacity = element.attributeValue("opacity");
                    if (StringUtil.isNotBlank(opacity)) {
                        svgOpacity = opacity;
                    }
                    String strokeWidth = element.attributeValue("stroke-width");
                    if (StringUtil.isNotBlank(strokeWidth)) {
                        svgStrokeWidth = strokeWidth;
                    }
                }
            }
            if (content.isEmpty()) {
                return null;
            }
            svgPath = content.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (svgPath == null) {
            return null;
        }
        FXSVGPath path = new FXSVGPath(svgPath);
        if (svgFill != null) {
            if (svgFill.startsWith("#")) {
                path.setFill(Paint.valueOf(svgFill));
            } else {
                int[] arr = parseRgb(svgFill);
                if (arr != null) {
                    if (svgOpacity != null) {
                        path.setFill(Color.rgb(arr[0], arr[1], arr[2], Integer.parseInt(svgOpacity)));
                    } else {
                        path.setFill(Color.rgb(arr[0], arr[1], arr[2]));
                    }
                }
            }
        }
        if (svgStroke != null) {
            if (svgStroke.startsWith("#")) {
                path.setStroke(Paint.valueOf(svgStroke));
            } else {
                int[] arr = parseRgb(svgStroke);
                if (arr != null) {
                    if (svgOpacity != null) {
                        path.setStroke(Color.rgb(arr[0], arr[1], arr[2], Integer.parseInt(svgOpacity)));
                    } else {
                        path.setStroke(Color.rgb(arr[0], arr[1], arr[2]));
                    }
                }
            }
        }
        if (svgStrokeWidth != null) {
            path.setStrokeWidth(Double.parseDouble(svgStrokeWidth));
        }
//        if (svgWidth != null && svgHeight != null) {
//            path.setProp("width", Double.parseDouble(svgWidth));
//            path.setProp("height", Double.parseDouble(svgHeight));
//        }
        return path;
    }

    private int[] parseRgb(String rgb) {
        try {
            String[] arr = rgb.substring(rgb.indexOf("(") + 1, rgb.indexOf(")")).split(",");
            int r = Integer.parseInt(arr[0]);
            int g = Integer.parseInt(arr[1]);
            int b = Integer.parseInt(arr[2]);
            return new int[]{r, g, b};
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    ///**
    // * 加载svg内容
    // *
    // * @param url 路径
    // * @return 结果
    // */
    //public String loadContent(String url) {
    //    if (url == null) {
    //        return null;
    //    }
    //    // 返回常量池地址
    //    url = url.trim();
    //    // url = url.trim().intern();
    //    // 缓存
    //    if (this.cache.containsKey(url)) {
    //        return this.cache.get(url);
    //    }
    //    // 获取路径
    //    URL u = ResourceUtil.getResource(url);
    //    if (u == null) {
    //        JulLog.warn("svg file: {} is not found.", url);
    //        return null;
    //    }
    //    String svgPath = null;
    //    try (XMLReader reader = new XMLReader()) {
    //        // 解析内容
    //        XMLDocument document = reader.read(u.openStream());
    //        XMLElement root = document.getRootElement();
    //        Iterator<XMLElement> iterator = root.elementIterator("path");
    //        if (iterator == null || !iterator.hasNext()) {
    //            XMLElement element = root.element("g");
    //            if (element != null) {
    //                iterator = element.elementIterator("path");
    //            }
    //        }
    //        StringBuilder content = new StringBuilder();
    //        if (iterator != null) {
    //            boolean first = true;
    //            while (iterator.hasNext()) {
    //                XMLElement element = iterator.next();
    //                String d = element.attributeValue("d");
    //                if (StringUtil.isNotBlank(d)) {
    //                    if (first) {
    //                        first = false;
    //                        content.append(d);
    //                    } else {
    //                        content.append(" ").append(d);
    //                    }
    //                }
    //            }
    //        }
    //        if (content.isEmpty()) {
    //            return null;
    //        }
    //        svgPath = content.toString();
    //        // 添加到缓存
    //        this.cache.put(url, svgPath);
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //    return svgPath;
    //}
}
