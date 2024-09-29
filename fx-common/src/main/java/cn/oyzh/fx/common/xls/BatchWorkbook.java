package cn.oyzh.fx.common.xls;

import cn.oyzh.fx.common.util.IOUtil;
import lombok.NonNull;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.CellReferenceType;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/8/29
 */
public class BatchWorkbook implements Workbook {

    /**
     * 文件
     */
    private final File file;

    /**
     * 工作薄
     */
    private Workbook workbook;

    /**
     * 是否xlsx
     */
    private final boolean isXlsx;

    /**
     * 是否首次执行
     */
    private boolean firstAppend = true;

    public BatchWorkbook(File file) throws IOException {
        this(false, file);
    }

    public BatchWorkbook(@NonNull String filePath) throws IOException {
        this(false, new File(filePath));
    }

    public BatchWorkbook(boolean isXlsx, @NonNull String filePath) throws IOException {
        this(isXlsx, new File(filePath));
    }

    public BatchWorkbook(boolean isXlsx, @NonNull File file) throws IOException {
        this.isXlsx = isXlsx;
        this.file = file;
        this.initWorkbook();
    }

    private boolean isClosed = true;

    protected void initWorkbook() throws IOException {
        if (this.isClosed) {
            if (this.isXlsx) {
                if (this.firstAppend) {
                    this.workbook = new XSSFWorkbook();
                } else {
                    this.workbook = new XSSFWorkbook(new FileInputStream(this.file));
                }
            } else {
                if (this.firstAppend) {
                    this.workbook = new HSSFWorkbook();
                } else {
                    this.workbook = new HSSFWorkbook(new POIFSFileSystem(this.file, true), true);
                }
            }
            this.isClosed = false;
        }
    }

    private void closeWorkbook() throws IOException {
        this.isClosed = true;
        if (this.workbook != null) {
            this.workbook.close();
        }
    }

    /**
     * 追加数据
     *
     * @throws IOException 异常
     */
    public void append() throws IOException {
        this.initWorkbook();
        FileOutputStream xlsOutput = new FileOutputStream(this.file);
        this.workbook.write(xlsOutput);
        this.closeWorkbook();
        IOUtil.close(xlsOutput);
        this.firstAppend = false;
    }

    @Override
    public void close() throws IOException {
        this.append();
    }

    @Override
    public int getActiveSheetIndex() {
        return this.workbook.getActiveSheetIndex();
    }

    @Override
    public void setActiveSheet(int sheetIndex) {
        this.workbook.setActiveSheet(sheetIndex);
    }

    @Override
    public int getFirstVisibleTab() {
        return this.workbook.getFirstVisibleTab();
    }

    @Override
    public void setFirstVisibleTab(int sheetIndex) {
        this.workbook.setFirstVisibleTab(sheetIndex);
    }

    @Override
    public void setSheetOrder(String sheetname, int pos) {
        this.workbook.setSheetOrder(sheetname, pos);
    }

    @Override
    public void setSelectedTab(int index) {
        this.workbook.setSelectedTab(index);
    }

    @Override
    public void setSheetName(int sheet, String name) {
        this.workbook.setSheetName(sheet, name);
    }

    @Override
    public String getSheetName(int sheet) {
        return this.workbook.getSheetName(sheet);
    }

    @Override
    public int getSheetIndex(String name) {
        return this.workbook.getSheetIndex(name);
    }

    @Override
    public int getSheetIndex(Sheet sheet) {
        return this.workbook.getSheetIndex(sheet);
    }

    @Override
    public Sheet createSheet() {
        return this.workbook.createSheet();
    }

    @Override
    public Sheet createSheet(String sheetname) {
        return this.workbook.createSheet(sheetname);
    }

    @Override
    public Sheet cloneSheet(int sheetNum) {
        return this.workbook.cloneSheet(sheetNum);
    }

    @Override
    public Iterator<Sheet> sheetIterator() {
        return this.workbook.sheetIterator();
    }

    @Override
    public int getNumberOfSheets() {
        return this.workbook.getNumberOfSheets();
    }

    @Override
    public Sheet getSheetAt(int index) {
        return this.workbook.getSheetAt(index);
    }

    @Override
    public Sheet getSheet(String name) {
        return this.workbook.getSheet(name);
    }

    @Override
    public void removeSheetAt(int index) {
        this.workbook.removeSheetAt(index);
    }

    @Override
    public Font createFont() {
        return this.workbook.createFont();
    }

    @Override
    public Font findFont(boolean bold, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline) {
        return this.workbook.findFont(bold, color, fontHeight, name, italic, strikeout, typeOffset, underline);
    }

    @Override
    public int getNumberOfFonts() {
        return this.workbook.getNumberOfFonts();
    }

    @Override
    public int getNumberOfFontsAsInt() {
        return this.workbook.getNumberOfFontsAsInt();
    }

    @Override
    public Font getFontAt(int idx) {
        return this.workbook.getFontAt(idx);
    }

    @Override
    public CellStyle createCellStyle() {
        return this.workbook.createCellStyle();
    }

    @Override
    public int getNumCellStyles() {
        return this.workbook.getNumCellStyles();
    }

    @Override
    public CellStyle getCellStyleAt(int idx) {
        return this.workbook.getCellStyleAt(idx);
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNumberOfNames() {
        return this.workbook.getNumberOfNames();
    }

    @Override
    public Name getName(String name) {
        return this.workbook.getName(name);
    }

    @Override
    public List<? extends Name> getNames(String name) {
        return this.workbook.getNames(name);
    }

    @Override
    public List<? extends Name> getAllNames() {
        return this.workbook.getAllNames();
    }

    @Override
    public Name createName() {
        return this.workbook.createName();
    }

    @Override
    public void removeName(Name name) {
        this.workbook.removeName(name);
    }

    @Override
    public int linkExternalWorkbook(String name, Workbook workbook) {
        return this.workbook.linkExternalWorkbook(name, workbook);
    }

    @Override
    public void setPrintArea(int sheetIndex, String reference) {
        this.workbook.setPrintArea(sheetIndex, reference);
    }

    @Override
    public void setPrintArea(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow) {
        this.workbook.setPrintArea(sheetIndex, startColumn, endColumn, startRow, endRow);
    }

    @Override
    public String getPrintArea(int sheetIndex) {
        return this.workbook.getPrintArea(sheetIndex);
    }

    @Override
    public void removePrintArea(int sheetIndex) {
        this.workbook.removePrintArea(sheetIndex);
    }

    @Override
    public Row.MissingCellPolicy getMissingCellPolicy() {
        return this.workbook.getMissingCellPolicy();
    }

    @Override
    public void setMissingCellPolicy(Row.MissingCellPolicy missingCellPolicy) {
        this.workbook.setMissingCellPolicy(missingCellPolicy);
    }

    @Override
    public DataFormat createDataFormat() {
        return this.workbook.createDataFormat();
    }

    @Override
    public int addPicture(byte[] pictureData, int format) {
        return this.workbook.addPicture(pictureData, format);
    }

    @Override
    public List<? extends PictureData> getAllPictures() {
        return this.workbook.getAllPictures();
    }

    @Override
    public CreationHelper getCreationHelper() {
        return this.workbook.getCreationHelper();
    }

    @Override
    public boolean isHidden() {
        return this.workbook.isHidden();
    }

    @Override
    public void setHidden(boolean hiddenFlag) {
        this.workbook.setHidden(hiddenFlag);
    }

    @Override
    public boolean isSheetHidden(int sheetIx) {
        return this.workbook.isSheetHidden(sheetIx);
    }

    @Override
    public boolean isSheetVeryHidden(int sheetIx) {
        return this.workbook.isSheetVeryHidden(sheetIx);
    }

    @Override
    public void setSheetHidden(int sheetIx, boolean hidden) {
        this.workbook.setSheetHidden(sheetIx, hidden);
    }

    @Override
    public SheetVisibility getSheetVisibility(int sheetIx) {
        return this.workbook.getSheetVisibility(sheetIx);
    }

    @Override
    public void setSheetVisibility(int sheetIx, SheetVisibility visibility) {
        this.workbook.setSheetVisibility(sheetIx, visibility);
    }

    @Override
    public void addToolPack(UDFFinder toolpack) {
        this.workbook.addToolPack(toolpack);
    }

    @Override
    public void setForceFormulaRecalculation(boolean value) {
        this.workbook.setForceFormulaRecalculation(value);
    }

    @Override
    public boolean getForceFormulaRecalculation() {
        return this.workbook.getForceFormulaRecalculation();
    }

    @Override
    public SpreadsheetVersion getSpreadsheetVersion() {
        return this.workbook.getSpreadsheetVersion();
    }

    @Override
    public int addOlePackage(byte[] oleData, String label, String fileName, String command) throws IOException {
        return this.workbook.addOlePackage(oleData, label, fileName, command);
    }

    @Override
    public EvaluationWorkbook createEvaluationWorkbook() {
        return this.workbook.createEvaluationWorkbook();
    }

    @Override
    public CellReferenceType getCellReferenceType() {
        return this.workbook.getCellReferenceType();
    }

    @Override
    public void setCellReferenceType(CellReferenceType cellReferenceType) {
        this.workbook.setCellReferenceType(cellReferenceType);
    }
}
