package cn.oyzh.fx.plus.file;

import cn.oyzh.fx.common.util.CollectionUtil;
import cn.oyzh.fx.common.util.FileUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.WindowManager;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author oyzh
 * @since 2024/8/28
 */
public class FXFileChooser {

    /**
     * 标题
     */
    @Getter
    @Setter
    @Accessors(fluent = true, chain = true)
    private String title;

    /**
     * 初始文件夹
     */
    @Getter
    @Accessors(fluent = true, chain = true)
    private File initDir = FileChooserHelper.DESKTOP_DIR;

    /**
     * 初始文件名称
     */
    @Setter
    @Accessors(fluent = true, chain = true)
    private String initialFileName;

    /**
     * 过滤器
     */
    @Getter
    @Accessors(fluent = true, chain = true)
    private List<FileExtensionFilter> filters;

    public FXFileChooser addFilter(@NonNull FileExtensionFilter filter) {
        if (this.filters == null) {
            this.filters = new ArrayList<>();
        }
        this.filters.add(filter);
        return this;
    }

    public FXFileChooser addFilter(String desc, String extension) {
        FileExtensionFilter filter = new FileExtensionFilter();
        filter.setDesc(desc);
        filter.addExtension(extension);
        this.addFilter(filter);
        return this;
    }

    public FXFileChooser addFilters(List<FileExtensionFilter> filters) {
        if (CollectionUtil.isNotEmpty(filters)) {
            for (FileExtensionFilter filter : filters) {
                this.addFilter(filter);
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
    public FXFileChooser initDir(String initDir) {
        this.initDir(new File(initDir));
        return this;
    }

    /**
     * 设置初始化目录
     *
     * @param initDir 初始化目录
     * @return 文件选择器
     */
    public FXFileChooser initDir(File initDir) {
        if (!FileUtil.exist(initDir)) {
            throw new RuntimeException("initDir路径不存在！");
        }
        if (!FileUtil.isDirectory(initDir)) {
            throw new RuntimeException("initDir不是目录！");
        }
        this.initDir = initDir;
        return this;
    }

    public FileChooser chooser() {
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
            fileChooser.setInitialDirectory(FileChooserHelper.DESKTOP_DIR);
        }
        for (FileExtensionFilter filter : filters) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filter.getDesc(), filter.getExtensions()));
        }
        return fileChooser;
    }

    public File showSaveDialog(Window owner) {
        if (owner == null) {
            owner = WindowManager.getActiveWindow();
        }
        AtomicReference<File> file = new AtomicReference<>();
        Window finalOwner = owner;
        FXUtil.runWait(() -> file.set(this.chooser().showSaveDialog(finalOwner)));
        return file.get();
    }

    public File showOpenDialog(Window owner) {
        if (owner == null) {
            owner = WindowManager.getActiveWindow();
        }
        AtomicReference<File> file = new AtomicReference<>();
        Window finalOwner = owner;
        FXUtil.runWait(() -> file.set(this.chooser().showOpenDialog(finalOwner)));
        return file.get();
    }

}
