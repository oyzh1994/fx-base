package cn.oyzh.fx.plus.file;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author oyzh
 * @since 2024/8/28
 */
public class FXDirChooser {

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
    private File initDir = FXChooser.DESKTOP_DIR;

    /**
     * 设置初始化目录
     *
     * @param initDir 初始化目录
     * @return 文件选择器
     */
    public FXDirChooser initDir(String initDir) {
        this.initDir(new File(initDir));
        return this;
    }

    /**
     * 设置初始化目录
     *
     * @param initDir 初始化目录
     * @return 文件选择器
     */
    public FXDirChooser initDir(File initDir) {
        if (!FileUtil.exist(initDir)) {
            throw new RuntimeException("initDir路径不存在！");
        }
        if (!FileUtil.isDirectory(initDir)) {
            throw new RuntimeException("initDir不是目录！");
        }
        this.initDir = initDir;
        return this;
    }

    public DirectoryChooser chooser() {
        DirectoryChooser chooser = new DirectoryChooser();
        if (this.title != null) {
            chooser.setTitle(this.title);
        }
        if (this.initDir != null) {
            chooser.setInitialDirectory(this.initDir);
        } else {
            chooser.setInitialDirectory(FXChooser.DESKTOP_DIR);
        }
        return chooser;
    }

    public File showDialog(Window owner) {
        AtomicReference<File> file = new AtomicReference<>();
        FXUtil.runWait(() -> file.set(this.chooser().showDialog(owner)));
        return file.get();
    }
}
