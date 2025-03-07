package cn.oyzh.fx.plus.file;

import javafx.stage.Window;
import lombok.experimental.UtilityClass;

import java.io.File;

/**
 * 文件选择器
 *
 * @author oyzh
 * @since 2020/10/21
 */
@UtilityClass
public class DirChooserHelper {

    /**
     * 选择文件夹
     *
     * @param title 标题
     * @return 文件夹
     */
    public static File choose(String title) {
        return choose(title, null);
    }

    /**
     * 选择文件夹
     *
     * @param title 标题
     * @param owner 父窗口
     * @return 文件夹
     */
    public static File choose(String title, Window owner) {
        FXDirChooser chooser = new FXDirChooser();
        chooser.title(title);
        return chooser.showDialog(owner);
    }
}
