package cn.oyzh.fx.common.ssh;

import lombok.Data;

/**
 * ssh转发信息
 *
 * @author oyzh
 * @since 2023/12/15
 */
@Data
public class SSHForwardInfo {

    /**
     * 远程端口
     */
    private int port;

    /**
     * 远程地址
     */
    private String host;

    /**
     * 本地端口
     */
    private int localPort;

}
