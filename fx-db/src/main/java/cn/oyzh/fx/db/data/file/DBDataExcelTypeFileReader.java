package cn.oyzh.fx.db.data.file;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.xls.WorkbookHelper;
import cn.oyzh.fx.db.data.dto.DBDataImportConfig;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel 类型文件读取器，基于 POI 工作簿将 Excel 文件内容解析为数据对象。
 *
 * @author oyzh
 * @since 2026-09-07
 */
public class DBDataExcelTypeFileReader extends DBDataTypeFileReader {

    /**
     * Excel 工作簿
     */
    private Workbook workbook;

    /**
     * 字段列表
     */
    private List<String> columns;

    /**
     * 导入配置
     */
    private DBDataImportConfig config;

    /**
     * 当前行索引
     */
    private Integer currentRowIndex;

    /**
     * 构造方法
     *
     * @param file   待读取的文件
     * @param config 导入配置
     * @throws Exception 异常
     */
    public DBDataExcelTypeFileReader(File file, DBDataImportConfig config) throws Exception {
        super(file);
        this.config = config;
        boolean isXlsx = StringUtil.endWithIgnoreCase(file.getName(), ".xlsx");
        this.workbook = WorkbookHelper.create(isXlsx, file);
        this.init();
    }

    @Override
    protected void init() throws Exception {
        this.columns = new ArrayList<>();
        Row row = this.workbook.getSheetAt(0).getRow(this.config.getColumnIndex());
        for (Cell cell : row) {
            this.columns.add(cell.getStringCellValue());
        }
        this.currentRowIndex = this.config.getDataStartIndex();
    }

    @Override
    public Map<String, Object> readObject() {
        Sheet sheet = this.workbook.getSheetAt(0);
        Row row = sheet.getRow(this.currentRowIndex++);
        if (row != null) {
            Map<String, Object> map = new HashMap<>();
            for (Cell cell : row) {
                Object val;
                CellType cellType = cell.getCellType();
                if (cellType == CellType.BOOLEAN) {
                    val = cell.getBooleanCellValue();
                } else if (cellType == CellType.NUMERIC) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        val = cell.getDateCellValue();
                    } else {
                        val = cell.getNumericCellValue();
                    }
                } else if (cellType == CellType.STRING) {
                    val = cell.getStringCellValue();
                } else {
                    val = cell.getStringCellValue();
                }
                String columnName = this.columns.get(cell.getColumnIndex());
                map.put(columnName, val);
            }
            return map;
        }
        return null;
    }

    @Override
    public void close() {
        try {
            if (this.workbook != null) {
                this.workbook.close();
                this.workbook = null;
                this.config = null;
                this.columns = null;
                this.currentRowIndex = null;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
