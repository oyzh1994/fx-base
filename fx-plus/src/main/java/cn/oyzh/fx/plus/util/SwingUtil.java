package cn.oyzh.fx.plus.util;

import javax.swing.SwingUtilities;

/**
 * @author oyzh
 * @since 2025-08-04
 */
public class SwingUtil {

    public static void runWait(Runnable func) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                func.run();
            }else {
                SwingUtilities.invokeAndWait(func);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void runLater(Runnable func) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                func.run();
            } else {
                SwingUtilities.invokeLater(func);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
