package cn.oyzh.fx.plus.util;

import cn.hutool.log.StaticLog;
import cn.oyzh.fx.common.thread.Task;
import cn.oyzh.fx.common.thread.TaskBuilder;
import cn.oyzh.fx.common.thread.TaskManager;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * fx工具类
 *
 * @author oyzh
 * @since 2021/8/19
 */
@UtilityClass
public class FXUtil {

    /**
     * app存储路径
     */
    @Getter
    @Setter
    private static String appStorePath;

    /**
     * 当前机器对象
     */
    private static Robot robot;

    /**
     * 获取机器对象
     *
     * @return 结果
     */
    public static Robot getRobot() {
        if (robot == null) {
            synchronized (FXUtil.class) {
                robot = new Robot();
            }
        }
        return robot;
    }

    /**
     * 计算窗口坐标
     *
     * @param owner  父窗口
     * @param target 目标窗口
     */
    public static void computePos(Window owner, Window target) {
        if (owner == null || target == null) {
            return;
        }
        // 计算x，y的位置
        double absX = FXUtil.getAbsoluteX(owner);
        double abxY = FXUtil.getAbsoluteY(owner);
        absX = absX + (owner.getWidth() - target.getWidth()) / 2.0;
        abxY = abxY + (owner.getHeight() - target.getHeight()) / 2.0;
        target.setX(absX);
        target.setY(abxY);
    }

    /**
     * 获取绝对坐标x
     *
     * @param target 对象
     * @return x坐标
     */
    public static double getAbsoluteX(@NonNull EventTarget target) {
        if (target instanceof Stage) {
            return ((Stage) target).getX();
        } else if (target instanceof Scene scene) {
            return scene.getX() + getAbsoluteX(scene.getWindow());
        } else {
            Node node = (Node) target;
            if (node.getParent() == null) {
                return node.getLayoutX() + getAbsoluteX(node.getScene());
            }
            return node.getLayoutX() + getAbsoluteX(node.getParent());
        }
    }

    /**
     * 获取绝对坐标y
     *
     * @param target 对象
     * @return y坐标
     */
    public static double getAbsoluteY(@NonNull EventTarget target) {
        if (target instanceof Stage stage) {
            return stage.getY();
        } else if (target instanceof Scene scene) {
            return scene.getY() + getAbsoluteY(scene.getWindow());
        } else {
            Node node = (Node) target;
            if (node.getParent() == null) {
                return node.getLayoutY() + getAbsoluteY(node.getScene());
            }
            return node.getLayoutY() + getAbsoluteY(node.getParent());
        }
    }

    /**
     * 同步运行
     *
     * @param task 任务
     */
    public static void runWait(@NonNull Runnable task) {
        runWaitByTimeout(task, -1);
    }

    /**
     * 同步运行
     *
     * @param task  任务
     * @param delay 延迟时间
     */
    public static void runWait(@NonNull Runnable task, int delay) {
        TaskManager.startDelay(() -> runWait(task), delay);
    }

    /**
     * 同步运行
     *
     * @param task    任务
     * @param timeout 超时时间
     */
    public static void runWaitByTimeout(@NonNull Runnable task, int timeout) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            // 等待执行完成
            CountDownLatch latch = new CountDownLatch(1);
            try {
                // 包装线程
                Task task1 = TaskBuilder.newBuilder().onStart(task).onFinish(latch::countDown).build();
                Platform.runLater(task1);
                if (timeout <= 0) {
                    latch.await();
                } else if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                    StaticLog.warn("latch.await fail!");
                }
                // 抛出异常(如果有)
                task1.throwRuntimeException();
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * 稍后执行
     *
     * @param task 任务
     */
    public static void runLater(@NonNull Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }

    /**
     * 稍后执行
     *
     * @param task  任务
     * @param delay 延迟时间
     */
    public static void runLater(@NonNull Runnable task, int delay) {
        TaskManager.startDelay(() -> runLater(task), delay);
    }
}
