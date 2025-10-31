package cn.oyzh.fx.plus.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.fx.plus.FXConst;
import com.sun.javafx.util.Logging;
import javafx.animation.AnimationTimer;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * fx工具类
 *
 * @author oyzh
 * @since 2021/8/19
 */
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
     * 偏好设置
     */
    private static Platform.Preferences preferences;

    /**
     * 获取偏好设置
     *
     * @return 偏好设置
     */
    public static Platform.Preferences getPreferences() {
        if (preferences == null) {
            AtomicReference<Platform.Preferences> preferencesRef = new AtomicReference<>();
            synchronized (FXUtil.class) {
                FXUtil.runWait(() -> preferencesRef.set(Platform.getPreferences()));
            }
            preferences = preferencesRef.get();
        }
        return preferences;
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
    public static double getAbsoluteX(EventTarget target) {
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
    public static double getAbsoluteY(EventTarget target) {
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
    public static void runWait(Runnable task) {
        // runWaitByTimeout(task, -1);
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            DownLatch latch = DownLatch.of();
            Platform.runLater(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
            latch.await();
        }
    }

    /**
     * 同步运行
     *
     * @param task  任务
     * @param delay 延迟时间
     */
    @Deprecated
    public static void runWait(Runnable task, int delay) {
        TaskManager.startDelay(() -> Platform.runLater(task), delay);
    }

    /**
     * 同步运行
     *
     * @param task    任务
     * @param timeout 超时时间
     */
    @Deprecated
    public static void runWaitByTimeout(Runnable task, int timeout) {
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
    public static void runLater(Runnable task) {
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
    public static void runLater(Runnable task, int delay) {
        TaskManager.startDelay(() -> Platform.runLater(task), delay);
    }

    /**
     * 异步执行
     *
     * @param task 任务
     */
    public static void runAsync(Runnable task) {
        runLater(task, 1);
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
    public static void runPulse(Runnable task) {
        runPulse(task, 10);
    }

    /**
     * 在脉冲周期后执行
     *
     * @param task 任务
     * @param sign 停止信号
     */
    public static void runPulse(Runnable task, int sign) {
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
    public static List<Image> getImages(String[] imgUrls) {
        return getImages(Arrays.asList(imgUrls));
    }

    /**
     * 获取图片
     *
     * @param imgUrls 图片列表地址
     * @return 图片列表
     */
    public static List<Image> getImages(List<String> imgUrls) {
        List<Image> icons = new ArrayList<>(imgUrls.size());
        for (String url : imgUrls) {
            if (JulLog.isInfoEnabled()) {
                JulLog.info("load imgUrl:{}", url);
            }
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
    public static Image getImage(String imgUrl) {
        if (JulLog.isInfoEnabled()) {
            JulLog.info("load imgUrl:{}", imgUrl);
        }
        InputStream stream = ResourceUtil.getResourceAsStream(imgUrl);
        if (stream != null) {
            return new Image(stream);
        }
        return new Image(ResourceUtil.getLocalFileUrl(imgUrl));
    }

    /**
     * 获取媒体
     *
     * @param mediaUrl 媒体地址
     * @return 媒体
     */
    public static Media getMedia(String mediaUrl) {
        if (JulLog.isInfoEnabled()) {
            JulLog.info("load mediaUrl:{}", mediaUrl);
        }
        return new Media(ResourceUtil.getLocalFileUrl(mediaUrl));
    }

    /**
     * 转换为fx的图片
     *
     * @param bufferedImage awt图片
     * @return fx图片
     */
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
            Platform.runLater(() -> {
            });
            // PlatformImpl.getPlatformPreferences();
        } catch (IllegalStateException ignored) {
            return false;
        }
        return true;
    }

    /**
     * 显示文档
     *
     * @param url 地址
     */
    public static void showDocument(String url) {
        if (url == null) {
            return;
        }
        HostServices services = FXConst.getHostServices();
        if (services != null) {
            services.showDocument(url);
        }
    }

    /**
     * 获取屏幕刷新率
     *
     * @return 屏幕刷新率
     */
    public static int screenRefreshRate() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        DisplayMode displayMode = device.getDisplayMode();
        // 获取屏幕刷新率
        int refreshRate = displayMode.getRefreshRate();
        if (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
            return 60;
        }
        return refreshRate;
    }

    /**
     * 获取屏幕缩放
     *
     * @return 屏幕缩放
     */
    public static double screenScale() {
        Screen primaryScreen = Screen.getPrimary();
        return primaryScreen.getOutputScaleX();
    }

    // /**
    //  * 将JavaFX的WritableImage转换为AWT的BufferedImage
    //  * @param fxImage JavaFX图像对象
    //  * @return AWT图像对象，转换失败时返回null
    //  */
    // public static BufferedImage toAwtImage(WritableImage fxImage) {
    //    return SwingFXUtils.fromFXImage(fxImage, null);
    // }

    /**
     * 获取滚动条
     *
     * @param node 节点
     * @return 滚动条
     */
    public static ScrollBar getScrollBar(Node node) {
        return (ScrollBar) node.lookup("ScrollBar");
    }

    /**
     * 获取垂直滚动条
     *
     * @param parent 节点
     * @return 水平滚动条
     */
    public static ScrollBar getVScrollBar(Parent parent) {
        return getScrollBars(parent).stream()
                .filter(sb -> sb.getOrientation().equals(javafx.geometry.Orientation.VERTICAL))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取水平滚动条
     *
     * @param parent 节点
     * @return 水平滚动条
     */
    public static ScrollBar getHScrollBar(Parent parent) {
        return getScrollBars(parent).stream()
                .filter(sb -> sb.getOrientation().equals(javafx.geometry.Orientation.HORIZONTAL))
                .findFirst()
                .orElse(null);
    }

    /**
     * 递归查找所有ScrollBar
     *
     * @param parent 节点
     * @return 滚动条列表
     */
    public static List<ScrollBar> getScrollBars(Parent parent) {
        return parent.getChildrenUnmodifiable().stream()
                .flatMap(node -> {
                    if (node instanceof ScrollBar) {
                        return java.util.stream.Stream.of((ScrollBar) node);
                    } else if (node instanceof Parent) {
                        return getScrollBars((Parent) node).stream();
                    } else {
                        return java.util.stream.Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断鼠标事件是否在节点内
     *
     * @param event 事件
     * @param node  节点
     * @return 结果
     */
    public static boolean isPointInNode(MouseEvent event, Node node) {
        if (event == null || node == null) {
            return false;
        }
        // 将鼠标事件的屏幕坐标转换为目标组件的本地坐标
        double localX = node.screenToLocal(event.getScreenX(), event.getScreenY()).getX();
        double localY = node.screenToLocal(event.getScreenX(), event.getScreenY()).getY();

        // 判断本地坐标是否在组件范围内
        return node.contains(localX, localY);
    }

}
