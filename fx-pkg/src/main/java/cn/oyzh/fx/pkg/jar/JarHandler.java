package cn.oyzh.fx.pkg.jar;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.filter.RegFilter;
import cn.oyzh.fx.pkg.util.JarUtil;
import cn.oyzh.fx.pkg.util.PkgUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.List;

/**
 * jar处理器
 *
 * @author oyzh
 * @since 2024/06/17
 */
public class JarHandler implements PreHandler {

    @Getter
    @Setter
    @Accessors(chain = false, fluent = true)
    private int order = PackOrder.ORDER_P7;

    private RegFilter filter;

    @Override
    public String name() {
        return "jar处理器";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        JarConfig jarConfig = packConfig.getJarConfig();
        if (jarConfig == null) {
            return;
        }
        String jdkPath = packConfig.getJdkPath();
        if (StringUtil.isBlank(jdkPath)) {
            throw new Exception("jdkPath为空！");
        }
        this.filter = new RegFilter(jarConfig.getExcludes());
        // 来源文件
        String src = packConfig.getMainJar();
        // 目标文件
        String dest = src.replace(".jar", "_clip.jar");
        // jar解压目录
        String jarUnDir = src.replace(".jar", "");
        // 删除解压目录，如果存在
        FileUtil.del(jarUnDir);
        // 删除目标文件，如果存在
        FileUtil.del(dest);
        // 解压主jar
        JarUtil.unJar(src, jarUnDir);
        // 裁剪主jar
        JarUtil.minimize(src, dest, this::jarFilter);
        // 裁剪类库jar
        this.handleLibs(jarUnDir);
        // 合并类库jar
        this.mergeLibs(jarUnDir, dest, jdkPath);
        // 设置最小化后的主程序
        packConfig.setMinimizeManJar(dest);
        // 设置jar解压目录
        packConfig.setJarUnDir(jarUnDir);
    }

    /**
     * jar过滤
     *
     * @param name 名称
     * @return 结果
     */
    private boolean jarFilter(String name) {
        boolean accept;
        // jar包不处理
        if (name.endsWith(".jar")) {
            accept = false;
        } else {// 其他文件
            accept = this.filter.apply(name);
        }
        if (!accept) {
            JulLog.info("文件:{}被过滤.", name);
        }
        return accept;
    }

    /**
     * 处理类库
     *
     * @param jarUnDir 主jar解压目录
     */
    private void handleLibs(String jarUnDir) {
        JulLog.info("handleLibs start, jarUnDir: {}.", jarUnDir);
        List<File> files = FileUtil.loopFiles(jarUnDir);
        for (File file : files) {
            try {
                // 非jar，跳过
                if (!JarUtil.isJar(file)) {
                    continue;
                }
                // 符合排除jar，删除文件
                if (!this.filter.apply(file.getName())) {
                    FileUtil.del(file);
                    JulLog.warn("类库:{}被排除, 已删除.", file.getName());
                    continue;
                }
                // 内容为空
                if (!JarUtil.hasClass(file.getPath())) {
                    FileUtil.del(file);
                    JulLog.warn("类库:{}内容为空, 已删除.", file.getName());
                    continue;
                }
                // 替换路径
                JulLog.info("minimize jar: {}.", file.getName());
                // 裁剪类库
                JarUtil.minimize(file.getPath(), file.getPath(), this::jarFilter);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        JulLog.info("handleLibs finish.");
    }

    /**
     * 合并类库
     *
     * @param jarUnDir 主jar解压目录
     * @param mainJar  主jar
     * @param jdkPath  jdk路径
     */
    private void mergeLibs(String jarUnDir, String mainJar, String jdkPath) throws Exception {
        JulLog.info("mergeLibs start, jarUnDir: {} mainJar: {}.", jarUnDir, mainJar);
        // 新jar文件
        File mainJarNewFile = new File(jarUnDir, "temp.jar");
        // 复制解压目录
        FileUtil.copy(mainJar, mainJarNewFile.getPath(), false);
        // 解压目录
        File dir = new File(jarUnDir);
        // lib目录合并
        if (FileUtil.exist(jarUnDir + "/BOOT-INF/lib")) {
            // 合并lib目录到主jar文件
            String cmdStr = "jar -uvf0 " + mainJarNewFile.getName() + " ./BOOT-INF/lib";
            cmdStr = PkgUtil.getJDKExecCMD(jdkPath, cmdStr);
            RuntimeUtil.execAndWait(cmdStr, dir);
        } else {// 单个jar逐个合并
            List<File> files = FileUtil.loopFiles(dir);
            files = files.parallelStream().filter(f -> f.isFile() && f.getName().endsWith(".jar")).toList();
            for (File file : files) {
                String fName = file.getPath().replace(dir.getPath(), "");
                String cmdStr = "jar -uvf0 " + mainJarNewFile.getName() + " ." + fName;
                cmdStr = PkgUtil.getJDKExecCMD(jdkPath, cmdStr);
                RuntimeUtil.execAndWait(cmdStr, dir);
            }
        }
        // 移动主jar文件到原始目录
        FileUtil.move(mainJarNewFile, new File(mainJar), true);
        JulLog.info("mergeLibs finish.");
    }

}
