package cn.oyzh.fx.common.util;

import cn.hutool.core.io.FileUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author oyzh
 * @since 2023/11/14
 */
@Slf4j
@UtilityClass
public class RuntimeUtil {

    /**
     * 执行并等待
     *
     * @param cmd 命令
     * @return 执行结果
     * @throws Exception 异常
     */
    public static int execAndWait(String cmd) throws Exception {
        return execAndWait(cmd, null, true, true);
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
        return execAndWait(cmd, new File(dir), true, true);
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
        return execAndWait(cmd, dir, true, true);
    }

    /**
     * 执行并等待
     *
     * @param cmd        命令
     * @param dir        执行目录
     * @param printInput 打印输入内容
     * @param printError 打印异常内容
     * @return 执行结果
     * @throws Exception 异常
     */
    public static int execAndWait(String cmd, File dir, boolean printInput, boolean printError) throws Exception {
        int code = 0;
        try {
            log.info("execAndWait start cmd:{} dir:{} printInput:{} printError:{}", cmd, dir, printInput, printError);
            Process process;
            if (FileUtil.isDirectory(dir)) {
                process = Runtime.getRuntime().exec(cmd, null, dir);
            } else {
                process = Runtime.getRuntime().exec(cmd, null);
            }
            if (printInput) {
                log.info("process input--->start");
                // 获取进程的标准输出流
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                // 读取输出并打印到控制台
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
                log.info("process input--->start");
            }
            if (printError) {
                log.info("process error--->start");
                // 获取进程的标准输出流
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                // 读取输出并打印到控制台
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
                log.info("process error--->end");
            }
            // 等待进程执行完成
            return code = process.waitFor();
        } finally {
            log.info("execAndWait finish code:{}", code);
        }
    }
}
