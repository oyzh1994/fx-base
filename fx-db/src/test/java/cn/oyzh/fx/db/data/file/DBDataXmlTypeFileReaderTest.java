package cn.oyzh.fx.db.data.file;

import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * XML 数据读取器测试。
 */
public class DBDataXmlTypeFileReaderTest {

    @Test
    public void appendsAllCharacterEventsForEntityEncodedValues() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <RECORDS>
                  <RECORD>
                    <C_JSON>{&quot;id&quot;:10,&quot;ok&quot;:true}</C_JSON>
                    <C_XML>&lt;root&gt;&amp;ok&lt;/root&gt;</C_XML>
                  </RECORD>
                </RECORDS>
                """;
        Path file = Files.createTempFile("oracle-xml-reader-", ".xml");
        Files.writeString(file, xml, StandardCharsets.UTF_8);
        DBDataXmlTypeFileReader reader = new DBDataXmlTypeFileReader(
                file.toFile(), new DBDataImportConfig());
        try {
            Map<String, Object> record = reader.readRecord();
            assertEquals("{\"id\":10,\"ok\":true}", record.get("C_JSON"));
            assertEquals("<root>&ok</root>", record.get("C_XML"));
            assertNull(reader.readRecord());
        } finally {
            reader.close();
            Files.deleteIfExists(file);
        }
    }
}
