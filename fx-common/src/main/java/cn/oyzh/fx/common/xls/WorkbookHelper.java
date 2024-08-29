package cn.oyzh.fx.common.xls;

import lombok.experimental.UtilityClass;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author oyzh
 * @since 2024/8/29
 */
@UtilityClass
public class WorkbookHelper {

    public static Workbook create(boolean isXlsx) throws IOException {
        Workbook workbook;
        if (isXlsx) {
            workbook = new XSSFWorkbook();
        } else {
            workbook = new HSSFWorkbook();
        }
        return workbook;
    }

    public static Workbook create(boolean isXlsx, String filePath) throws IOException, InvalidFormatException {
        Workbook workbook;
        if (isXlsx) {
            workbook = new XSSFWorkbook(new FileInputStream(filePath));
        } else {
            workbook = new HSSFWorkbook(new FileInputStream(filePath));
        }
        return workbook;
    }

    public static void write(Workbook workbook, String filePath) throws IOException {
        FileOutputStream xlsOutput = new FileOutputStream(filePath);
        workbook.write(xlsOutput);
        xlsOutput.close();
    }

    public static Sheet getActiveSheet(Workbook workbook) {
        return workbook.getSheetAt(workbook.getActiveSheetIndex());
    }
}
