package cn.oyzh.fx.plus.util;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.util.Duration;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * 动画工具类
 *
 * @author oyzh
 * @since 2023/3/13
 */
@UtilityClass
public class AnimationUtil {

    /**
     * 创建旋转过渡动画
     *
     * @param node 待旋转节点
     * @return 旋转过渡动画
     */
    public static RotateTransition rotate(@NonNull Node node) {
        RotateTransition transition = new RotateTransition(Duration.seconds(3), node);
        transition.setByAngle(360);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.setInterpolator(Interpolator.LINEAR);
        return transition;
    }
}
