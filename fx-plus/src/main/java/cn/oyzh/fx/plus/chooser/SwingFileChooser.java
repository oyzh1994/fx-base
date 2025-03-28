package cn.oyzh.fx.plus.chooser;

import javax.swing.*;
import java.io.File;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025/03/28
 */
public class SwingFileChooser extends JFileChooser {

    public void showFileChooser(Consumer<File[]> callback) {
        // 在事件分发线程中运行 GUI 代码
        SwingUtilities.invokeLater(() -> {
            if (this.getCurrentDirectory() == null) {
                this.setCurrentDirectory(FXChooser.DESKTOP_DIR);
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
