package cn.oyzh.fx.db.data.file;


import cn.oyzh.fx.db.data.dto.DBDataImportConfig;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * XML 类型文件读取器，基于 StAX 事件流将 XML 文件内容解析为数据对象。
 *
 * @author oyzh
 * @since 2026-09-07
 */
public class DBDataXmlTypeFileReader extends DBDataTypeFileReader {

    /**
     * xml读取器
     */
    private XMLEventReader reader;

    /**
     * 导入配置
     */
    private DBDataImportConfig config;

    /**
     * 构造方法
     *
     * @param file   待读取的文件
     * @param config 导入配置
     * @throws Exception 异常
     */
    public DBDataXmlTypeFileReader(File file, DBDataImportConfig config) throws Exception {
        super(file);
        this.config = config;
        this.reader = XMLInputFactory.newInstance().createXMLEventReader(new FileInputStream(file), config.getCharset());
        this.init();
    }

    @Override
    protected void init() throws Exception {
        while (this.reader.hasNext()) {
            XMLEvent event = this.reader.nextEvent();
            if (event.isStartElement()) {
                break;
            }
        }
    }

    @Override
    public Map<String, Object> readObject() throws Exception {
        String name = null;
        String value = null;
        Map<String, Object> map = null;
        boolean objStart = false;
        boolean childStart = false;
        while (this.reader.hasNext()) {
            XMLEvent event = this.reader.nextEvent();
            // 读取结束
            if (event.isEndElement() && objStart && !childStart) {
                break;
            }
            // 属性读取为字段
            if (this.config.isAttrToColumn()) {
                // 读取开始
                if (event.isStartElement() && !objStart) {
                    objStart = true;
                    StartElement element = event.asStartElement();
                    Iterator<Attribute> attributes = element.getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        if (map == null) {
                            map = new HashMap<>();
                        }
                        map.put(attribute.getName().getLocalPart(), attribute.getValue());
                    }
                }
            } else {// 属性为子节点
                // 读取开始
                if (event.isStartElement() && !objStart) {
                    objStart = true;
                    continue;
                }
                // 子节点开始
                if (event.isStartElement() && !childStart) {
                    childStart = true;
                    StartElement element = event.asStartElement();
                    name = element.getName().getLocalPart();
                    continue;
                }
                // 子节点结束
                if (event.isEndElement() && childStart) {
                    childStart = false;
                    if (map == null) {
                        map = new HashMap<>();
                    }
                    map.put(name, value);
                    name = null;
                    value = null;
                    continue;
                }
                // 子节点数据
                if (event.isCharacters() && name != null) {
                    value = event.asCharacters().getData();
                }
            }
        }
        return map;
    }

    @Override
    public void close() {
        try {
            if (this.reader != null) {
                this.reader.close();
                this.reader = null;
                this.config = null;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
