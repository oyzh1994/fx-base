package cn.oyzh.fx.db.data.dto;

import org.junit.Assert;
import org.junit.Test;

/**
 * 数据库数据配置继承测试
 *
 * <p>验证 {@link DBDataExportConfig} 与 {@link DBDataImportConfig} 正确继承
 * base-store 的文件配置，且重复配置项已收敛到父类。</p>
 *
 * @author oyzh
 * @since 2026-10-19
 */
public class DBDataConfigInheritTest {

    @Test
    public void testExportConfigInheritsFileWriteConfig() {
        DBDataExportConfig config = new DBDataExportConfig();

        // 父类默认值为redis/zk语义，子类构造器覆盖为数据库导出语义
        Assert.assertEquals(";", config.getFieldSeparator());
        Assert.assertTrue(config.isIncludeTitle());

        // 通用配置由父类提供
        Assert.assertEquals(System.lineSeparator(), config.getRecordSeparator());
        Assert.assertEquals("\"", config.getTxtIdentifier());
        Assert.assertEquals("UTF-8", config.getCharset());

        // 数据库导出特有配置
        Assert.assertFalse(config.isFieldToAttr());
        Assert.assertFalse(config.isEarlyVersion());
        Assert.assertFalse(config.isContinueWithError());
    }

    @Test
    public void testImportConfigInheritsFileReadConfig() {
        DBDataImportConfig config = new DBDataImportConfig();

        Assert.assertEquals(";", config.getFieldSeparator());
        Assert.assertEquals(System.lineSeparator(), config.getRecordSeparator());
        Assert.assertEquals("\"", config.getTxtIdentifier());
        Assert.assertEquals("UTF-8", config.getCharset());

        // 数据库导入特有配置
        Assert.assertTrue(config.isCopyMode());
        Assert.assertFalse(config.isAppendMode());
        Assert.assertEquals(0, config.getColumnIndex());
        Assert.assertNull(config.getRecordLabel());
        Assert.assertFalse(config.isAttrToColumn());
    }

    @Test
    public void testInheritedSettersTakeEffect() {
        DBDataExportConfig exportConfig = new DBDataExportConfig();
        exportConfig.setFieldSeparator(",");
        exportConfig.setTxtIdentifier("'");
        exportConfig.setRecordSeparator("\r\n");
        exportConfig.setCharset("GBK");
        exportConfig.setIncludeTitle(false);

        Assert.assertEquals(",", exportConfig.getFieldSeparator());
        Assert.assertEquals("'", exportConfig.getTxtIdentifier());
        Assert.assertEquals("\r\n", exportConfig.getRecordSeparator());
        Assert.assertEquals("GBK", exportConfig.getCharset());
        Assert.assertFalse(exportConfig.isIncludeTitle());

        DBDataImportConfig importConfig = new DBDataImportConfig();
        importConfig.setDataRowStarts(3);
        importConfig.setFieldSeparator("|");

        Assert.assertEquals(Integer.valueOf(3), importConfig.getDataRowStarts());
        Assert.assertEquals("|", importConfig.getFieldSeparator());
    }

    @Test
    public void testDataRowStartsIsOneBased() {
        DBDataImportConfig config = new DBDataImportConfig();
        config.setDataRowStarts(2);
        // dataRowStarts 为 1 基，读取器内部换算为 0 基记录序号
        Assert.assertEquals(Integer.valueOf(2), config.getDataRowStarts());
    }
}
