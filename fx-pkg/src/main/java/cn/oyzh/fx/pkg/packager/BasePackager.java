package cn.oyzh.fx.pkg.packager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.thread.ThreadUtil;
import cn.oyzh.fx.pkg.clip.clipper.JarClipConfig;
import cn.oyzh.fx.pkg.clip.clipper.JarClipper;
import cn.oyzh.fx.pkg.clip.clipper.JreClipConfig;
import cn.oyzh.fx.pkg.clip.clipper.JreClipper;
import cn.oyzh.fx.pkg.config.GlobalConfig;
import cn.oyzh.fx.pkg.config.PlatformConfig;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.jlink.JLinkHandler;
import cn.oyzh.fx.pkg.jpackage.JPackageConfig;
import cn.oyzh.fx.pkg.jpackage.JPackageHandler;
import cn.oyzh.fx.pkg.packr.PackrConfigExt;
import cn.oyzh.fx.pkg.packr.PackrPackager;
import cn.oyzh.fx.pkg.util.PkgUtil;
import com.badlogicgames.packr.PackrConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

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
    protected final JarClipper jarClipper = new JarClipper();

    /**
     * jre裁剪器
     */
    protected final JreClipper jreClipper = new JreClipper();

    /**
     * jink处理器
     */
    protected final JLinkHandler jLinkHandler = new JLinkHandler();

    /**
     * packr打包处理
     */
    protected final PackrPackager packrPackager = new PackrPackager();

    /**
     * jpackage打包处理
     */
    protected final JPackageHandler jPackageHandler = new JPackageHandler();

    /**
     * 全局配置
     */
    @Getter
    @Setter
    private GlobalConfig globalConfig;

    /**
     * 平台配置
     */
    @Getter
    @Setter
    private PlatformConfig platformConfig;

    // /**
    //  * jar裁剪配置
    //  */
    // @Getter
    // @Setter
    // private JarClipConfig jarClipConfig;
    //
    // /**
    //  * jar裁剪配置
    //  */
    // @Getter
    // @Setter
    // private JreClipConfig jreClipConfig;
    //
    // /**
    //  * jink配置
    //  */
    // @Getter
    // @Setter
    // private JLinkConfig jLinkConfig;

    /*
     * 裁剪的jar
     */
    protected String clapJar;

    /**
     * jre裁剪目录
     */
    protected String jreClipDir;

    /*
     * jlink目录
     */
    protected String jreJLinkDir;

    /*
     * jpackage输入目录
     */
    protected String jPackageInputDir;

    // /**
    //  * 是否删除裁剪的jar
    //  */
    // private boolean deleteClapJar = true;
    //
    // /**
    //  * 是否删除打包目录
    //  */
    // private boolean clearPackDir = true;

    // /**
    //  * 是否删除裁剪的jre目录
    //  */
    // @Getter
    // @Setter
    // private boolean clearJreClipDir = true;
    //
    // /**
    //  * 是否删除jlink目录
    //  */
    // @Getter
    // @Setter
    // private boolean clearJreJLinkDir = true;

    // /**
    //  * 打包配置
    //  */
    // @Getter
    // @Setter
    // protected PackageConfig packageConfig;

    /**
     * 打包前处理
     *
     * @throws Exception 异常
     */
    protected void packBefore() throws Exception {
        if (!this.platformConfig.isEnable()) {
            log.warn("platform is disable, skip pack.");
            return;
        }
        if (this.jLinkConfig() != null && this.jLinkConfig().isEnable()) {
            log.info("jlink start------------------------------------------------>");
            ThreadUtil.sleep(1500);
            this.jLinkHandler.exec(this.jLinkConfig());
            this.packageConfig().setJrePath(this.jLinkConfig().getOutput());
            this.jreJLinkDir = this.jLinkConfig().getOutput();
            log.info("jlink finish------------------------------------------------>");
        } else {
            log.warn("jLinkConfig is null or jLinkConfig.enable is false, skip jlink.");
        }
        if (this.jreClipConfig() != null && this.jreClipConfig().isEnable()) {
            log.info("jreClip start------------------------------------------------>");
            ThreadUtil.sleep(1500);
            this.jreClipper.clip(this.jreClipConfig());
            this.packageConfig().setJrePath(this.jreClipConfig().getDest());
            this.jreClipDir = this.jreClipConfig().getDest();
            log.info("jreClip finish------------------------------------------------>");
        } else {
            log.warn("jreClipConfig is null or jreClipConfig.enable is false, skip jre clip.");
        }
        if (this.jarClipConfig() != null && this.jarClipConfig().isEnable()) {
            log.info("jarClip start------------------------------------------------>");
            ThreadUtil.sleep(1500);
            this.jarClipper.clip(this.jarClipConfig());
            this.packageConfig().setJarPath(this.jarClipConfig().getDest());
            this.clapJar = this.jarClipConfig().getDest();
            log.info("jarClip finish------------------------------------------------>");
        } else {
            log.warn("jarClipConfig is null or jarClipConfig.enable is false, skip jar clip.");
        }
    }

    protected JarClipConfig jarClipConfig() {
        return this.platformConfig == null ? null : this.platformConfig.getJarClipConfig();
    }

    protected JreClipConfig jreClipConfig() {
        return this.platformConfig == null ? null : this.platformConfig.getJreClipConfig();
    }

    protected PackageConfig packageConfig() {
        return this.platformConfig == null ? null : this.platformConfig.getPackageConfig();
    }

    protected JLinkConfig jLinkConfig() {
        return this.platformConfig == null ? null : this.platformConfig.getJLinkConfig();
    }

    /**
     * 是否保留过程中目录
     *
     * @return 结果
     */
    public boolean isRetainDuringDir() {
        return this.globalConfig != null && this.globalConfig.isRetainDuringDir();
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
        if (!this.platformConfig.isEnable()) {
            log.warn("platform is disable, skip pack.");
            return;
        }
        log.info("pack after start.");
        if (StrUtil.isNotBlank(this.packageConfig().getCompressType())) {
            switch (this.packageConfig().getCompressType().toLowerCase()) {
                case "zip" ->
                        PkgUtil.zipDest(this.getCompressName(), this.packageConfig().getAppDest(), this.getPlatform() == PackrConfig.Platform.MacOS);
                case "tar" -> PkgUtil.tarDest(this.getCompressName(), this.packageConfig().getAppDest());
                case "tar.gz" -> PkgUtil.gzipDest(this.getCompressName(), this.packageConfig().getAppDest());
            }
        }
        // 删除中间目录
        if (!this.isRetainDuringDir()) {
            // 删除裁剪的jar
            if (this.clapJar != null) {
                FileUtil.del(this.clapJar);
                log.info("delete ClapJar:{}.", this.clapJar);
            }
            // 删除打包目录
            if (this.packageConfig().getAppDest() != null) {
                FileUtil.del(this.packageConfig().getAppDest());
                log.info("delete AppDest:{}.", this.packageConfig().getAppDest());
            }
            // 删除jre裁剪目录
            if (this.jreClipDir != null) {
                FileUtil.del(this.jreClipDir);
                log.info("delete JreClipDir:{}.", this.jreClipDir);
            }
            // 删除jlink目录
            if (this.jreJLinkDir != null) {
                FileUtil.del(this.jreJLinkDir);
                log.info("delete JreJLinkDir:{}.", this.jreJLinkDir);
            }
            // 删除jlink目录
            if (this.jPackageInputDir != null) {
                FileUtil.del(this.jPackageInputDir);
                log.info("delete jPackageInputDir:{}.", this.jPackageInputDir);
            }
        }
        log.info("pack after finish.");
    }

    // /**
    //  * 压缩打包目录，zip格式
    //  */
    // protected void zipDest() {
    //     String compressName = this.getCompressName() + ".zip";
    //     log.info("zipDest start, config.compressType is:{} compressName:{}.", this.packageConfig().getCompressType(), compressName);
    //     File compressFile = new File(this.packageConfig().getDestFile(), compressName);
    //     // File compressFile = new File(this.packageConfig().getDestFile().getParentFile(), compressName);
    //     // 进行zip压缩，如果是macos则保留目录名称，否则不保留
    //     ZipUtil.zip(this.packageConfig().getAppDest(), compressFile.getPath(), this.getPlatform() == PackrConfig.Platform.MacOS);
    //     log.info("zipDest finish.");
    // }
    //
    // /**
    //  * 压缩打包目录，tar格式
    //  */
    // protected void tarDest() {
    //     String compressName = this.getCompressName() + ".tar";
    //     log.info("tarDest start, config.compressType is:{} compressName:{}.", this.packageConfig().getCompressType(), compressName);
    //     File dest = this.packageConfig().getDestFile();
    //     File compressFile = new File(this.packageConfig().getDestFile().getParentFile(), compressName);
    //     // 进行tar压缩
    //     Archiver archiver = CompressUtil.createArchiver(StandardCharsets.UTF_8, ArchiveStreamFactory.TAR, compressFile)
    //             .add(dest);
    //     archiver.finish().close();
    //     log.info("tarDest finish.");
    // }
    //
    // /**
    //  * 压缩打包文件，tar.gz格式
    //  */
    // protected void gzipDest() {
    //     String compressName = this.getCompressName() + ".tar.gz";
    //     log.info("gzipDest start, config.compressType is:{} compressName:{}.", this.packageConfig().getCompressType(), compressName);
    //     File dest = this.packageConfig().getDestFile();
    //     File compressFile = new File(this.packageConfig().getDestFile().getParentFile(), compressName);
    //     // 进行tar.gz压缩
    //     Archiver archiver = CompressUtil.createArchiver(StandardCharsets.UTF_8, "tar.gz", compressFile)
    //             .add(dest);
    //     archiver.finish().close();
    //     log.info("gzipDest finish.");
    // }

    /**
     * 获取压缩文件名称
     *
     * @return 压缩文件名称
     */
    protected String getCompressName() {
        String name = "";
        if (this.packageConfig().getAppName() != null) {
            name += this.packageConfig().getAppName();
        }
        if (this.packageConfig().getVersion() != null) {
            name = name + "_" + this.packageConfig().getVersion();
        }
        if (this.packageConfig().getPlatform() != null) {
            name = name + "_" + this.packageConfig().getPlatform();
        }
        name = name + "_release";
        return name;
    }

    // /**
    //  * 设置jlink参数
    //  *
    //  * @param jLinkConfig jlink参数
    //  */
    // public void setJLinkConfig(@NonNull JLinkConfig jLinkConfig) {
    //     if (this.jLinkHandler == null && jLinkConfig.isEnable()) {
    //         this.jLinkHandler = new JLinkHandler();
    //     }
    //     this.jLinkConfig = jLinkConfig;
    // }
    //
    // /**
    //  * 设置jar裁剪参数
    //  *
    //  * @param jarClipConfig jar裁剪参数
    //  */
    // public void setJarClipConfig(@NonNull JarClipConfig jarClipConfig) {
    //     if (this.jarClipper == null && jarClipConfig.isEnable()) {
    //         this.jarClipper = new JarClipper();
    //     }
    //     this.jarClipConfig = jarClipConfig;
    // }
    //
    // /**
    //  * 设置jre裁剪参数
    //  *
    //  * @param jreClipConfig jre裁剪参数
    //  */
    // public void setJreClipConfig(JreClipConfig jreClipConfig) {
    //     if (this.jreClipper == null && jreClipConfig.isEnable()) {
    //         this.jreClipper = new JreClipper();
    //     }
    //     this.jreClipConfig = jreClipConfig;
    // }

    /**
     * 使用JPackage打包
     *
     * @throws Exception 异常
     */
    protected void packByJPackage() throws Exception {
        if (!this.packageConfig().isEnable() || !this.platformConfig.isEnable()) {
            log.warn("package or platform is disable, skip pack.");
            return;
        }
        log.info("pack with JPackage================================");
        // copy主jar到输出目录
        File mainJar = PkgUtil.copyJarToJpackageInputDir(this.packageConfig().getDestPath(), this.packageConfig().getPlatform(), this.packageConfig().getJarFile());
        // jPackage输出目录
        this.jPackageInputDir = mainJar.getParent();
        // 初始化参数
        JPackageConfig config = JPackageConfig.from(this.packageConfig());
        config.setMainJar(mainJar.getName());
        config.setInput(this.jPackageInputDir);
        // 删除app的目标目录
        FileUtil.del(this.packageConfig().getAppDest());
        // 执行打包
        this.jPackageHandler.exec(config);
    }

    /**
     * 使用Packr打包
     *
     * @throws Exception 异常
     */
    protected void packByPackr() throws Exception {
        if (!this.packageConfig().isEnable() || !this.platformConfig.isEnable()) {
            log.warn("package or platform is disable, skip pack.");
            return;
        }
        log.info("pack with Packr================================");
        // 删除app的目标目录
        FileUtil.del(this.packageConfig().getAppDest());
        PackrConfigExt config = PackrConfigExt.form(this.packageConfig());
        config.platform = this.getPlatform();
        this.packrPackager.pack(config);
    }

    /**
     * 获取平台
     *
     * @return 平台
     */
    protected abstract PackrConfig.Platform getPlatform();

}
