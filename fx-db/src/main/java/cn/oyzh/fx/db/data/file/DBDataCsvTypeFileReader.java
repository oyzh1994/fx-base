package cn.oyzh.fx.db.data.file;

import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import cn.oyzh.fx.db.data.file.csv.DBDataCsvFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 基于 Apache Commons CSV 的数据库数据导入读取器。
 *
 * <p>列索引和数据起始索引均沿用原有配置，以 CSV 记录序号从 0 开始计算。</p>
 *
 * @author oyzh
 * @since 2026-09-07
 */
public class DBDataCsvTypeFileReader extends DBDataTypeFileReader {

    /**
     * CSV 解析器
     */
    private final CSVParser parser;

    /**
     * CSV 记录迭代器
     */
    private final Iterator<CSVRecord> records;

    /**
     * 字段列表
     */
    private List<String> columns;

    /**
     * 下一条数据记录的索引
     */
    private int nextRecordIndex;

    /**
     * 构造 CSV 文件读取器。
     *
     * @param file   待读取文件
     * @param config 导入配置
     * @throws IOException IO 异常
     */
    public DBDataCsvTypeFileReader(File file, DBDataImportConfig config) throws IOException {
        this(file, config, ",");
    }

    /**
     * 构造定界文本文件读取器。
     *
     * @param file           待读取文件
     * @param config         导入配置
     * @param fieldSeparator 字段分隔符
     * @throws IOException IO 异常
     */
    protected DBDataCsvTypeFileReader(File file, DBDataImportConfig config, String fieldSeparator) throws IOException {
        super(file);
        Charset charset = Charset.forName(config.getCharset());
        Reader reader = Files.newBufferedReader(file.toPath(), charset);
        boolean initialized = false;
        CSVParser csvParser = null;
        try {
            csvParser = DBDataCsvFormat.create(fieldSeparator, config.getTxtIdentifier(), config.getRecordSeparator()).parse(reader);
            this.parser = csvParser;
            this.records = csvParser.iterator();
            this.init(config);
            initialized = true;
        } finally {
            if (!initialized) {
                if (csvParser != null) {
                    csvParser.close();
                } else {
                    reader.close();
                }
            }
        }
    }

    /**
     * 读取列名并定位到数据起始位置。
     *
     * @param config 导入配置
     * @throws IOException IO 异常或 CSV 内容不符合配置
     */
    private void init(DBDataImportConfig config) throws IOException {
        this.skipTo(config.getColumnIndex());
        if (!this.records.hasNext()) {
            throw new IOException("CSV 文件中不存在索引为 " + config.getColumnIndex() + " 的列名记录");
        }
        CSVRecord header = this.records.next();
        this.nextRecordIndex++;
        List<String> names = new ArrayList<>(header.size());
        for (String name : header) {
            names.add(name);
        }
        this.columns = Collections.unmodifiableList(names);
        this.skipTo(config.getDataStartIndex());
    }

    /**
     * 将迭代器定位到指定记录索引。
     *
     * @param recordIndex 记录索引
     * @throws IOException IO 异常
     */
    private void skipTo(int recordIndex) throws IOException {
        while (this.nextRecordIndex < recordIndex) {
            if (!this.records.hasNext()) {
                throw new IOException("CSV 文件中不存在索引为 " + recordIndex + " 的记录");
            }
            this.records.next();
            this.nextRecordIndex++;
        }
    }

    @Override
    public Map<String, Object> readObject() throws IOException {
        if (!this.records.hasNext()) {
            return null;
        }
        CSVRecord record = this.records.next();
        this.nextRecordIndex++;
        if (record.size() != this.columns.size()) {
            throw new IOException("CSV 第 " + record.getRecordNumber() + " 条记录包含 " + record.size()
                    + " 个字段，但列名包含 " + this.columns.size() + " 个字段");
        }
        Map<String, Object> object = new HashMap<>();
        for (int index = 0; index < this.columns.size(); index++) {
            object.put(this.columns.get(index), record.get(index));
        }
        return object;
    }

    @Override
    public void close() throws IOException {
        this.parser.close();
        this.columns = null;
    }
}
