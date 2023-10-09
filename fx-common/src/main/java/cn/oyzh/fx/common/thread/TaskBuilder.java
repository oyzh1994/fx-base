package cn.oyzh.fx.common.thread;

import java.util.function.Consumer;

public class TaskBuilder {

    private Runnable start;

    private Runnable finish;

    private Runnable success;

    private Consumer<Exception> error;

    public TaskBuilder onStart(Runnable start) {
        this.start = start;
        return this;

    }

    public TaskBuilder onSuccess(Runnable success) {
        this.success = success;
        return this;
    }

    public TaskBuilder onFinish(Runnable finish) {
        this.finish = finish;
        return this;
    }

    public TaskBuilder onError(Consumer<Exception> error) {
        this.error = error;
        return this;
    }

    public Task build() {
        Task task = new Task();
        task.setError(this.error);
        task.setStart(this.start);
        task.setFinish(this.finish);
        task.setSuccess(this.success);
        return task;
    }

    public static TaskBuilder newBuilder() {
        return new TaskBuilder();
    }
}
