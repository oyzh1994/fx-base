package cn.oyzh.fx.plus.chooser;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.i18n.I18nHelper;
import lombok.experimental.UtilityClass;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * @author oyzh
 * @since 2025-03-07
 */
@UtilityClass
public class FXChooser {

    /**
     * 桌面路径
     */
    public static final File DESKTOP_DIR = FileSystemView.getFileSystemView().getHomeDirectory();

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

    public static FileExtensionFilter csvExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.csvType(), "*.csv");
    }

    public static FileExtensionFilter htmlExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.htmlType(), "*.html");
    }

    public static FileExtensionFilter xlsExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.xlsType(), "*.xls");
    }

    public static FileExtensionFilter xlsxExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.xlsxType(), "*.xlsx");
    }

    public static FileExtensionFilter excelExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.excelType(), "*.xls", "*.xlsx");
    }

    public static FileExtensionFilter allExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.allType(), "*.*");
    }

    public static FileExtensionFilter jsonExtensionFilter() {
        return new FileExtensionFilter(I18nHelper.jsonType(), "*.json");
    }

    public static String getDownloadDirectory() {
        File file = new File(System.getProperty("user.home"), "Downloads");
        if (file.exists() && file.isDirectory()) {
            return file.getPath();
        }
        return DESKTOP_DIR.getPath();
    }
}
