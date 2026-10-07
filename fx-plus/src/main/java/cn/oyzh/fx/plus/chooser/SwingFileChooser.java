package cn.oyzh.fx.plus.chooser;

import javax.swing.*;
import java.io.File;
import java.util.function.Consumer;

/**
 * 基于Swing的文件选择器
 *
 * @author oyzh
 * @since 2025-03-28
 */
public class SwingFileChooser extends JFileChooser {

    /**
     * 显示文件选择器
     *
     * @param callback 选择结果回调，取消选择时回调参数为null
     */
    public void showFileChooser(Consumer<File[]> callback) {
        // 在事件分发线程中运行 GUI 代码
        SwingUtilities.invokeLater(() -> {
            if (this.getCurrentDirectory() == null) {
                this.setCurrentDirectory(FXChooser.HOME_DIR);
            }
            this.setMultiSelectionEnabled(true);
            int result = this.showOpenDialog(null);
            File[] files = this.getSelectedFiles();
            if (result == JFileChooser.APPROVE_OPTION) {
                callback.accept(files);
            } else {
                callback.accept(null);
            }
        });
    }
}
