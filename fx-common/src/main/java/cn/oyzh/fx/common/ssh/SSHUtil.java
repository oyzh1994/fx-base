package cn.oyzh.fx.common.ssh;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;


/**
 * ssh工具类
 *
 * @author oyzh
 * @since 2023/12/15
 */
@UtilityClass
public class SSHUtil {

    /**
     * 排除端口
     */
    private final static Set<Integer> EXCLUDED_PORT = new HashSet<>();

    static {
        EXCLUDED_PORT.add(21);
        EXCLUDED_PORT.add(22);
        EXCLUDED_PORT.add(23);
        EXCLUDED_PORT.add(25);
        EXCLUDED_PORT.add(53);
        EXCLUDED_PORT.add(80);
        EXCLUDED_PORT.add(99);
        EXCLUDED_PORT.add(110);
        EXCLUDED_PORT.add(113);
        EXCLUDED_PORT.add(119);
        EXCLUDED_PORT.add(135);
        EXCLUDED_PORT.add(137);
        EXCLUDED_PORT.add(138);
        EXCLUDED_PORT.add(139);
        EXCLUDED_PORT.add(143);
        EXCLUDED_PORT.add(161);
        EXCLUDED_PORT.add(163);
        EXCLUDED_PORT.add(389);
        EXCLUDED_PORT.add(443);
        EXCLUDED_PORT.add(445);
        EXCLUDED_PORT.add(1433);
        EXCLUDED_PORT.add(2181);
        EXCLUDED_PORT.add(3306);
        EXCLUDED_PORT.add(3389);
        EXCLUDED_PORT.add(6379);
        EXCLUDED_PORT.add(8080);
    }

    /**
     * 寻找可用的端口
     *
     * @return 结果
     */
    public static int findAvailablePort() {
        // 默认从10000-12000区间里面找
        return findAvailablePort(null, 10_000, 12_000);
    }

    /**
     * 寻找可用的端口
     *
     * @param excludes 排除的列表
     * @return 结果
     */
    public static int findAvailablePort(Set<Integer> excludes) {
        // 默认从10000-12000区间里面找
        return findAvailablePort(excludes, 10_000, 12_000);
    }

    /**
     * 寻找可用的端口
     *
     * @param excludes 排除的列表
     * @param start    开始端口
     * @param end      结束端口
     * @return 可用端口，-1代表未找到
     */
    public static int findAvailablePort(Set<Integer> excludes, int start, int end) {
        for (int i = start; i <= end; i++) {
            if (excludes != null && excludes.contains(i)) {
                continue;
            }
            if (EXCLUDED_PORT.contains(i)) {
                continue;
            }
            if (isPortAvailable(i, 100)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 检查本地端口是否可用
     *
     * @param port    端口
     * @param timeout 超时
     * @return 结果
     */
    public static boolean isPortAvailable(int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), timeout);
            // 端口被占用
            return false;
        } catch (IOException ignored) {
        }
        // 端口未被占用
        return true;
    }
}
