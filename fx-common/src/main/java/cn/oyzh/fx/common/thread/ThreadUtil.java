package cn.oyzh.fx.common.thread;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.oyzh.fx.common.util.CollectionUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 线程工具类
 *
 * @author oyzh
 * @since 2023/1/3
 */
@UtilityClass
public class ThreadUtil {

    /**
     * 开始运行线程
     * TODO: 虚拟线程，轻量级别，适合io密集型任务
     *
     * @param task 任务
     * @return 线程
     */
    public static Thread startVirtual(@NonNull Runnable task) {
        return Thread.ofVirtual().start(task);
    }

    /**
     * 执行任务列表
     * TODO: 虚拟线程，轻量级别，适合io密集型任务
     *
     * @param tasks 任务列表
     */
    public static void submitVirtual(List<Runnable> tasks) {
        if (CollectionUtil.isNotEmpty(tasks)) {
            try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> futures = new ArrayList<>(tasks.size());
                for (Runnable task : tasks) {
                    futures.add(service.submit(task));
                }
                for (Future<?> future : futures) {
                    future.get();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * 执行任务列表，并活得结果
     * TODO: 虚拟线程，轻量级别，适合io密集型任务
     *
     * @param tasks 任务列表
     */
    public static <V> List<V> invokeVirtual(List<Callable<V>> tasks) {
        List<V> results = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(tasks)) {
            try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<V>> futures = service.invokeAll(tasks);
                for (Future<V> future : futures) {
                    results.add(future.get());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return results;
    }

    /**
     * 开始运行线程
     *
     * @param task 任务
     * @return 线程
     */
    public static Thread start(@NonNull Runnable task) {
        ThreadExt thread = new ThreadExt(task);
        thread.start();
        return thread;
    }

    /**
     * 开始运行线程
     *
     * @param task  任务
     * @param delay 延迟
     * @return 线程
     */
    public static Thread start(Runnable task, long delay) {
        ThreadExt thread = new ThreadExt(task);
        ExecutorUtil.start(thread::start, delay);
        return thread;
    }

    /**
     * 执行任务列表
     * TODO: 物理线程，重量级别，适合cpu密集型任务
     *
     * @param tasks 任务列表
     */
    public static void submit(List<Runnable> tasks) {
        if (CollectionUtil.isNotEmpty(tasks)) {
            ExecutorService service = Executors.newCachedThreadPool();
            try {
                List<Future<?>> futures = new ArrayList<>(tasks.size());
                for (Runnable task : tasks) {
                    futures.add(service.submit(task));
                }
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } finally {
                service.shutdown();
            }
        }
    }

    /**
     * 执行任务列表，并拿到返回结果
     * TODO: 物理线程，重量级别，适合cpu密集型任务
     *
     * @param tasks 任务列表
     * @param <V>   结果泛型
     * @return 结果列表
     */
    public static <V> List<V> invoke(List<Callable<V>> tasks) {
        if (CollectionUtil.isNotEmpty(tasks)) {
            try (ExecutorService service = Executors.newCachedThreadPool()) {
                List<Future<V>> futures = service.invokeAll(tasks);
                List<V> results = new ArrayList<>(futures.size());
                for (Future<V> future : futures) {
                    try {
                        V result = future.get();
                        if (result != null) {
                            results.add(result);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return results;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    /**
     * 休眠
     *
     * @param millis 毫秒值
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 线程是否结束
     *
     * @return 结果
     */
    public static boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    /**
     * 线程是否结束
     *
     * @param thread 线程
     * @return 结果
     */
    public static boolean isInterrupted(Thread thread) {
        return thread == null || thread.isInterrupted();
    }

    /**
     * 结束线程
     *
     * @param thread 线程
     */
    public static void interrupt(Thread thread) {
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
    }

    /**
     * 加入线程
     *
     * @param thread 线程
     */
    public static void join(Thread thread) {
        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 创建单任务线程池
     *
     * @return 单任务线程池
     */
    public static ExecutorService newSingleExecutor() {
        return ExecutorBuilder.create().setCorePoolSize(1).setMaxPoolSize(1).setKeepAliveTime(0L).buildFinalizable();
    }
}
