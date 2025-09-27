package cn.oyzh.fx.plus.chooser;

import cn.oyzh.fx.plus.window.StageManager;
import javafx.stage.Window;

import java.io.File;

/**
 * 文件夹选择器
 *
 * @author oyzh
 * @since 2020/10/21
 */
public class DirChooserHelper {

    /**
     * 选择文件夹
     *
     * @param title 标题
     * @return 文件夹
     */
    public static File choose(String title) {
        return choose(title, null, StageManager.getFrontWindow());
    }

    /**
     * 选择下载文件夹
     *
     * @param title 标题
     * @return 文件夹
     */
    public static File chooseDownload(String title) {
        return choose(title, FXChooser.getDownloadDirectory(), StageManager.getFrontWindow());
    }

    /**
     * 选择桌面文件夹
     *
     * @param title 标题
     * @return 文件夹
     */
    public static File chooseDesktop(String title) {
        return choose(title, FXChooser.getDesktopDirectory(), StageManager.getFrontWindow());
    }

    /**
     * 选择文件夹
     *
     * @param title   标题
     * @param initDir 初始目录
     * @param owner   父窗口
     * @return 文件夹
     */
    public static File choose(String title, String initDir, Window owner) {
        FXDirChooser chooser = new FXDirChooser();
        chooser.setTitle(title);
        if (initDir != null) {
            chooser.initDir(initDir);
        }
        return chooser.showDialog(owner);
    }
}
