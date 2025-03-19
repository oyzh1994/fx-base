package cn.oyzh.fx.terminal.execute;


/**
 * 终端执行结果
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class TerminalExecuteResult {

    /**
     * 结果
     */
    private Object result;

    /**
     * 错误信息
     */
    private String errMsg;

    /**
     * 异常
     */
    private Exception exception;

    public boolean isIgnoreOutput() {
        return ignoreOutput;
    }

    public void setIgnoreOutput(boolean ignoreOutput) {
        this.ignoreOutput = ignoreOutput;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * 忽略输出
     */
    private boolean ignoreOutput;

    /**
     * 是否成功
     *
     * @return 结果
     */
    public boolean isSuccess() {
        return this.errMsg == null && this.exception == null;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息
     */
    public String getErrMsg() {
        if (this.errMsg != null) {
            return this.errMsg;
        }
        return this.exception == null ? null : this.exception.getMessage();
    }

    /**
     * 追加结果
     *
     * @param result 结果
     */
    public void appendResult(String result) {
        if (result == null) {
            return;
        }
        if (this.result == null) {
            this.result = "";
        }
        this.result += result;
    }

    /**
     * 追加结果，附带换行
     *
     * @param result 结果
     */
    public void appendResultLine(String result) {
        if (result == null) {
            return;
        }
        if (this.result == null) {
            this.result = "";
        } else if (!this.result.toString().endsWith("\n")) {
            this.result += "\n";
        }
        this.result += result;
    }

    /**
     * 执行ok
     *
     * @return TerminalExecuteResult
     */
    public static TerminalExecuteResult ok() {
        return new TerminalExecuteResult();
    }

    /**
     * 执行失败
     *
     * @param exception 异常信息
     * @return TerminalExecuteResult
     */
    public static TerminalExecuteResult fail(Exception exception) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        result.exception = exception;
        return result;
    }

    public String result() {
        return this.result == null ? "" : this.result.toString();
    }
}
