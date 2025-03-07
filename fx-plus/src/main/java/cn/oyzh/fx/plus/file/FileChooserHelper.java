package cn.oyzh.fx.plus.file;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 文件选择器
 *
 * @author oyzh
 * @since 2020/10/21
 */
public class FileChooserHelper {


    /**
     * 保存文件
     *
     * @param title        标题
     * @param initFileName 初始文件名称
     * @param desc         描述
     * @param extension    扩展名
     */
    public static File save(String title, String initFileName, String desc, String extension) {
        return save(title, initFileName, List.of(new FileExtensionFilter(desc, extension)), new Stage());
    }

    /**
     * 保存文件
     *
     * @param title        标题
     * @param initFileName 初始文件名称
     * @param filter       过滤器
     */
    public static File save(String title, String initFileName, FileExtensionFilter filter) {
        AtomicReference<File> reference = new AtomicReference<>();
        FXUtil.runWait(() -> {
            File file = save(title, initFileName, List.of(filter), new Stage());
            reference.set(file);
        });
        return reference.get();
    }

    /**
     * 保存文件
     *
     * @param title        标题
     * @param initFileName 初始文件名称
     * @param filters      过滤器
     */
    public static File save(String title, String initFileName, FileExtensionFilter... filters) {
        AtomicReference<File> reference = new AtomicReference<>();
        FXUtil.runWait(() -> {
            File file = save(title, initFileName, List.of(filters), new Stage());
            reference.set(file);
        });
        return reference.get();
    }

    /**
     * 保存文件
     *
     * @param title        标题
     * @param initFileName 初始文件名称
     * @param filters      过滤器
     * @param owner        父窗口
     */
    public static File save(String title, String initFileName, List<FileExtensionFilter> filters, Window owner) {
        FXFileChooser chooser = new FXFileChooser();
        chooser.title(title).addFilters(filters).initialFileName(initFileName);
        return chooser.showSaveDialog(owner);
    }

    /**
     * 选择文件
     *
     * @param title     标题
     * @param desc      描述
     * @param extension 扩展名
     */
    public static File choose(String title, String desc, String extension) {
        return choose(title, List.of(new FileExtensionFilter(desc, extension)), new Stage());
    }

    /**
     * 选择文件
     *
     * @param title  标题
     * @param filter 过滤器
     * @return 文件
     */
    public static File choose(String title, FileExtensionFilter filter) {
        return choose(title, List.of(filter), null);
    }

    /**
     * 选择文件
     *
     * @param title   标题
     * @param filters 过滤器
     * @return 文件
     */
    public static File choose(String title, FileExtensionFilter... filters) {
        return choose(title, List.of(filters), null);
    }

    /**
     * 选择文件
     *
     * @param title   标题
     * @param filters 过滤器
     * @param owner   父窗口
     * @return 文件
     */
    public static File choose(String title, List<FileExtensionFilter> filters, Window owner) {
        FXFileChooser chooser = new FXFileChooser();
        chooser.title(title).addFilters(filters);
        return chooser.showOpenDialog(owner);
    }

    /**
     * 选择文件
     *
     * @param title   标题
     * @param filters 过滤器
     * @return 文件
     */
    public static List<File> chooseMultiple(String title, FileExtensionFilter... filters) {
        return chooseMultiple(title, List.of(filters), null);
    }

    /**
     * 选择文件
     *
     * @param title   标题
     * @param filters 过滤器
     * @param owner   父窗口
     * @return 文件
     */
    public static List<File> chooseMultiple(String title, List<FileExtensionFilter> filters, Window owner) {
        FXFileChooser chooser = new FXFileChooser();
        chooser.title(title).addFilters(filters);
        return chooser.showOpenMultipleDialog(owner);
    }

//    /**
//     * 获取类型过滤器
//     *
//     * @param type 类型
//     * @return 类型过滤器
//     */
//    public static FileExtensionFilter extensionFilter(String type) {
//        if (StringUtil.equalsAnyIgnoreCase("sql", type)) {
//            return sqlExtensionFilter();
//        }
//        if (StringUtil.equalsAnyIgnoreCase("txt", type)) {
//            return txtExtensionFilter();
//        }
//        if (StringUtil.equalsAnyIgnoreCase("json", type)) {
//            return jsonExtensionFilter();
//        }
//        if (StringUtil.equalsAnyIgnoreCase("xml", type)) {
//            return xmlExtensionFilter();
//        }
//        if (StringUtil.equalsAnyIgnoreCase("csv", type)) {
//            return csvExtensionFilter();
//        }
//        if (StringUtil.equalsAnyIgnoreCase("html", type)) {
//            return htmlExtensionFilter();
//        }
//        if (StringUtil.equalsAnyIgnoreCase("xls", type)) {
//            return xlsExtensionFilter();
//        }
//        if (StringUtil.equalsAnyIgnoreCase("xlsx", type)) {
//            return xlsxExtensionFilter();
//        }
//        if (StringUtil.equalsAnyIgnoreCase("excel", type)) {
//            return excelExtensionFilter();
//        }
//        return allExtensionFilter();
//    }
//
//    public static FileExtensionFilter sqlExtensionFilter() {
//        return new FileExtensionFilter(I18nHelper.sqlType(), "*.sql");
//    }
//
//    public static FileExtensionFilter txtExtensionFilter() {
//        return new FileExtensionFilter(I18nHelper.txtType(), "*.txt");
//    }
//
//    public static FileExtensionFilter xmlExtensionFilter() {
//        return new FileExtensionFilter(I18nHelper.xmlType(), "*.xml");
//    }
//
//    public static FileExtensionFilter csvExtensionFilter() {
//        return new FileExtensionFilter(I18nHelper.csvType(), "*.csv");
//    }
//
//    public static FileExtensionFilter htmlExtensionFilter() {
//        return new FileExtensionFilter(I18nHelper.htmlType(), "*.html");
//    }
//
//    public static FileExtensionFilter xlsExtensionFilter() {
//        return new FileExtensionFilter(I18nHelper.xlsType(), "*.xls");
//    }
//
//    public static FileExtensionFilter xlsxExtensionFilter() {
//        return new FileExtensionFilter(I18nHelper.xlsxType(), "*.xlsx");
//    }
//
//    public static FileExtensionFilter excelExtensionFilter() {
//        return new FileExtensionFilter(I18nHelper.excelType(), "*.xls", "*.xlsx");
//    }
//
//    public static FileExtensionFilter allExtensionFilter() {
//        return new FileExtensionFilter(I18nHelper.allType(), "*.*");
//    }
//
//    public static FileExtensionFilter jsonExtensionFilter() {
//        return new FileExtensionFilter(I18nHelper.jsonType(), "*.json");
//    }
}
