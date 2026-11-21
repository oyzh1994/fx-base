package cn.oyzh.fx.db.data.file;

import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * CSV 分隔符读取测试。
 *
 * @since 2026-11-22
 */
public class DBDataCsvTypeFileReaderTest {

    @Test
    public void detectsCommaWhenConfiguredSeparatorIsSemicolon() throws Exception {
        this.assertReads("\"id\",\"name\"\n\"1\",\"a,b\"\n", ",", "a,b");
    }

    @Test
    public void detectsSemicolonWhenConfiguredSeparatorIsComma() throws Exception {
        this.assertReads("\"id\";\"name\"\n\"1\";\"a;b\"\n", ";", "a;b");
    }

    private void assertReads(String content, String expectedSeparator, String expectedName)
            throws Exception {
        Path file = Files.createTempFile("easyshell-csv-separator-", ".csv");
        try {
            Files.writeString(file, content);
            DBDataImportConfig config = new DBDataImportConfig();
            config.setFieldSeparator(";".equals(expectedSeparator) ? "," : ";");
            config.setTxtIdentifier("\"");
            config.setRecordSeparator("\n");
            config.setColumnIndex(0);
            config.setDataRowStarts(2);
            try (DBDataCsvTypeFileReader reader =
                         new DBDataCsvTypeFileReader(file.toFile(), config)) {
                List<Map<String, Object>> records = reader.readRecords(1);
                assertEquals(1, records.size());
                assertEquals(expectedName, records.getFirst().get("name"));
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
