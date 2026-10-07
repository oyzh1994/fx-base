package cn.oyzh.fx.terminal.execute;


/**
 * 终端执行结果
 *
 * @author oyzh
 * @since 2023-10-09
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

    /**
     * 是否忽略输出。
     *
     * @return 忽略输出
     */
    public boolean isIgnoreOutput() {
        return ignoreOutput;
    }

    /**
     * 设置忽略输出。
     *
     * @param ignoreOutput 忽略输出
     */
    public void setIgnoreOutput(boolean ignoreOutput) {
        this.ignoreOutput = ignoreOutput;
    }

    /**
     * 获取异常。
     *
     * @return 异常
     */
    public Exception getException() {
        return exception;
    }

    /**
     * 设置异常。
     *
     * @param exception 异常
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * 设置错误消息。
     *
     * @param errMsg 错误消息
     */
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    /**
     * 获取结果。
     *
     * @return 结果
     */
    public Object getResult() {
        return result;
    }

    /**
     * 设置结果。
     *
     * @param result 结果
     */
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
//
//    /**
//     * 追加结果，附带换行
//     *
//     * @param result 结果
//     */
//    public void appendResultLine(String result) {
//        if (result == null) {
//            return;
//        }
//        if (this.result == null) {
//            this.result = "";
//        } else if (!this.result.toString().endsWith("\n")) {
//            this.result += "\n";
//        }
//        this.result += result;
//    }

    /**
     * 执行成功
     *
     * @return 执行结果
     */
    public static TerminalExecuteResult ok() {
        return new TerminalExecuteResult();
    }

    /**
     * 执行失败
     *
     * @param exception 异常信息
     * @return 执行结果
     */
    public static TerminalExecuteResult fail(Exception exception) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        result.exception = exception;
        return result;
    }

    /**
     * 获取结果
     *
     * @return 结果
     */
    public String result() {
        return this.result == null ? "" : this.result.toString();
    }
}
