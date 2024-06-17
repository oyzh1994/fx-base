package cn.oyzh.fx.pkg.jar;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.common.util.RuntimeUtil;
import cn.oyzh.fx.pkg.ConfigParser;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.clip.filter.ClassFilter;
import cn.oyzh.fx.pkg.clip.filter.FileFilter;
import cn.oyzh.fx.pkg.clip.filter.JarFilter;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;
import cn.oyzh.fx.pkg.util.JarUtil;
import cn.oyzh.fx.pkg.util.PkgUtil;

import java.io.File;
import java.util.List;

/**
 * 主jar裁剪器
 *
 * @author oyzh
 * @since 2022/12/14
 */
public class JarHandler implements PreHandler, ConfigParser<JarConfig> {

    private final JarConfig config;

    /**
     * jar过滤器
     */
    protected final JarFilter jarFilter = new JarFilter();

    /**
     * 类过滤器
     */
    protected final ClassFilter classFilter = new ClassFilter();

    /**
     * 文件过滤器
     */
    protected final FileFilter fileFilter = new FileFilter();

    public JarHandler(String configFile) {
        this.config = this.parse(configFile);
        this.jarFilter.addExcludes(this.config.getExcludeJars());
        this.fileFilter.addExcludes(this.config.getExcludeFiles());
        this.classFilter.addExcludes(this.config.getExcludeClasses());
    }

    @Override
    public void handle(ExtPackrConfig packrConfig) throws Exception {
        StaticLog.info("clip start.");
        // 来源文件
        String src = packrConfig.getMainJar();
        // 目标文件
        String dest = src.replace(".jar", "_clip.jar");
        long start = System.currentTimeMillis();
        // jar解压目录
        String jarUnDir = src.replace(".jar", "");
        // 删除解压目录
        FileUtil.del(jarUnDir);
        // 解压主jar
        JarUtil.unJar(src, jarUnDir);
        // 设置jar解压目录
        packrConfig.setJarUnDir(jarUnDir);
        // 裁剪主jar
        JarUtil.minimize(src, dest, this::filterName);
        // 裁剪类库jar
        this.handleLibs(jarUnDir);
        // 合并类库jar
        this.mergeLibs(jarUnDir, dest, packrConfig.jdk);
        packrConfig.setMinimizeManJar(dest);
        long end = System.currentTimeMillis();
        StaticLog.info("clip end, used time: {}ms.", end - start);
    }

    @Override
    public JarConfig parse(String configFile) {
        JSONObject object = JSONUtil.parseObj(FileUtil.readUtf8String(configFile));
        JarConfig config1 = new JarConfig();
        config1.parseConfig(object);
        return config1;
    }

    public boolean filterName(String name) {
        boolean accept;
        // class文件
        if (name.endsWith(".class")) {
            accept = this.classFilter.accept(name);
        } else if (name.endsWith(".jar")) { // jar包不处理
            accept = false;
        } else {// 其他文件
            accept = this.fileFilter.accept(name);
        }
        if (!accept) {
            StaticLog.info("file:{} filtered.", name);
        }
        return accept;
    }

    /**
     * 处理类库
     *
     * @param jarUnDir 主jar解压目录
     */
    private void handleLibs(String jarUnDir) {
        StaticLog.info("handleLibs start, jarUnDir: {}.", jarUnDir);
        List<File> files = FileUtil.loopFiles(jarUnDir);
        for (File file : files) {
            try {
                // 非jar，跳过
                if (!JarUtil.isJar(file)) {
                    continue;
                }
                // 符合排除jar，删除文件
                if (this.jarFilter.acceptExclude(file.getName())) {
                    FileUtil.del(file);
                    StaticLog.warn("类库:{}被排除, 已删除.", file.getName());
                    continue;
                }
                // 内容为空
                if (!JarUtil.hasClass(file.getPath())) {
                    FileUtil.del(file);
                    StaticLog.warn("类库:{}内容为空, 已删除.", file.getName());
                    continue;
                }
                // 替换路径
                StaticLog.info("minimize jar: {}.", file.getName());
                // 裁剪类库
                File dest = JarUtil.minimize(file.getPath(), null, this::filterName);
                // 覆盖类库
                FileUtil.move(dest, file, true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        StaticLog.info("handleLibs finish.");
    }

    /**
     * 合并类库
     *
     * @param jarUnDir 主jar解压目录
     * @param mainJar  主jar
     * @param jdkPath  jdk路径
     */
    private void mergeLibs(String jarUnDir, String mainJar, String jdkPath) {
        StaticLog.info("mergeLibs start, jarUnDir: {} mainJar: {}.", jarUnDir, mainJar);
        // jar文件
        File mainJarFile = new File(mainJar);
        // 新jar文件
        File mainJarNewFile = new File(jarUnDir, mainJarFile.getName());
        // 移动到解压目录
        if (mainJarNewFile.exists()) {
            FileUtil.move(mainJarFile, mainJarNewFile, true);
        } else {
            FileUtil.copy(mainJarFile, mainJarNewFile, false);
        }
        // 解压目录
        File dir = new File(jarUnDir);
        // lib目录合并
        if (FileUtil.exist(jarUnDir + "/BOOT-INF/lib")) {
            try {
                // 合并lib目录到主jar文件
                String cmdStr = "jar -uvf0 " + mainJarFile.getName() + " ./BOOT-INF/lib";
                cmdStr = PkgUtil.getJDKExecCMD(jdkPath, cmdStr);
                RuntimeUtil.execAndWait(cmdStr, dir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {// 单个jar逐个合并
            List<File> files = FileUtil.loopFiles(dir);
            files = files.parallelStream().filter(f -> f.isFile() && f.getName().endsWith(".jar")).toList();
            for (File file : files) {
                try {
                    String fName = file.getPath().replace(dir.getPath(), "");
                    String cmdStr = "jar -uvf0 " + mainJarFile.getName() + " ." + fName;
                    cmdStr = PkgUtil.getJDKExecCMD(jdkPath, cmdStr);
                    RuntimeUtil.execAndWait(cmdStr, dir);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // 移动主jar文件到原始目录
        FileUtil.move(mainJarNewFile, mainJarFile, true);
        StaticLog.info("mergeLibs finish.");
    }
}
