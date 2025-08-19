package cn.oyzh.fx.plus.util;

import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.FXHeaderBar;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;

import java.awt.Image;

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
     * @return 图标组件
     */
    public static ImageView getIcon() {
        ImageView imageView = new ImageView(IconUtil.getIcon(FXConst.appIcon()));
        imageView.setFitHeight(24);
        imageView.setPreserveRatio(true);
        return imageView;
    }

}
