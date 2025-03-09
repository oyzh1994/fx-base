package cn.oyzh.fx.plus.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.fx.plus.FXConst;
import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.util.Logging;
import javafx.animation.AnimationTimer;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.robot.Robot;
import javafx.stage.Window;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * fx工具类
 *
 * @author oyzh
 * @since 2021/8/19
 */
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
        if (target instanceof Window window) {
            return window.getX();
        } else if (target instanceof Scene scene) {
            return scene.getX() + getAbsoluteX(scene.getWindow());
        } else if (target instanceof Node node) {
            if (node.getParent() == null) {
                return node.getLayoutX() + getAbsoluteX(node.getScene());
            }
            return node.getLayoutX() + getAbsoluteX(node.getParent());
        }
        return Double.NaN;
    }

    /**
     * 获取绝对坐标y
     *
     * @param target 对象
     * @return y坐标
     */
    public static double getAbsoluteY(@NonNull EventTarget target) {
        if (target instanceof Window window) {
            return window.getY();
        } else if (target instanceof Scene scene) {
            return scene.getY() + getAbsoluteY(scene.getWindow());
        } else if (target instanceof Node node) {
            if (node.getParent() == null) {
                return node.getLayoutY() + getAbsoluteY(node.getScene());
            }
            return node.getLayoutY() + getAbsoluteY(node.getParent());
        }
        return Double.NaN;
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
                Task task1 = TaskBuilder.newBuilder().onStart(task::run).onFinish(latch::countDown).build();
                Platform.runLater(task1);
                if (timeout <= 0) {
                    latch.await();
                } else if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                    JulLog.warn("latch.await fail!");
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

    /**
     * 获取fx线程
     *
     * @return fx线程
     */
    public static Thread getFXThread() {
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> entry : map.entrySet()) {
            Thread thread = entry.getKey();
            if ("JavaFX Application Thread".equalsIgnoreCase(thread.getName())) {
                return thread;
            }
        }
        return null;
    }

    /**
     * 在脉冲周期后执行
     *
     * @param task 任务
     */
    public static void runPulse(@NonNull Runnable task) {
        runPulse(task, 10);
    }

    /**
     * 在脉冲周期后执行
     *
     * @param task 任务
     * @param sign 停止信号
     */
    public static void runPulse(@NonNull Runnable task, int sign) {
        AtomicInteger tick = new AtomicInteger();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (sign <= 0 || tick.incrementAndGet() >= sign) {
                    try {
                        task.run();
                    } finally {
                        this.stop();
                    }
                }
            }
        };
        timer.start();
    }

    /**
     * 禁用css日志
     */
    public static void disableCSSLogger() {
        Logging.getCSSLogger().disableLogging();
    }

    /**
     * 获取图片
     *
     * @param imgUrls 图片列表地址
     * @return 图片列表
     */
    public static List<Image> getImages(@NonNull String[] imgUrls) {
        return getImages(Arrays.asList(imgUrls));
    }

    /**
     * 获取图片
     *
     * @param imgUrls 图片列表地址
     * @return 图片列表
     */
    public static List<Image> getImages(@NonNull List<String> imgUrls) {
        List<Image> icons = new ArrayList<>(imgUrls.size());
        for (String url : imgUrls) {
            JulLog.info("load imgUrl:{}", url);
            InputStream stream = ResourceUtil.getResourceAsStream(url);
            if (stream == null) {
                JulLog.warn("img stream is null.");
            } else {
                icons.add(new Image(stream));
            }
        }
        return icons;
    }

    /**
     * 获取图片
     *
     * @param imgUrl 图片地址
     * @return 图片
     */
    public static Image getImage(@NonNull String imgUrl) {
        JulLog.info("load imgUrl:{}", imgUrl);
        InputStream stream = ResourceUtil.getResourceAsStream(imgUrl);
        if (stream == null) {
            JulLog.warn("img stream is null.");
            return null;
        }
        return new Image(stream);
    }

    public static Image toImage(BufferedImage bufferedImage) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return new Image(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtil.close(byteArrayOutputStream);
        }
    }

    /**
     * 判断fx是否可用
     *
     * @return 结果
     */
    public static boolean isInitialized() {
        try {
            PlatformImpl.getPlatformPreferences();
        } catch (IllegalStateException ignored) {
            return false;
        }
        return true;
    }

    public static void showDocument(String url) {
        if (url == null) {
            return;
        }
        HostServices services = FXConst.getHostServices();
        if (services != null) {
            services.showDocument(url);
        }
    }
}
