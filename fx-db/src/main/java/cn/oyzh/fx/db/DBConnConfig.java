package cn.oyzh.fx.db;

/**
 * 数据库连接配置，保存连接所需的地址、认证及代理等信息
 *
 * @author oyzh
 * @since 2026-09-01
 */
public class DBConnConfig {

    /**
     * 环境标识
     */
    private String env;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 用户名
     */
    private String user;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否启用SSL
     */
    private boolean useSSL;

    /**
     * 代理主机地址
     */
    private String proxyHost;

    /**
     * 代理端口
     */
    private Integer proxyPort;

    /**
     * 代理用户名
     */
    private String proxyUser;

    /**
     * 代理类型
     */
    private String proxyType;

    /**
     * 代理密码
     */
    private String proxyPassword;

    /**
     * 套接字工厂
     */
    private String socketFactory;

    /**
     * 连接超时时间(秒)
     */
    private int connectTimeout = 5;

    /**
     * 获取主机。
     *
     * @return 主机
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置主机。
     *
     * @param host 主机
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 获取端口。
     *
     * @return 端口
     */
    public Integer getPort() {
        return port;
    }

    /**
     * 设置端口。
     *
     * @param port 端口
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * 获取用户。
     *
     * @return 用户
     */
    public String getUser() {
        return user;
    }

    /**
     * 设置用户。
     *
     * @param user 用户
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * 获取密码。
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码。
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取代理主机。
     *
     * @return 代理主机
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * 设置代理主机。
     *
     * @param proxyHost 代理主机
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * 获取代理端口。
     *
     * @return 代理端口
     */
    public Integer getProxyPort() {
        return proxyPort;
    }

    /**
     * 设置代理端口。
     *
     * @param proxyPort 代理端口
     */
    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * 获取代理用户。
     *
     * @return 代理用户
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * 设置代理用户。
     *
     * @param proxyUser 代理用户
     */
    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    /**
     * 获取代理密码。
     *
     * @return 代理密码
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * 设置代理密码。
     *
     * @param proxyPassword 代理密码
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /**
     * 获取代理类型。
     *
     * @return 代理类型
     */
    public String getProxyType() {
        return proxyType;
    }

    /**
     * 设置代理类型。
     *
     * @param proxyType 代理类型
     */
    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    /**
     * 获取套接字工厂。
     *
     * @return 套接字工厂
     */
    public String getSocketFactory() {
        return socketFactory;
    }

    /**
     * 设置套接字工厂。
     *
     * @param socketFactory 套接字工厂
     */
    public void setSocketFactory(String socketFactory) {
        this.socketFactory = socketFactory;
    }

    /**
     * 获取连接超时。
     *
     * @return 连接超时
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置连接超时。
     *
     * @param connectTimeout 连接超时
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * 是否使用SSL。
     *
     * @return 使用SSL
     */
    public boolean isUseSSL() {
        return useSSL;
    }

    /**
     * 设置使用SSL。
     *
     * @param useSSL 是否使用SSL
     */
    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    /**
     * 获取环境。
     *
     * @return 环境
     */
    public String getEnv() {
        return env;
    }

    /**
     * 设置环境。
     *
     * @param env 环境
     */
    public void setEnv(String env) {
        this.env = env;
    }
}
