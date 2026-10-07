package cn.oyzh.fx.plus.chooser;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.i18n.I18nHelper;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * 文件选择器工具类，提供常用文件类型过滤器及常用目录的获取
 *
 * @author oyzh
 * @since 2025-03-07
 */
public class FXChooser {

    /**
     * 桌面路径
     */
    public static final File HOME_DIR = FileSystemView.getFileSystemView().getHomeDirectory();

    /**
     * 获取类型过滤器
     *
     * @param type 类型
     * @return 类型过滤器
     */
    public static FileExtensionFilter extensionFilter(String type) {
        if (StringUtil.equalsAnyIgnoreCase("sql", type)) {
            return sqlExtensionFilter();
        }
        if (StringUtil.equalsAnyIgnoreCase("txt", type)) {
            return txtExtensionFilter();
        }
        if (StringUtil.equalsAnyIgnoreCase("json", type)) {
            return jsonExtensionFilter();
        }
        if (StringUtil.equalsAnyIgnoreCase("xml", type)) {
            return xmlExtensionFilter();
        }
        if (StringUtil.equalsAnyIgnoreCase("csv", type)) {
            return csvExtensionFilter();
        }
        if (StringUtil.equalsAnyIgnoreCase("html", type)) {
            return htmlExtensionFilter();
        }
        if (StringUtil.equalsAnyIgnoreCase("xls", type)) {
            return xlsExtensionFilter();
        }
        if (StringUtil.equalsAnyIgnoreCase("xlsx", type)) {
            return xlsxExtensionFilter();
        }
        if (StringUtil.equalsAnyIgnoreCase("excel", type)) {
            return excelExtensionFilter();
        }
        if (StringUtil.equalsAnyIgnoreCase("js", type)) {
            return jsExtensionFilter();
        }
        return allExtensionFilter();
    }

    /**
     * 获取js类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter jsExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.jsType(), "*.js");
    }

    /**
     * 获取sql类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter sqlExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.sqlType(), "*.sql");
    }

    /**
     * 获取txt类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter txtExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.txtType(), "*.txt");
    }

    /**
     * 获取xml类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter xmlExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.xmlType(), "*.xml");
    }

    /**
     * 获取csv类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter csvExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.csvType(), "*.csv");
    }

    /**
     * 获取html类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter htmlExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.htmlType(), "*.html");
    }

    /**
     * 获取xls类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter xlsExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.xlsType(), "*.xls");
    }

    /**
     * 获取xlsx类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter xlsxExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.xlsxType(), "*.xlsx");
    }

    /**
     * 获取excel类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter excelExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.excelType(), "*.xls", "*.xlsx");
    }

    /**
     * 获取word类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter wordExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.wordType(), "*.doc", "*.docx");
    }

    /**
     * 获取全部类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter allExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.allType(), "*.*");
    }

    /**
     * 获取json类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter jsonExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.jsonType(), "*.json");
    }

    /**
     * 获取png类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter pngExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.pngType(), "*.png");
    }

    /**
     * 获取jpg类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter jpgExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.jpgType(), "*.jpg");
    }

    /**
     * 获取jpeg类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter jpegExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.jpegType(), "*.jpeg");
    }

    /**
     * 获取gif类型过滤器
     *
     * @return 类型过滤器
     */
    public static FileExtensionFilter gifExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.gifType(), "*.gif");
    }

    /**
     * 创建指定类型的文件过滤器
     *
     * @param type 类型
     * @return 类型过滤器
     */
    public static FileExtensionFilter newExtensionFilter(String type) {
        return new FileExtensionFilter(type, "*." + type);
    }

    /**
     * 获取下载路径
     *
     * @return 结果
     */
    public static String getDownloadDirectory() {
        File file = new File(SystemUtil.userHome(), "Downloads");
        if (!file.exists() || !file.isDirectory()) {
            file = new File(SystemUtil.userHome(), "下载");
        }
        if (file.exists() && file.isDirectory()) {
            return file.getPath();
        }
        return HOME_DIR.getPath();
    }

    /**
     * 获取桌面路径
     *
     * @return 结果
     */
    public static String getDesktopDirectory() {
        if (!OSUtil.isWindows()) {
            String userHome = SystemUtil.userHome();
            File file = new File(userHome, "Desktop");
            if (!file.exists() || !file.isDirectory()) {
                file = new File(userHome, "桌面");
            }
            if (file.exists() && file.isDirectory()) {
                return file.getPath();
            }
        }
        return HOME_DIR.getPath();
    }

    /**
     * 获取文档路径
     *
     * @return 结果
     */
    public static String getDocumentDirectory() {
        File file = new File(SystemUtil.userHome(), "Documents");
        if (!file.exists() || !file.isDirectory()) {
            file = new File(SystemUtil.userHome(), "文档");
        }
        if (file.exists() && file.isDirectory()) {
            return file.getPath();
        }
        return HOME_DIR.getPath();
    }
}
