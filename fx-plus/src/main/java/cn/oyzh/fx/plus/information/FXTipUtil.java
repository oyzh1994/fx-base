package cn.oyzh.fx.plus.information;

import javafx.scene.Node;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * 提示窗口工具栏
 *
 * @author oyzh
 * @since 2023/01/04
 */
@UtilityClass
public class FXTipUtil {

    /**
     * 最近一次消息提示
     */
    private static FXTip last;

    /**
     * 在指定节点位置显示，并且使节点获得焦点
     *
     * @param tipMsg  提示消息
     * @param tipNode 提示节点
     */
    public static void tip(@NonNull String tipMsg, @NonNull Node tipNode) {
        if (last != null && last.isShowing()) {
            last.close();
        }
        FXTip fxTip = new FXTip(tipNode, tipMsg);
        last = fxTip;
        fxTip.setOnHidden(e -> {
            if (fxTip == last) {
                last = null;
            }
        });
        fxTip.show();
    }
}
