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

    /*
     * 裁剪的jar
     */
    protected String clapJar;

    /*
     * app文件
     */
    protected File destFile;

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

    /**
     * 获取jar裁剪配置
     *
     * @return jar裁剪配置
     */
    protected JarClipConfig jarClipConfig() {
        return this.platformConfig == null ? null : this.platformConfig.getJarClipConfig();
    }

    /**
     * 获取jre裁剪配置
     *
     * @return jre裁剪配置
     */
    protected JreClipConfig jreClipConfig() {
        return this.platformConfig == null ? null : this.platformConfig.getJreClipConfig();
    }

    /**
     * 获取打包配置
     *
     * @return 打包配置
     */
    protected PackageConfig packageConfig() {
        return this.platformConfig == null ? null : this.platformConfig.getPackageConfig();
    }

    /**
     * 获取jlink配置
     *
     * @return jlink配置
     */
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
            log.info("jlink start, platform:{}------------------------------------------------>", this.getPlatform());
            ThreadUtil.sleep(1500);
            // 删除旧的jlink目录
            FileUtil.del(this.jLinkConfig().getOutput());
            this.jLinkHandler.exec(this.jLinkConfig(), this.platformConfig.getJdkPath());
            this.packageConfig().setJrePath(this.jLinkConfig().getOutput());
            this.jreJLinkDir = this.jLinkConfig().getOutput();
            log.info("jlink finish jreJLinkDir:{}------------------------------------------------>", this.jreJLinkDir);
        } else {
            log.warn("jLinkConfig is null or jLinkConfig.enable is false, skip jlink.");
        }
        if (this.jreClipConfig() != null && this.jreClipConfig().isEnable()) {
            log.info("jreClip start, platform:{}------------------------------------------------>", this.getPlatform());
            ThreadUtil.sleep(1500);
            this.jreClipper.clip(this.jreClipConfig());
            this.packageConfig().setJrePath(this.jreClipConfig().getDest());
            this.jreClipDir = this.jreClipConfig().getDest();
            log.info("jreClip finish jreClipDir:{}------------------------------------------------>", this.jreClipDir);
        } else {
            log.warn("jreClipConfig is null or jreClipConfig.enable is false, skip jre clip.");
        }
        if (this.jarClipConfig() != null && this.jarClipConfig().isEnable()) {
            log.info("jarClip start, platform:{}------------------------------------------------>", this.getPlatform());
            ThreadUtil.sleep(1500);
            this.jarClipper.clip(this.jarClipConfig(), this.platformConfig.getJdkPath());
            this.packageConfig().setJarPath(this.jarClipConfig().getDest());
            this.clapJar = this.jarClipConfig().getDest();
            log.info("jarClip finish clapJar:{}------------------------------------------------>", this.clapJar);
        } else {
            log.warn("jarClipConfig is null or jarClipConfig.enable is false, skip jar clip.");
        }
    }

    /**
     * 打包后处理
     */
    protected void packAfter() {
        if (!this.platformConfig.isEnable()) {
            log.warn("platform is disable, skip pack.");
            return;
        }
        log.info("pack after start.");
        if (StrUtil.isNotBlank(this.packageConfig().getCompressType()) && this.packageConfig().isEnable()) {
            this.destFile = switch (this.packageConfig().getCompressType().toLowerCase()) {
                case "zip" -> {
                    if (this.getPlatform() == PackrConfig.Platform.MacOS) {
                        yield PkgUtil.zipDestByMacos(this.getCompressName(), this.packageConfig().getAppDest());
                    } else {
                        yield PkgUtil.zipDest(this.getCompressName(), this.packageConfig().getAppDest());
                    }
                }
                case "tar" -> PkgUtil.tarDest(this.getCompressName(), this.packageConfig().getAppDest());
                case "tar.gz" -> PkgUtil.gzipDest(this.getCompressName(), this.packageConfig().getAppDest());
                default ->
                        throw new IllegalStateException("Unexpected value: " + this.packageConfig().getCompressType().toLowerCase());
            };
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
        log.info("pack after finish, dest:{}", this.destFile);
    }

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
        log.info("pack with JPackage, platform:{}================================", this.getPlatform());
        // copy主jar到输出目录
        File mainJar = PkgUtil.copyJarToJpackageInputDir(this.packageConfig().getDestPath(), this.platformConfig.getPlatform(), this.packageConfig().getJarFile());
        // jPackage输出目录
        this.jPackageInputDir = mainJar.getParent();
        // 初始化参数
        JPackageConfig config = JPackageConfig.from(this.packageConfig());
        config.setMainJar(mainJar.getName());
        config.setInput(this.jPackageInputDir);
        // 删除旧的jpackage目录
        FileUtil.del(this.packageConfig().getAppDest());
        // 执行打包
        this.jPackageHandler.exec(config, this.platformConfig.getJdkPath());
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
        log.info("pack with Packr, platform:{}================================", this.getPlatform());
        // 删除旧的打包目录
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
