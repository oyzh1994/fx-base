package cn.oyzh.fx.db.data.file;

import cn.oyzh.store.file.FileTextUtil;
import cn.oyzh.store.file.TypeFileReader;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据类型文件读取器基类，定义将文件内容按类型解析为数据对象的通用能力。
 *
 * <p>记录类型为按列名映射的 {@code Map<String, Object>}，与 base-store 的
 * {@code FileRecord}（按下标映射）不同，因此以泛型参数区分。</p>
 *
 * @author oyzh
 * @since 2026-09-07
 */
public abstract class DBDataTypeFileReader extends TypeFileReader<Map<String, Object>> {

    /**
     * 待读取的文件
     */
    private File file;

    /**
     * 构造数据库数据类型文件读取器对象。
     */
    public DBDataTypeFileReader() {

    }

    /**
     * 构造数据库数据类型文件读取器对象。
     *
     * @param file 文件
     */
    public DBDataTypeFileReader(File file) {
        this.file = file;
    }

    /**
     * 获取文件。
     *
     * @return 文件
     */
    public File getFile() {
        return this.file;
    }

//    /**
//     * 初始化读取器
//     *
//     * @throws Exception 异常
//     */
//    protected void init() throws Exception {
//
//    }
//
//    /**
//     * 读取单个数据对象
//     *
//     * @return 数据对象，读取完毕返回 null
//     * @throws Exception 异常
//     */
//    public abstract Map<String, Object> readObject() throws Exception;
//
//    /**
//     * 批量读取指定数量的数据对象
//     *
//     * @param count 读取数量
//     * @return 数据对象列表
//     * @throws Exception 异常
//     */
//    public List<Map<String, Object>> readObjects(int count) throws Exception {
//        // 数据列表
//        List<Map<String, Object>> records = new ArrayList<>();
//        // 读取数据
//        while (records.size() < count) {
//            Map<String, Object> item = this.readObject();
//            if (item == null) {
//                break;
//            }
//            records.add(item);
//        }
//        return records;
//    }

    /**
     * 解析单行文本为字段列表
     *
     * @param line           行文本
     * @param txtIdentifier  文本识别符
     * @param fieldSeparator 字段分割符
     * @return 字段列表
     * @throws IOException IO异常
     */
    protected List<String> parseLine(String line, char txtIdentifier, char fieldSeparator) throws IOException {
        return FileTextUtil.parseLine(line, txtIdentifier, fieldSeparator);
//        List<String> list = new ArrayList<>();
//        StringBuilder sb = new StringBuilder();
//        boolean txtStart = false;
//        Character lastChar = null;
//        try (StringReader reader = new StringReader(line)) {
//            while (reader.ready()) {
//                int i = reader.read();
//                if (i == -1) {
//                    break;
//                }
//                char c = (char) i;
//                try {
//                    if (txtStart && c == fieldSeparator) {
//                        txtStart = false;
//                        continue;
//                    }
//                    if (c == txtIdentifier) {
//                        if (lastChar != null && lastChar == '\\') {
//                            sb.append(c);
//                            continue;
//                        }
//                        if (txtStart) {
//                            list.add(sb.toString());
//                            sb.delete(0, sb.length());
//                        } else {
//                            txtStart = true;
//                        }
//                    } else if (txtStart) {
//                        sb.append(c);
//                    }
//                } finally {
//                    lastChar = c;
//                }
//            }
//        }
//        return list;
    }

}
