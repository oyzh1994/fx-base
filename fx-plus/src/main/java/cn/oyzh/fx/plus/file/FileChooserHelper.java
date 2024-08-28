package cn.oyzh.fx.plus.file;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.List;

/**
 * 文件选择器
 *
 * @author oyzh
 * @since 2020/10/21
 */
public class FileChooserHelper {

    /**
     * 桌面路径
     */
    public static final File DESKTOP_DIR = FileSystemView.getFileSystemView().getHomeDirectory();

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
        return save(title, initFileName, List.of(filter), new Stage());
    }

    /**
     * 保存文件
     *
     * @param title        标题
     * @param initFileName 初始文件名称
     * @param filters      过滤器
     */
    public static File save(String title, String initFileName, FileExtensionFilter... filters) {
        return save(title, initFileName, List.of(filters), new Stage());
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

    public static FileExtensionFilter extensionFilter(String type) {
        if (StrUtil.equalsAnyIgnoreCase("sql", type)) {
            return sqlExtensionFilter();
        }
        if (StrUtil.equalsAnyIgnoreCase("txt", type)) {
            return txtExtensionFilter();
        }
        if (StrUtil.equalsAnyIgnoreCase("json", type)) {
            return jsonExtensionFilter();
        }
        if (StrUtil.equalsAnyIgnoreCase("xml", type)) {
            return xmlExtensionFilter();
        }
        return allExtensionFilter();
    }

    public static FileExtensionFilter sqlExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.sqlType(), "*.sql");
    }

    public static FileExtensionFilter txtExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.txtType(), "*.txt");
    }

    public static FileExtensionFilter xmlExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.xmlType(), "*.xml");
    }

    public static FileExtensionFilter allExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.allType(), "*.*");
    }

    public static FileExtensionFilter jsonExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.jsonType(), "*.json");
    }

}
