package cn.oyzh.fx.plus.util;

import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * 动画工具类
 *
 * @author oyzh
 * @since 2023/3/13
 */
public class AnimationUtil {

    /**
     * 创建旋转过渡动画
     *
     * @param node 待旋转节点
     * @return 旋转过渡动画
     */
    public static RotateTransition rotate(Node node) {
        RotateTransition transition = new RotateTransition(Duration.seconds(3), node);
        transition.setByAngle(360);
        transition.setCycleCount(Animation.INDEFINITE);
        transition.setInterpolator(Interpolator.LINEAR);
        return transition;
    }

//    public static void move(Node node, Node target) {
//        double[] mouse = MouseUtil.getMousePosition();
//        Point2D point2 = target.localToScene(0, 0);
//        move(StageManager.getFrontWindow(), node, mouse[0], mouse[1], point2.getX(), point2.getY());
//    }

    /**
     * 移动动画
     *
     * @param node   要移动的节点
     * @param source 参考的节点
     * @param target 目标节点
     */
    public static void move(Node node, Node source, Node target) {
        move(StageManager.getFrontWindow(), node, source, target);
    }

    /**
     * 移动动画
     *
     * @param window 窗口
     * @param node   要移动的节点
     * @param source 参考的节点
     * @param target 目标节点
     */
    public static void move(Window window, Node node, Node source, Node target) {
        Point2D point1 = source.localToScene(0, 0);
        Point2D point2 = target.localToScene(0, 0);
        double h = NodeUtil.getHeight(target);
        move(window, node, point1.getX(), point1.getY(), point2.getX(), point2.getY() - h);
    }

    /**
     * 移动动画
     *
     * @param window 窗口
     * @param node   要移动的节点
     * @param startX 起始位置x
     * @param startY 起始位置y
     * @param endX   结束位置x
     * @param endY   结束位置y
     */
    public static void move(Window window, Node node, double startX, double startY, double endX, double endY) {
        if (window == null) {
            return;
        }
        Scene scene = window.getScene();
        if (scene == null) {
            return;
        }
        Pane pane;
        if (scene.getRoot() instanceof Pane root) {
            pane = root;
        } else {
            return;
        }
        endX = Math.abs(endX);
        endY = Math.abs(endY);
        // 添加的目标阶段
        pane.getChildren().add(node);
        node.setLayoutX(startX);
        node.setLayoutY(startY);
        // 创建一个时间轴动画
        Timeline timeline = new Timeline();

        // 创建移动和缩小的关键帧
        KeyFrame startFrame = new KeyFrame(Duration.ZERO,
                new KeyValue(node.layoutXProperty(), node.getLayoutX()),
                new KeyValue(node.layoutYProperty(), node.getLayoutY()),
                new KeyValue(node.scaleXProperty(), 1),
                new KeyValue(node.scaleYProperty(), 1)
        );

        KeyFrame endFrame = new KeyFrame(Duration.seconds(1.5),
                new KeyValue(node.layoutXProperty(), endX),
                new KeyValue(node.layoutYProperty(), endY),
                new KeyValue(node.scaleXProperty(), 0),
                new KeyValue(node.scaleYProperty(), 0)
        );

        // 将关键帧添加到时间轴
        timeline.getKeyFrames().addAll(startFrame, endFrame);
        // 动画结束后移除矩形
        timeline.setOnFinished(e -> pane.getChildren().remove(node));
        // 播放动画
        timeline.play();
    }
}
