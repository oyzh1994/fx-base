package cn.oyzh.fx.plus.util;

import cn.oyzh.fx.common.thread.Task;
import cn.oyzh.fx.common.thread.TaskBuilder;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * fx工具类
 *
 * @author oyzh
 * @since 2021/8/19
 */
@Slf4j
@UtilityClass
public class FXUtil {

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

//    /**
//     * 根据id寻找子节点
//     *
//     * @param children 子节点集合
//     * @param id       id
//     * @return 子节点
//     */
//    public static <T extends Node> T findChildById(@NonNull ObservableList<T> children, @NonNull String id) {
//        Node node = null;
//        for (Node next : children) {
//            if (id.equals(next.getId())) {
//                node = next;
//                break;
//            }
//        }
//        return (T) node;
//    }
//
//    /**
//     * 根据id寻找子节点
//     *
//     * @param parent 父节点
//     * @param id     id
//     * @return 子节点
//     */
//    public static <T extends Node> T findChildById(@NonNull Parent parent, @NonNull String id) {
//        Node node = findChildById(parent.getChildrenUnmodifiable(), id);
//        if (node == null) {
//            for (Node next : parent.getChildrenUnmodifiable()) {
//                if (next instanceof Parent) {
//                    node = findChildById((Parent) next, id);
//                    if (node != null) {
//                        break;
//                    }
//                }
//            }
//        }
//        return (T) node;
//    }

    /**
     * 同步运行
     *
     * @param task 任务
     */
    public static void runWait(@NonNull Runnable task) {
        runWait(task, -1);
    }

    /**
     * 同步运行
     *
     * @param task    任务
     * @param timeout 超时时间
     */
    public static void runWait(@NonNull Runnable task, int timeout) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            // 等待执行完成
            CountDownLatch latch = new CountDownLatch(1);
            try {
                // 包装线程
                Task task1 = TaskBuilder.newBuilder().onStart(task).onFinish(latch::countDown).build();
                Platform.runLater(task1);
                // Platform.runLater(new RunTask(task1));
                if (timeout > 0) {
                    if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                        log.warn("latch.await fail!");
                    }
                } else {
                    latch.await();
                }
                // 抛出异常(如果有)
                task1.throwRuntimeException();
            } catch (InterruptedException ignored) {
            }
        }
    }

    /* 延迟执行
     *
     * @param task 任务
     */
    public static void runLater(@NonNull Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
           Platform.runLater(task);
            // Platform.runLater(new RunTask(task));
        }
    }

    // /**
    //  * 复制到粘贴板
    //  *
    //  * @param content 内容
    //  * @return 结果
    //  */
    // public static boolean clipboardCopy(@NonNull String content) {
    //     try {
    //         StringSelection stringSelection = new StringSelection(content);
    //         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return false;
    //     }
    //     return true;
    // }
    //
    // /**
    //  * 获取粘贴板文本
    //  *
    //  * @return 结果
    //  */
    // public static String clipboardContent() {
    //     try {
    //         // 获取系统剪贴板
    //         Clipboard clipboard = Clipboard.getSystemClipboard();
    //         // 判断剪贴板中是否有文本内容
    //         if (clipboard.hasString()) {
    //             // 获取文本内容
    //             return clipboard.getString();
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     return null;
    // }
}
