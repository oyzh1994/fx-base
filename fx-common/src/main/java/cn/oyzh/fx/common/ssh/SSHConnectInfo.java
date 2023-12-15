package cn.oyzh.fx.common.ssh;

import lombok.Data;

/**
 * ssh连接信息
 *
 * @author oyzh
 * @since 2023/12/15
 */
@Data
public class SSHConnectInfo {

    /**
     * 连接端口，默认22
     */
    private int port = 22;

    /**
     * 连接地址
     */
    private String host;

    /**
     * ssh用户名
     */
    private String user;

    /**
     * ssh密码
     */
    private String password;

    /**
     * 连接超时
     */
    private int timeout = 5000;
}
