package cn.oyzh.fx.common.ssh;

import com.jcraft.jsch.JSchException;

/**
 * @author oyzh
 * @since 2023/12/15
 */
public class SSHException extends RuntimeException{

    public SSHException() {
        super();
    }

    public SSHException(String message) {
        super(message);
    }

    public SSHException(String s, Throwable e) {
        super(s, e);
    }

    public SSHException(JSchException ex) {
        super(ex.getMessage(), ex.getCause());
    }
}
