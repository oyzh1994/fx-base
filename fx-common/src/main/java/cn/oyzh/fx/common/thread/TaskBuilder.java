package cn.oyzh.fx.common.thread;

import java.util.function.Consumer;

/**
 * 任务构建器
 *
 * @author oyzh
 * @since 2023/9/27
 */
public class TaskBuilder {

    /**
     * 开始业务
     */
    private IRunnable start;

    /**
     * 结束业务
     */
    private IRunnable finish;

    /**
     * 成功业务
     */
    private IRunnable success;

    /**
     * 异常处理
     */
    private Consumer<Exception> error;

    /**
     * 从task创建
     *
     * @param task 任务
     * @return TaskBuilder
     */
    public TaskBuilder from(Task task) {
        if (task != null) {
            this.start = task.getStart();
            this.error = task.getError();
            this.finish = task.getFinish();
            this.success = task.getSuccess();
        }
        return this;
    }

    /**
     * 设置start业务
     *
     * @param start start业务
     * @return TaskBuilder
     */
    public TaskBuilder onStart(IRunnable start) {
        this.start = start;
        return this;
    }

    // /**
    //  * 设置start业务
    //  *
    //  * @param start start业务
    //  * @return TaskBuilder
    //  */
    // public TaskBuilder onStart(Runnable start) {
    //     this.start = start::run;
    //     return this;
    // }

    /**
     * 设置success业务
     *
     * @param success finish业务
     * @return TaskBuilder
     */
    public TaskBuilder onSuccess(IRunnable success) {
        this.success = success;
        return this;
    }

    // /**
    //  * 设置success业务
    //  *
    //  * @param success finish业务
    //  * @return TaskBuilder
    //  */
    // public TaskBuilder onSuccess(Runnable success) {
    //     this.success = success::run;
    //     return this;
    // }

    /**
     * 设置finish业务
     *
     * @param finish finish业务
     * @return TaskBuilder
     */
    public TaskBuilder onFinish(IRunnable finish) {
        this.finish = finish;
        return this;
    }

    // /**
    //  * 设置finish业务
    //  *
    //  * @param finish finish业务
    //  * @return TaskBuilder
    //  */
    // public TaskBuilder onFinish(Runnable finish) {
    //     this.finish = finish::run;
    //     return this;
    // }

    /**
     * 设置error业务
     *
     * @param error error业务
     * @return TaskBuilder
     */
    public TaskBuilder onError(Consumer<Exception> error) {
        this.error = error;
        return this;
    }

    /**
     * 构建task对象
     *
     * @return task对象
     */
    public Task build() {
        Task task = new Task();
        task.setError(this.error);
        task.setStart(this.start);
        task.setFinish(this.finish);
        task.setSuccess(this.success);
        return task;
    }

    /**
     * 创建一个新的task构建器
     *
     * @return task构建器
     */
    public static TaskBuilder newBuilder() {
        return new TaskBuilder();
    }
}
