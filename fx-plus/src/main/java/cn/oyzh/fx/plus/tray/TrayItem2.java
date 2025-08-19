package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.util.FXUtil;
import dorkbox.systemTray.Entry;
import dorkbox.systemTray.MenuItem;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;

import javax.imageio.stream.ImageInputStream;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author oyzh
 * @since 2025-08-19
 */
public class TrayItem2 extends MenuItem {

    public TrayItem2(String label, Runnable action) {
        super(label, e -> action.run());
    }

    public TrayItem2(String label, Node icon, Runnable action) {
        super(label, FXUtil.toAwtImage(icon.snapshot(null, null)), e -> action.run());
    }
}
