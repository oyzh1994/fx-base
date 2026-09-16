package cn.oyzh.fx.pkg.jar;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.common.function.ExceptionConsumer;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ProcessExecResult;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.filter.RegFilter;
import cn.oyzh.fx.pkg.util.JarUtil;
import cn.oyzh.fx.pkg.util.PkgUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * jar处理器
 *
 * @author oyzh
 * @since 2024/06/17
 */
public class JarHandler implements PreHandler {

    private int order = PackOrder.ORDER_P7;

    @Override
    public int order() {
        return order;
    }

    @Override
    public void order(int order) {
        this.order = order;
    }

    private RegFilter filter;

    private RegFilter skipFilter;

    @Override
    public String name() {
        return "jar处理器";
    }

    /**
     * jar配置
     */
    private PackConfig config;

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        JarConfig jarConfig = packConfig.getJarConfig();
        if (jarConfig == null) {
            return;
        }
        this.config = packConfig;
        String jdkPath = packConfig.getJdkPath();
        if (StringUtil.isBlank(jdkPath)) {
            throw new Exception("jdkPath为空！");
        }
        this.filter = new RegFilter(jarConfig.getExcludes());
        this.skipFilter = new RegFilter(jarConfig.getSkipsJar());
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
        // 裁剪文件
        if (jarConfig.isEnable()) {
            // 裁剪主jar
            JarUtil.minimize(src, dest, this::jarFilter);
            // 裁剪类库jar
            this.handleLibs(jarUnDir);
            // 合并类库jar
            this.mergeLibs(jarUnDir, dest, jdkPath);
        } else {// 不裁剪
            JulLog.warn("jar裁剪未启用，已跳过");
            FileUtil.copy(src, dest, true);
        }
        // 设置最小化后的主程序
        packConfig.setMinimizeManJar(dest);
        // 设置jar解压目录
        packConfig.setJarUnDir(jarUnDir);
    }

    /**
     * 处理jfx库
     *
     * @param src  路径
     * @param name 名称
     */
    private void handleJfxLib(String src, String name) {
        String javafxPath = this.config.getJarConfig().getJavafxPath();
        try {
            // 初始化jfx路径
            if (this.config.getJarConfig().getJavafxPath() == null) {
                Path path = Paths.get(SystemUtil.tmpdir(), "_temp_javafx_" + UUIDUtil.uuidSimple());
                Files.createDirectory(path);
                javafxPath = path.toString();
                this.config.getJarConfig().setJavafxPath(javafxPath);
            }
            String subName = null;
            if (src.contains("javafx-graphics-")) {
                subName = "javafx.graphics.jmod";
            } else if (src.contains("javafx-media-")) {
                subName = "javafx.media.jmod";
            } else if (src.contains("javafx-web-")) {
                subName = "javafx.web.jmod";
            }
            // jmods处理
            if (subName != null) {
                String javaHome = SystemUtil.javaHome();
                Path path = Paths.get(javaHome, "jmods", subName);
                String jdkPath = this.config.getJdkPath();
                // 检查jmods文件是否存在
                if (Files.exists(path)) {
                    String modDir = path.toFile().getName();
                    modDir = modDir.substring(0, modDir.lastIndexOf("."));
                    Path path1 = Paths.get(javaHome, "jmods", modDir);
                    String[] cmd = PkgUtil.getJModCMD(path1.toString(), path.toString());
                    cmd = PkgUtil.getJDKExecCMD(jdkPath, cmd);
                    String cmdStr = StringUtil.join(" ", cmd);
                    JulLog.info("JMod cmd:{}", "\n" + cmdStr);
                    ProcessExecResult result = RuntimeUtil.execForResult(cmd);
                    JulLog.info("JMod result:{}", result);
                    if (!result.isSuccess()) {
                        JulLog.error("JMod error:{}", result.getError());
                        throw new Exception("JMod error:" + result.getError());
                    }
                    String finalJavafxPath = javafxPath;
                    cn.oyzh.common.file.FileUtil.getAllFiles(path1.toFile(), (ExceptionConsumer<File>) file -> {
                        if (!StringUtil.endsWithAny(file.getName(), ".dylib", ".dll", ".so")) {
                            return;
                        }
                        Path path2 = Paths.get(finalJavafxPath, file.getName());
                        if (Files.exists(path2)) {
                            return;
                        }
                        Files.copy(file.toPath(), path2);
                    });
                    return;
                }
            }

            // 普通jar处理
            try (JarInputStream jarIn = new JarInputStream(new BufferedInputStream(new FileInputStream(src)))) {
                ZipEntry entry;
                while ((entry = jarIn.getNextJarEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    // 匹配目标条目
                    if (entry.getName().equals(name)) {
                        Files.copy(jarIn, Paths.get(javafxPath, name));
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * jar过滤
     *
     * @param src  源文件
     * @param name 名称
     * @return 结果
     */
    private boolean jarFilter(String src, String name) {
        // jar包不处理
        if (name.endsWith(".jar")) {
            return true;
        }
        // jfx优化
        if (this.config.getJarConfig().isJavafxOptimize()) {
            if (src.endsWith(".jar")
                    && StringUtil.containsAny(src, "javafx-media-", "javafx-graphics-", "javafx-web-")
                    && StringUtil.endsWithAny(name, ".dylib", ".dll", ".so")) {
                this.handleJfxLib(src, name);
                JulLog.info("javafx模块，文件:{}被过滤.", name);
                return false;
            }
        }
        // 其他文件
        boolean accept = this.filter.apply(name);
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
        List<File> files = cn.oyzh.common.file.FileUtil.getAllFiles(jarUnDir);
        for (File file : files) {
            try {
                // 非jar，跳过
                if (!JarUtil.isJar(file)) {
                    continue;
                }
                // 符合跳过jar，忽略文件
                if (!this.skipFilter.apply(file.getName())) {
                    JulLog.warn("类库:{}被跳过, 已忽略.", file.getName());
                    continue;
                }
                // 符合排除jar，删除文件
                if (!this.filter.apply(file.getName())) {
                    cn.oyzh.common.file.FileUtil.del(file);
                    JulLog.warn("类库:{}被排除, 已删除.", file.getName());
                    continue;
                }
                // 内容为空
                if (this.config.getJarConfig().isRemoveEmpty() && !JarUtil.hasClass(file.getPath())) {
                    cn.oyzh.common.file.FileUtil.del(file);
                    JulLog.warn("类库:{}内容为空, 已删除.", file.getName());
                    continue;
                }
                // 替换路径
                JulLog.info("minimize jar: {}.", file.getName());
                // 裁剪类库
                JarUtil.minimize(file.getPath(), file.getPath(), this::jarFilter);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
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
            String[] cmdArr = new String[]{"jar", "-uvf0", mainJarNewFile.getName(), "./BOOT-INF/lib"};
            cmdArr = PkgUtil.getJDKExecCMD(jdkPath, cmdArr);
            String cmdStr = StringUtil.join(" ", cmdArr);
            JulLog.info(cmdStr);
            ProcessExecResult result = RuntimeUtil.execForResult(cmdArr, null, dir);
            if (!result.isSuccess()) {
                JulLog.error("Jar error:{} exitCode:{}", result.getError(), result.getExitCode());
                throw new RuntimeException("Jar error:" + result.getError() + " exitCode:" + result.getExitCode());
            }
        } else {// 单个jar逐个合并
            List<File> files = FileUtil.loopFiles(dir);
            files = files.parallelStream().filter(f -> f.isFile() && f.getName().endsWith(".jar")).toList();
            for (File file : files) {
                String fName = file.getPath().replace(dir.getPath(), "");
                String[] cmdArr = new String[]{"jar", "-uvf0", mainJarNewFile.getName(), "." + fName};
                cmdArr = PkgUtil.getJDKExecCMD(jdkPath, cmdArr);
                String cmdStr = StringUtil.join(" ", cmdArr);
                JulLog.info(cmdStr);
                ProcessExecResult result = RuntimeUtil.execForResult(cmdArr, null, dir);
                if (!result.isSuccess()) {
                    JulLog.error("Jar error:{} exitCode:{}", result.getError(), result.getExitCode());
                    throw new RuntimeException("Jar error:" + result.getError() + " exitCode:" + result.getExitCode());
                }
            }
        }
        // 移动主jar文件到原始目录
        FileUtil.move(mainJarNewFile, new File(mainJar), true);
        JulLog.info("mergeLibs finish.");
    }

}
