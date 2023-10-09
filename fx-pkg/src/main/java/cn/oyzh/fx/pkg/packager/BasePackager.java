package cn.oyzh.fx.pkg.packager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.compress.CompressUtil;
import cn.hutool.extra.compress.archiver.Archiver;
import cn.oyzh.fx.pkg.clip.clipper.JarClipConfig;
import cn.oyzh.fx.pkg.clip.clipper.JarClipper;
import cn.oyzh.fx.pkg.clip.clipper.JreClipConfig;
import cn.oyzh.fx.pkg.clip.clipper.JreClipper;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.jlink.JLinkHandler;
import cn.oyzh.fx.pkg.jpackage.JPackageConfig;
import cn.oyzh.fx.pkg.jpackage.JPackageHandler;
import cn.oyzh.fx.pkg.packr.PackrPackager;
import com.badlogicgames.packr.PackrConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 基础打包实现
 *
 * @author oyzh
 * @since 2023/3/8
 */
@Slf4j
public abstract class BasePackager {

    /**
     * jar裁剪器
     */
    @Getter
    @Setter
    private JarClipper jarClipper;

    /**
     * jar裁剪配置
     */
    @Getter
    private JarClipConfig jarClipConfig;

    /**
     * jre裁剪器
     */
    @Getter
    @Setter
    private JreClipper jreClipper;

    /**
     * jar裁剪配置
     */
    @Getter
    private JreClipConfig jreClipConfig;

    /**
     * jink处理器
     */
    @Getter
    protected JLinkHandler jLinkHandler;

    /**
     * jink配置
     */
    @Getter
    private JLinkConfig jLinkConfig;

    /*
     * 裁剪的jar
     */
    @Getter
    protected String clapJar;

    /**
     * jre裁剪目录
     */
    @Getter
    protected String jreClipDir;

    /*
     * jlink目录
     */
    @Getter
    protected String jreJLinkDir;

    /**
     * 是否删除裁剪的jar
     */
    @Getter
    @Setter
    private boolean deleteClapJar = true;

    /**
     * 是否删除打包目录
     */
    @Getter
    @Setter
    private boolean clearPackDir = true;

    /**
     * 是否删除裁剪的jre目录
     */
    @Getter
    @Setter
    private boolean clearJreClipDir = true;

    /**
     * 是否删除jlink目录
     */
    @Getter
    @Setter
    private boolean clearJreJLinkDir = true;

    /**
     * packr打包处理
     */
    protected final PackrPackager packrPackager = new PackrPackager();

    /**
     * jpackage打包处理
     */
    protected final JPackageHandler jPackageHandler = new JPackageHandler();

    /**
     * 打包前处理
     *
     * @throws Exception 异常
     */
    protected void packBefore() throws Exception {
        if (this.jLinkHandler != null) {
            log.info("jlink start.");
            this.jLinkHandler.exec(this.jLinkConfig);
            this.getPkgConfig().setJrePath(this.jLinkConfig.getOutput());
            this.jreJLinkDir = this.jLinkConfig.getOutput();
            log.info("jlink finish.");
        }
        if (this.jreClipper != null) {
            log.info("jreClip start.");
            this.jreClipper.clip(this.jreClipConfig);
            this.getPkgConfig().setJrePath(this.jreClipConfig.getDest());
            this.jreClipDir = this.jreClipConfig.getDest();
            log.info("jreClip finish.");
        }
        if (this.jarClipper != null) {
            log.info("jarClip start.");
            this.jarClipper.clip(this.jarClipConfig);
            this.getPkgConfig().setJarPath(this.jarClipConfig.getDest());
            this.clapJar = this.jarClipConfig.getDest();
            log.info("jarClip finish.");
        }
    }

    /**
     * 打包
     *
     * @throws Exception 异常
     */
    public abstract void pack() throws Exception;

    /**
     * 打包后处理
     */
    protected void packAfter() {
        log.info("pack after start.");
        if (StrUtil.isNotBlank(this.getPkgConfig().getCompressType())) {
            switch (this.getPkgConfig().getCompressType().toLowerCase()) {
                case "zip" -> this.zipDest();
                case "tar" -> this.tarDest();
                case "tar.gz" -> this.gzipDest();
            }
            this.clearPackDir = true;
        } else {
            this.clearPackDir = false;
        }
        // 删除裁剪的jar
        if (this.deleteClapJar && this.getClapJar() != null) {
            FileUtil.del(this.getClapJar());
            log.info("delete ClapJar.");
        }
        // 删除打包目录
        if (this.clearPackDir && this.getPkgConfig().getDestFile() != null) {
            FileUtil.del(this.getPkgConfig().getDestFile());
            log.info("clear PackDir.");
        }
        // 删除jre裁剪目录
        if (this.clearJreClipDir && this.getJreClipDir() != null) {
            FileUtil.del(this.getJreClipDir());
            log.info("clear JreClipDir.");
        }
        // 删除jlink目录
        if (this.clearJreJLinkDir && this.getJreJLinkDir() != null) {
            FileUtil.del(this.getJreJLinkDir());
            log.info("clear JreJLinkDir.");
        }
        log.info("pack after finish.");
    }

    /**
     * 压缩打包目录，zip格式
     */
    protected void zipDest() {
        String compressName = this.getCompressName() + ".zip";
        log.info("zipDest start, config.compressType is:{} compressName:{}.", this.getPkgConfig().getCompressType(), compressName);
        File compressFile = new File(this.getPkgConfig().getDestFile().getParentFile(), compressName);
        // 进行zip压缩，如果是macos则保留目录名称，否则不保留
        ZipUtil.zip(this.getPkgConfig().getDestPath(), compressFile.getPath(), this.getPlatform() == PackrConfig.Platform.MacOS);
        log.info("zipDest finish.");
    }

    /**
     * 压缩打包目录，tar格式
     */
    protected void tarDest() {
        String compressName = this.getCompressName() + ".tar";
        log.info("tarDest start, config.compressType is:{} compressName:{}.", this.getPkgConfig().getCompressType(), compressName);
        File dest = this.getPkgConfig().getDestFile();
        File compressFile = new File(this.getPkgConfig().getDestFile().getParentFile(), compressName);
        // 进行tar压缩
        Archiver archiver = CompressUtil.createArchiver(StandardCharsets.UTF_8, ArchiveStreamFactory.TAR, compressFile)
                .add(dest);
        archiver.finish().close();
        log.info("tarDest finish.");
    }

    /**
     * 压缩打包文件，tar.gz格式
     */
    protected void gzipDest() {
        String compressName = this.getCompressName() + ".tar.gz";
        log.info("gzipDest start, config.compressType is:{} compressName:{}.", this.getPkgConfig().getCompressType(), compressName);
        File dest = this.getPkgConfig().getDestFile();
        File compressFile = new File(this.getPkgConfig().getDestFile().getParentFile(), compressName);
        // 进行tar.gz压缩
        Archiver archiver = CompressUtil.createArchiver(StandardCharsets.UTF_8, "tar.gz", compressFile)
                .add(dest);
        archiver.finish().close();
        log.info("gzipDest finish.");
    }

    /**
     * 获取压缩文件名称
     *
     * @return 压缩文件名称
     */
    protected String getCompressName() {
        String name = "";
        if (this.getPkgConfig().getAppName() != null) {
            name += this.getPkgConfig().getAppName();
        }
        if (this.getPkgConfig().getVersion() != null) {
            name = name + "_" + this.getPkgConfig().getVersion();
        }
        if (this.getPkgConfig().getPlatform() != null) {
            name = name + "_" + this.getPkgConfig().getPlatform();
        }
        name = name + "_release";
        return name;
    }

    /**
     * 获取打包配置
     *
     * @return 打包配置
     */
    public abstract BasePkgConfig getPkgConfig();

    /**
     * 设置jlink参数
     *
     * @param jLinkConfig jlink参数
     */
    public void setJLinkConfig(@NonNull JLinkConfig jLinkConfig) {
        if (this.jLinkHandler == null) {
            this.jLinkHandler = new JLinkHandler();
        }
        this.jLinkConfig = jLinkConfig;
    }

    /**
     * 设置jar裁剪参数
     *
     * @param jarClipConfig jar裁剪参数
     */
    public void setJarClipConfig(@NonNull JarClipConfig jarClipConfig) {
        if (this.jarClipper == null) {
            this.jarClipper = new JarClipper();
        }
        this.jarClipConfig = jarClipConfig;
    }

    /**
     * 设置jre裁剪参数
     *
     * @param jreClipConfig jre裁剪参数
     */
    public void setJreClipConfig(JreClipConfig jreClipConfig) {
        if (this.jreClipper == null) {
            this.jreClipper = new JreClipper();
        }
        this.jreClipConfig = jreClipConfig;
    }

    /**
     * 使用JPackage打包
     *
     * @throws Exception 异常
     */
    protected void packByJPackage() throws Exception {
        log.info("pack with JPackage================================");
        // 初始化参数
        JPackageConfig config = new JPackageConfig();
        // 生成输出目录，然后copy主jar到输出目录
        File inputDir;
        String inputPath = this.getPkgConfig().getDestPath() + "_jpackage_input_dir";
        if (FileUtil.exist(inputPath)) {
            FileUtil.clean(inputPath);
            inputDir = new File(inputPath);
        } else {
            inputDir = FileUtil.mkdir(inputPath);
        }
        // jpackage主jar
        File mainJar = FileUtil.copy(this.getPkgConfig().getJarFile(), inputDir, true);
        config.setVerbose(true);
        config.setMainJar(mainJar.getName());
        config.setType(this.getPkgConfig().getType());
        config.setName(this.getPkgConfig().getAppName());
        config.setIcon(this.getPkgConfig().getAppIcon());
        config.setDest(this.getPkgConfig().getDestPath());
        config.setVendor(this.getPkgConfig().getVendor());
        config.setDescription(this.getPkgConfig().getDesc());
        if (StrUtil.isNotBlank(this.getPkgConfig().getVersion())) {
            config.setAppVersion(this.getPkgConfig().getVersion().toLowerCase().replace("v", ""));
        }
        config.setRuntimeImage(this.getPkgConfig().getJrePath());
        config.setInput(inputDir.getPath());
        // 执行打包
        this.jPackageHandler.exec(config);
        // 删除输出目录
        FileUtil.del(inputDir);
    }

    /**
     * 使用Packr打包
     *
     * @throws Exception 异常
     */
    protected void packByPackr() throws Exception {
        log.info("pack with Packr================================");
        FileUtil.del(this.getPkgConfig().getDestPath());
        PackrConfig config = new PackrConfig();
        config.verbose = true;
        config.platform = this.getPlatform();
        config.vmArgs = new ArrayList<>();
        config.useZgcIfSupportedOs = true;
        config.jdk = this.getPkgConfig().getJrePath();
        config.outDir = this.getPkgConfig().getDestFile();
        config.mainClass = this.getPkgConfig().getMainClass();
        config.iconResource = this.getPkgConfig().getIconFile();
        config.executable = this.getPkgConfig().getExecutable();
        config.classpath = List.of(this.getPkgConfig().getJarPath());
        this.packrPackager.pack(config);
    }

    /**
     * 获取平台
     *
     * @return 平台
     */
    protected abstract PackrConfig.Platform getPlatform();


}
