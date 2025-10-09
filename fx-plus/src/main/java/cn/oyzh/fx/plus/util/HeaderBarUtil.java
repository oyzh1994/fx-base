package cn.oyzh.fx.plus.util;

import cn.oyzh.fx.plus.controls.FXHeaderBar;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;

/**
 * @author oyzh
 * @since 2025-08-19
 */
public class HeaderBarUtil {

    /**
     * 获取扩展标题栏
     *
     * @param parent 根节点
     * @return 标题栏
     */
    public static FXHeaderBar getHeaderBar(Parent parent) {
        if (parent != null) {
            FXHeaderBar bar = (FXHeaderBar) parent.lookup("#headerBar");
            if (bar == null) {
                bar = (FXHeaderBar) parent.lookup("FXHeaderBar");
            }
            return bar;
        }
        return null;
    }

    /**
     * 获取图标
     *
     * @param iconUrl 图标地址
     * @return 图标组件
     */
    public static ImageView getIcon(String iconUrl) {
        ImageView imageView = new ImageView(IconUtil.getIcon(iconUrl));
        imageView.setFitHeight(16);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    // /**
    //  * 安装事件
    //  *
    //  * @param stage     组件
    //  * @param headerBar 标题
    //  */
    // public static void installEvent(Stage stage, FXHeaderBar headerBar) {
    //     Parent root = stage.getScene().getRoot();
    //
    //     // 鼠标按下事件
    //     stage.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
    //         if (MouseUtil.isPrimaryButton(event) && headerBar.checkBounds(event) && event.getTarget() == root) {
    //             // 全屏则忽略
    //             if (stage.isFullScreen()) {
    //                 return;
    //             }
    //             // 记录位置
    //             if (event.getClickCount() == 1) {
    //                 headerBar.doRecordLocation();
    //             } else if (event.getClickCount() == 2) {  // 最大化
    //
    //                 stage.setMaximized(!stage.isMaximized());
    //             }
    //         }
    //     });
    //     // 鼠标拖动事件
    //     stage.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
    //         if (MouseUtil.isPrimaryButton(event) && headerBar.checkBounds(event) && event.getTarget() == root) {
    //             // 更新位置
    //             headerBar.doUpdateLocation();
    //         }
    //     });
    //     // 鼠标释放事件
    //     stage.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
    //         if (MouseUtil.isPrimaryButton(event) && headerBar.checkBounds(event) && event.getTarget() == root) {
    //             // 清除位置
    //             headerBar.doClearLocation();
    //         }
    //     });
    // }

}
