package cn.oyzh.fx.db.data.file;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * db类型文件写入器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public abstract class DBDataTypeFileWriter implements Closeable {

    /**
     * 初始化
     *
     * @throws Exception 异常
     */
    protected void init() throws Exception {

    }

    /**
     * 写入头
     *
     * @throws Exception 异常
     */
    public void writeHeader() throws Exception {

    }

    /**
     * 写入尾
     *
     * @throws Exception 异常
     */
    public void writeTrial() throws Exception {

    }

    /**
     * 写入对象
     *
     * @param object 对象
     * @throws Exception 异常
     */
    public abstract void writeObject(Map<String, Object> object) throws Exception;

    /**
     * 写入多个对象
     *
     * @param objects 对象
     * @throws Exception 异常
     */
    public void writeObjects(List<Map<String, Object>> objects) throws Exception {
        for (Map<String, Object> object : objects) {
            this.writeObject(object);
        }
    }

    /**
     * 格式化行
     *
     * @param objects         对象数组
     * @param fieldSeparator  字段分隔符
     * @param txtIdentifier   文本标识符
     * @param recordSeparator 记录分隔符
     * @return 格式化后的行
     */
    protected String formatLine(Object[] objects, String fieldSeparator, String txtIdentifier, String recordSeparator) {
        List<Object> list = new ArrayList<>();
        for (Object object : objects) {
            list.add(Objects.requireNonNullElse(object, ""));
        }
        //        List<?> list = CollectionUtil.of(objects);
        return this.formatLine(list, fieldSeparator, txtIdentifier, recordSeparator);
    }

    /**
     * 格式化行
     *
     * @param list            对象列表
     * @param fieldSeparator  字段分隔符
     * @param txtIdentifier   文本标识符
     * @param recordSeparator 记录分隔符
     * @return 格式化后的行
     */
    protected String formatLine(List<?> list, String fieldSeparator, String txtIdentifier, String recordSeparator) {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object val : list) {
            sb.append(fieldSeparator).append(txtIdentifier).append(val).append(txtIdentifier);
        }
        sb.append(recordSeparator);
        return sb.substring(1);
    }
}
