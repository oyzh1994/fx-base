package cn.oyzh.fx.plus.util;

import cn.hutool.core.io.FileUtil;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 文件选择器
 *
 * @author oyzh
 * @since 2020/10/21
 */
public class FileChooserUtil {

    /**
     * 桌面路径
     */
    private static final File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();

    /**
     * 扩展名过滤器
     */
    private final List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();

    /**
     * 标题
     */
    private String title;

    /**
     * 初始文件夹
     */
    private File initDir;

    /**
     * 初始文件名称
     */
    @Setter
    @Accessors(fluent = true, chain = true)
    private String initialFileName;

    /**
     * 设置标题
     *
     * @param title 标题
     * @return 文件选择器
     */
    public FileChooserUtil title(String title) {
        this.title = title;
        return this;
    }

    /**
     * 设置过滤器列表
     *
     * @param filters 过滤器列表
     * @return 文件选择器
     */
    public FileChooserUtil extensionFilters(FileChooser.ExtensionFilter... filters) {
        if (filters != null) {
            for (FileChooser.ExtensionFilter filter : filters) {
                if (filter != null) {
                    this.extensionFilters.add(filter);
                }
            }
        }
        return this;
    }

    /**
     * 设置初始化目录
     *
     * @param initDir 初始化目录
     * @return 文件选择器
     */
    public FileChooserUtil initDir(String initDir) {
        this.initDir(new File(initDir));
        return this;
    }

    /**
     * 设置初始化目录
     *
     * @param initDir 初始化目录
     * @return 文件选择器
     */
    public FileChooserUtil initDir(File initDir) {
        if (!FileUtil.exist(initDir)) {
            throw new RuntimeException("initDir路径不存在！");
        }
        if (!FileUtil.isDirectory(initDir)) {
            throw new RuntimeException("initDir不是目录！");
        }
        this.initDir = initDir;
        return this;
    }

    /**
     * 构建文件选择器
     *
     * @return 文件选择器
     */
    public FileChooser build() {
        FileChooser fileChooser = new FileChooser();
        if (this.initialFileName != null) {
            fileChooser.setInitialFileName(this.initialFileName);
        }
        if (this.title != null) {
            fileChooser.setTitle(this.title);
        }
        if (this.initDir != null) {
            fileChooser.setInitialDirectory(this.initDir);
        } else {
            fileChooser.setInitialDirectory(desktopDir);
        }
        fileChooser.getExtensionFilters().addAll(this.extensionFilters);
        return fileChooser;
    }

    /**
     * 创建文件选择构建器
     *
     * @return 文件选择构建器
     */
    public static FileChooserUtil builder() {
        return new FileChooserUtil();
    }

    /**
     * 保存文件
     *
     * @param title           标题
     * @param initFileName    初始文件名称
     * @param extensionFilter 过滤器
     */
    public static File save(String title, String initFileName, FileChooser.ExtensionFilter[] extensionFilter) {
        AtomicReference<File> file = new AtomicReference<>();
        FXUtil.runWait(() -> {
            FileChooserUtil builder = FileChooserUtil.builder();
            builder.title(title).extensionFilters(extensionFilter).initialFileName(initFileName);
            FileChooser fileChooser = builder.build();
            file.set(fileChooser.showSaveDialog(new Stage()));
        });
        return file.get();
    }

    /**
     * 选择文件
     *
     * @param title           标题
     * @param extensionFilter 过滤器
     * @return 文件
     */
    public static File choose(String title, FileChooser.ExtensionFilter[] extensionFilter) {
        AtomicReference<File> file = new AtomicReference<>();
        FXUtil.runWait(() -> {
            FileChooserUtil builder = FileChooserUtil.builder();
            builder.title(title).extensionFilters(extensionFilter);
            FileChooser fileChooser = builder.build();
            file.set(fileChooser.showOpenDialog(new Stage()));
        });
        return file.get();
    }
}
