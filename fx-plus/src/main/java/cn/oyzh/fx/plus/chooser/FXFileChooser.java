package cn.oyzh.fx.plus.chooser;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.stage.FileChooser;
import javafx.stage.Window;

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
    private String title;

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 初始文件夹
     */
    private File initDir = FXChooser.HOME_DIR;

    /**
     * 初始文件名称
     */
    private String initialFileName;

    public void setInitialFileName(String initialFileName) {
        this.initialFileName = initialFileName;
    }

    /**
     * 过滤器
     */
    private List<FileExtensionFilter> filters;

    public void filters(List<FileExtensionFilter> filters) {
        this.filters = filters;
    }

    public FXFileChooser addFilter( FileExtensionFilter filter) {
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
            fileChooser.setInitialDirectory(FXChooser.HOME_DIR);
        }
        for (FileExtensionFilter filter : filters) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filter.getDesc(), filter.getExtensions()));
        }
        return fileChooser;
    }

    public File showSaveDialog(Window owner) {
//        if (owner == null) {
//            owner = WindowManager.getActiveWindow();
//        }
        AtomicReference<File> file = new AtomicReference<>();
        FXUtil.runWait(() -> file.set(this.chooser().showSaveDialog(owner)));
//        Window finalOwner = owner;
//        FXUtil.runWait(() -> file.set(this.chooser().showSaveDialog(finalOwner)));
        return file.get();
    }

    public File showOpenDialog(Window owner) {
//        if (owner == null) {
//            owner = WindowManager.getActiveWindow();
//        }
        AtomicReference<File> file = new AtomicReference<>();
        FXUtil.runWait(() -> file.set(this.chooser().showOpenDialog(owner)));
//        Window finalOwner = owner;
//        FXUtil.runWait(() -> file.set(this.chooser().showOpenDialog(finalOwner)));
        return file.get();
    }

    public List<File> showOpenMultipleDialog(Window owner) {
//        if (owner == null) {
//            owner = WindowManager.getActiveWindow();
//        }
        List<File> files = new ArrayList<>();
//        Window finalOwner = owner;
        FXUtil.runWait(() -> {
            List<File> files1 = this.chooser().showOpenMultipleDialog(owner);
//            List<File> files1 = this.chooser().showOpenMultipleDialog(finalOwner);
            if (files1 != null) {
                files.addAll(files1);
            }
        });
        return files;
    }

}
