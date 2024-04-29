package cn.oyzh.fx.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.StaticLog;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author oyzh
 * @since 2023/11/14
 */
@UtilityClass
public class RuntimeUtil {

    /**
     * 获取处理器数量
     *
     * @return 处理器数量
     */
    public static int processorCount() {
        int count = Runtime.getRuntime().availableProcessors();
        if (count <= 0) {
            count = 1;
        }
        return count;
    }

    /**
     * 执行并等待
     *
     * @param cmd 命令
     * @return 执行结果
     * @throws Exception 异常
     */
    public static int execAndWait(String cmd) throws Exception {
        return execAndWait(cmd, (File) null);
    }

    /**
     * 执行并等待
     *
     * @param cmd 命令
     * @param dir 执行目录
     * @return 执行结果
     * @throws Exception 异常
     */
    public static int execAndWait(String cmd, String dir) throws Exception {
        return execAndWait(cmd, new File(dir));
    }

    /**
     * 执行并等待
     *
     * @param cmd 命令
     * @param dir 执行目录
     * @return 执行结果
     * @throws Exception 异常
     */
    public static int execAndWait(String cmd, File dir) throws Exception {
        Charset charset;
        if (OSUtil.isWindows()) {
            charset = Charset.forName(System.getProperty("sun.jnu.encoding"));
        } else {
            charset = Charset.defaultCharset();
        }
        return execAndWait(cmd, dir, true, true, charset);
    }

    /**
     * 执行并等待
     *
     * @param cmd        命令
     * @param dir        执行目录
     * @param printInput 打印输入内容
     * @param printError 打印异常内容
     * @param charset    流打印字符集
     * @return 执行结果
     * @throws Exception 异常
     */
    public static int execAndWait(String cmd, File dir, boolean printInput, boolean printError, Charset charset) throws Exception {
        int code = 0;
        try {
            if (charset == null) {
                charset = Charset.defaultCharset();
            }
            StaticLog.info("execAndWait start cmd:{} dir:{} printInput:{} printError:{}", cmd, dir, printInput, printError);
            Process process;
            if (FileUtil.isDirectory(dir)) {
                process = Runtime.getRuntime().exec(cmd, null, dir);
            } else {
                process = Runtime.getRuntime().exec(cmd, null);
            }
            if (process.getInputStream().available() <= 0) {
                process.waitFor(1000, TimeUnit.MILLISECONDS);
            }
            if (printInput && process.getInputStream().available() > 0) {
                StaticLog.info("process input--->start");
                // 获取进程的标准输出流
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
                // 读取输出并打印到控制台
                String line;
                while ((line = reader.readLine()) != null) {
                    StaticLog.info(line);
                }
                StaticLog.info("process input--->start");
            }
            if (printError && process.getErrorStream().available() > 0) {
                StaticLog.info("process error--->start");
                // 获取进程的标准输出流
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), charset));
                // 读取输出并打印到控制台
                String line;
                while ((line = reader.readLine()) != null) {
                    StaticLog.info(line);
                }
                StaticLog.info("process error--->end");
            }
            // 等待进程执行完成
            return code = process.waitFor();
        } finally {
            StaticLog.info("execAndWait finish code:{}", code);
        }
    }
}
