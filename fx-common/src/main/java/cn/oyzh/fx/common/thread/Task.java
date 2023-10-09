package cn.oyzh.fx.common.thread;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2023/9/14
 */
public class Task implements Runnable {

    @Setter
    @Getter
    private Runnable start;

    @Setter
    @Getter
    private Runnable finish;

    @Setter
    @Getter
    private Runnable success;

    @Setter
    @Getter
    private Consumer<Exception> error;

    private Exception exception;

    public void onStart() throws Exception {
        if (this.start != null) {
            this.start.run();
        }
    }

    public void onSuccess() {
        if (this.success != null) {
            this.success.run();
        }
    }

    public void onFinish() {
        if (this.finish != null) {
            this.finish.run();
        }
    }

    public void onError(Exception ex) {
        if (this.error != null) {
            this.error.accept(ex);
        }
    }

    @Override
    public final void run() {
        try {
            this.onStart();
            this.onSuccess();
        } catch (Exception ex) {
            this.exception = ex;
            this.onError(ex);
        } finally {
            this.onFinish();
        }
    }

    public void throwRuntimeException() {
        if (this.exception != null) {
            throw new RuntimeException(this.exception);
        }
    }
}
