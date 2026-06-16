package cn.oyzh.fx.plus.util;

import cn.oyzh.common.cache.CacheUtil;
import cn.oyzh.common.cache.TimedCache;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.fx.gui.svg.glyph.file.File3gpSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.File7zSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.a.FileAacSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.a.FileActionScriptSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.a.FileAmrSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.a.FileApkSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.a.FileAsciidoctorSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.a.FileAsmSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.a.FileAspSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.b.FileBatSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.b.FileBibtexSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.b.FileBinSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.b.FileBmpSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.b.FileBz2SVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileCSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileCerSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileCfgSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileChmSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileClassSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileClojureSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileCmdSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileCoffeeScriptSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileComSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileCompressSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileConfSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileCppSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileCsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileCssSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.c.FileCudaSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.d.FileDartSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.d.FileDbSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.d.FileDiffSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.d.FileDllSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.d.FileDmgSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.d.FileDockerfileSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.d.FileDotSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.d.FileDsstoreSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.d.FileDylibSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.e.FileEpubSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.e.FileErlangSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.e.FileExcelSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.e.FileExeSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.f.FileFlacSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.f.FileFlvSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.f.FileFsharpSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.g.FileGifSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.g.FileGitIgnoreSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.g.FileGitRebaseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.g.FileGoSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.g.FileGradleSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.g.FileGroovySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.g.FileGzSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.h.FileHandlebarsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.h.FileHlslSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.h.FileHtmlSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.i.FileIcnsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.i.FileIcoSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.i.FileImageSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.i.FileInfSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.i.FileIniSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.i.FileIsoSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.j.FileJarSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.j.FileJavaSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.j.FileJpgSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.j.FileJsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.j.FileJsonSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.j.FileJspSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.j.FileJuliaSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.k.FileKconfigSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.k.FileKmkSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.k.FileKtSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.l.FileLatexSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.l.FileLessSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.l.FileLuaSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.m.FileM4aSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.m.FileMakefileSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.m.FileMarkdownSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.m.FileMkvSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.m.FileMovSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.m.FileMp3SVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.m.FileMp4SVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.o.FileObjectiveCPPSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.o.FileObjectiveCSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.o.FileOcxSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.o.FileOggSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePcmSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePdfSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePerlSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePhpSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePlistSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePowershellSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePptSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePropertiesSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FileProtobufSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePsdSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePugSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.p.FilePycSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.r.FileRSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.r.FileRarSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.r.FileRazorSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.r.FileRestructuredtextSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.r.FileRmSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.r.FileRmvbSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.r.FileRpmSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.r.FileRssSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.r.FileRtfSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.r.FileRubySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.r.FileRustSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileSasSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileScalaSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileScssSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileSearchResultSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileShSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileShaderlabSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileSoSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileSqlSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileSrtSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileSvgSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileSwfSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.s.FileSwiftSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.t.FileTarSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.t.FileTerminalSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.t.FileTexSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.t.FileTextSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.t.FileTomlSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.t.FileTsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.t.FileTsxSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.t.FileTtfSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.t.FileTwigSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.u.FileUnknownSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.v.FileVbSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.v.FileVbsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.v.FileVimSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.w.FileWarSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.w.FileWavSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.w.FileWbmpSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.w.FileWebmSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.w.FileWebpSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.w.FileWmaSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.w.FileWordSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.w.FileWpsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.x.FileXlsSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.x.FileXmlSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.x.FileXslSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.x.FileXzSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.y.FileYamlSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.y.FileYmlSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.z.FileZipSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图标工具类
 *
 * @author oyzh
 * @since 2023/4/4
 */
public class IconUtil {

    /**
     * 图标缓存
     */
    private static final TimedCache<String, WeakReference<byte[]>> ICON_CACHE = CacheUtil.newTimedCache(60 * 1000L);

    /**
     * 获取图标
     *
     * @param iconUrls 图标列表地址
     * @return 图标列表
     */
    public static List<Image> getIcons(String[] iconUrls) {
        return getIcons(Arrays.asList(iconUrls));
    }

    /**
     * 获取图标
     *
     * @param iconUrls 图标列表地址
     * @return 图标列表
     */
    public static List<Image> getIcons(List<String> iconUrls) {
        List<Image> icons = new ArrayList<>(iconUrls.size());
        for (String iconUrl : iconUrls) {
            Image icon = getIcon(iconUrl);
            if (icon != null) {
                icons.add(icon);
            }
        }
        return icons;
    }

    /**
     * 获取图标
     *
     * @param iconUrl 图标地址
     * @return 图标
     */
    public static Image getIcon(String iconUrl) {
        InputStream stream;
        WeakReference<byte[]> reference = ICON_CACHE.get(iconUrl);
        if (reference == null || reference.get() == null) {
            stream = ResourceUtil.getResourceAsStream(iconUrl);
            if (stream != null) {
                byte[] bytes = IOUtil.readBytes(stream);
                ICON_CACHE.put(iconUrl, new WeakReference<>(bytes));
                stream = IOUtil.toStream(bytes);
            }
            if (JulLog.isInfoEnabled()) {
                JulLog.info("load icon form file.");
            }
        } else {
            stream = IOUtil.toStream(reference.get());
            if (JulLog.isInfoEnabled()) {
                JulLog.info("load icon form cache.");
            }
        }
        if (stream == null) {
            return null;
        }
        return new Image(stream, 0, 0, true, true);
    }

    /**
     * 图标转为fx图片
     *
     * @param icon 图标
     * @return 结果
     */
    public static Image iconToFxImage(Icon icon) {
        if (icon == null) {
            return null;
        }

        // 将 Icon 绘制到 BufferedImage 上
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        icon.paintIcon(null, g2d, 0, 0);
        g2d.dispose();

        // 将 BufferedImage 转换为 JavaFX Image
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        int[] pixels = new int[width * height];
        bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);
        pixelWriter.setPixels(0, 0, width, height,
                javafx.scene.image.PixelFormat.getIntArgbInstance(), pixels, 0, width);

        return writableImage;
    }

    /**
     * 系统图标缓存
     */
    private static Map<String, Image> SYSTEM_ICON_CACHE;

    /**
     * 根据扩展名，获取系统图标
     *
     * @param extName 扩展名
     * @return 结果
     */
    public static Image getSystemIcon(String extName) {
        // 无扩展名时的通用图标
        if (extName.isEmpty()) {
            extName = "file";
        }
        if (SYSTEM_ICON_CACHE == null) {
            SYSTEM_ICON_CACHE = new ConcurrentHashMap<>();
        }
        // 从缓存获取
        return SYSTEM_ICON_CACHE.computeIfAbsent(extName, key -> {
            File tempFile = null;
            try {
                // 创建临时文件，只为了获取图标
                tempFile = new File(FileUtil.tmpPath(), UUIDUtil.uuidSimple() + "." + key);
                // 空文件即可
                FileUtil.touch(tempFile);
                // 使用 FileSystemView 获取系统图标
                FileSystemView fsv = FileSystemView.getFileSystemView();
                // 获取图标
                Icon icon = fsv.getSystemIcon(tempFile);
                // 转换为javafx图标
                return iconToFxImage(icon);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                // 清理临时文件
                FileUtil.del(tempFile);
            }
            return null;
        });
    }

    /**
     * 根据扩展名，获取svg图标
     *
     * @param extName 扩展名
     * @return 结果
     */
    public static SVGGlyph getSVGIcon(String extName) {
        SVGGlyph glyph;
        if (StringUtil.isEmpty(extName)) {
            glyph = new FileUnknownSVGGlyph("12");
        } else if (FileNameUtil.isAspType(extName)) {
            glyph = new FileAspSVGGlyph("12");
        } else if (FileNameUtil.isCmdType(extName)) {
            glyph = new FileCmdSVGGlyph("12");
        } else if (FileNameUtil.isRmType(extName)) {
            glyph = new FileRmSVGGlyph("12");
        } else if (FileNameUtil.isMkvType(extName)) {
            glyph = new FileMkvSVGGlyph("12");
        } else if (FileNameUtil.isDotType(extName)) {
            glyph = new FileDotSVGGlyph("12");
        } else if (FileNameUtil.isRtfType(extName)) {
            glyph = new FileRtfSVGGlyph("12");
        } else if (FileNameUtil.isWavType(extName)) {
            glyph = new FileWavSVGGlyph("12");
        } else if (FileNameUtil.isMovType(extName)) {
            glyph = new FileMovSVGGlyph("12");
        } else if (FileNameUtil.isSoType(extName)) {
            glyph = new FileSoSVGGlyph("12");
        } else if (FileNameUtil.isDllType(extName)) {
            glyph = new FileDllSVGGlyph("12");
        } else if (FileNameUtil.isDylibType(extName)) {
            glyph = new FileDylibSVGGlyph("12");
        } else if (FileNameUtil.isIsoType(extName)) {
            glyph = new FileIsoSVGGlyph("12");
        } else if (FileNameUtil.isRssType(extName)) {
            glyph = new FileRssSVGGlyph("12");
        } else if (FileNameUtil.isSrtType(extName)) {
            glyph = new FileSrtSVGGlyph("12");
        } else if (FileNameUtil.isExcelType(extName)) {
            glyph = new FileExcelSVGGlyph("12");
        } else if (FileNameUtil.isXlsType(extName)
                || FileNameUtil.isXlsxType(extName)) {
            glyph = new FileXlsSVGGlyph("12");
        } else if (FileNameUtil.isWordType(extName)) {
            glyph = new FileWordSVGGlyph("12");
        } else if (FileNameUtil.isPptType(extName)
                || FileNameUtil.isPptxType(extName)) {
            glyph = new FilePptSVGGlyph("12");
        } else if (FileNameUtil.isPdfType(extName)) {
            glyph = new FilePdfSVGGlyph("12");
        } else if (FileNameUtil.isPerlType(extName)) {
            glyph = new FilePerlSVGGlyph("12");
        } else if (FileNameUtil.isActionScriptType(extName)) {
            glyph = new FileActionScriptSVGGlyph("12");
        } else if (FileNameUtil.isDartType(extName)) {
            glyph = new FileDartSVGGlyph("12");
        } else if (FileNameUtil.isPhpType(extName)) {
            glyph = new FilePhpSVGGlyph("12");
        } else if (FileNameUtil.isRustType(extName)) {
            glyph = new FileRustSVGGlyph("12");
        } else if (FileNameUtil.isRubyType(extName)) {
            glyph = new FileRubySVGGlyph("12");
        } else if (FileNameUtil.isScalaType(extName)) {
            glyph = new FileScalaSVGGlyph("12");
        } else if (FileNameUtil.isJpgType(extName)) {
            glyph = new FileJpgSVGGlyph("12");
        } else if (FileNameUtil.isExeType(extName)) {
            glyph = new FileExeSVGGlyph("12");
        } else if (FileNameUtil.isMp3Type(extName)) {
            glyph = new FileMp3SVGGlyph("12");
        } else if (FileNameUtil.isMp4Type(extName)) {
            glyph = new FileMp4SVGGlyph("12");
        } else if (FileNameUtil.isDmgType(extName)) {
            glyph = new FileDmgSVGGlyph("12");
        } else if (FileNameUtil.isRarType(extName)) {
            glyph = new FileRarSVGGlyph("12");
        } else if (FileNameUtil.is7zType(extName)) {
            glyph = new File7zSVGGlyph("12");
        } else if (FileNameUtil.isGzType(extName)) {
            glyph = new FileGzSVGGlyph("12");
        } else if (FileNameUtil.isXzType(extName)) {
            glyph = new FileXzSVGGlyph("12");
        } else if (FileNameUtil.isBz2Type(extName)) {
            glyph = new FileBz2SVGGlyph("12");
        } else if (FileNameUtil.isZipType(extName)) {
            glyph = new FileZipSVGGlyph("12");
        } else if (FileNameUtil.isTsType(extName)) {
            glyph = new FileTsSVGGlyph("12");
        } else if (FileNameUtil.isApkType(extName)) {
            glyph = new FileApkSVGGlyph("12");
        } else if (FileNameUtil.is3gpType(extName)) {
            glyph = new File3gpSVGGlyph("12");
        } else if (FileNameUtil.isAmrType(extName)) {
            glyph = new FileAmrSVGGlyph("12");
        } else if (FileNameUtil.isRpmType(extName)) {
            glyph = new FileRpmSVGGlyph("12");
        } else if (FileNameUtil.isWpsType(extName)) {
            glyph = new FileWpsSVGGlyph("12");
        } else if (FileNameUtil.isWebpType(extName)) {
            glyph = new FileWebpSVGGlyph("12");
        } else if (FileNameUtil.isWbmpType(extName)) {
            glyph = new FileWbmpSVGGlyph("12");
        } else if (FileNameUtil.isSqlType(extName)) {
            glyph = new FileSqlSVGGlyph("12");
        } else if (FileNameUtil.isBmpType(extName)) {
            glyph = new FileBmpSVGGlyph("12");
        } else if (FileNameUtil.isJarType(extName)) {
            glyph = new FileJarSVGGlyph("12");
        } else if (FileNameUtil.isSwfType(extName)) {
            glyph = new FileSwfSVGGlyph("12");
        } else if (FileNameUtil.isTarType(extName)) {
            glyph = new FileTarSVGGlyph("12");
        } else if (FileNameUtil.isTomlType(extName)) {
            glyph = new FileTomlSVGGlyph("12");
        } else if (FileNameUtil.isRmvbType(extName)) {
            glyph = new FileRmvbSVGGlyph("12");
        } else if (FileNameUtil.isGifType(extName)) {
            glyph = new FileGifSVGGlyph("12");
        } else if (FileNameUtil.isFlvType(extName)) {
            glyph = new FileFlvSVGGlyph("12");
        } else if (FileNameUtil.isWebmType(extName)) {
            glyph = new FileWebmSVGGlyph("12");
        } else if (FileNameUtil.isFlacType(extName)) {
            glyph = new FileFlacSVGGlyph("12");
        } else if (FileNameUtil.isAacType(extName)) {
            glyph = new FileAacSVGGlyph("12");
        } else if (FileNameUtil.isM4aType(extName)) {
            glyph = new FileM4aSVGGlyph("12");
        } else if (FileNameUtil.isOggType(extName)) {
            glyph = new FileOggSVGGlyph("12");
        } else if (FileNameUtil.isPcmType(extName)) {
            glyph = new FilePcmSVGGlyph("12");
        } else if (FileNameUtil.isWmaType(extName)) {
            glyph = new FileWmaSVGGlyph("12");
        } else if (FileNameUtil.isGzType(extName)) {
            glyph = new FileRmvbSVGGlyph("12");
        } else if (FileNameUtil.isHtmType(extName)
                || FileNameUtil.isHtmlType(extName)) {
            glyph = new FileHtmlSVGGlyph("12");
        } else if (FileNameUtil.isXmlType(extName)) {
            glyph = new FileXmlSVGGlyph("12");
        } else if (FileNameUtil.isJsType(extName)) {
            glyph = new FileJsSVGGlyph("12");
        } else if (FileNameUtil.isJspType(extName)) {
            glyph = new FileJspSVGGlyph("12");
        } else if (FileNameUtil.isJsonType(extName)
                || FileNameUtil.isJsoncType(extName)
                || FileNameUtil.isJsonlType(extName)) {
            glyph = new FileJsonSVGGlyph("12");
        } else if (FileNameUtil.isMarkdownType(extName)) {
            glyph = new FileMarkdownSVGGlyph("12");
        } else if (FileNameUtil.isCssType(extName)) {
            glyph = new FileCssSVGGlyph("12");
        } else if (FileNameUtil.isConfType(extName)) {
            glyph = new FileConfSVGGlyph("12");
        } else if (FileNameUtil.isBatType(extName)) {
            glyph = new FileBatSVGGlyph("12");
        } else if (FileNameUtil.isYamlType(extName)) {
            glyph = new FileYamlSVGGlyph("12");
        } else if (FileNameUtil.isIniType(extName)) {
            glyph = new FileIniSVGGlyph("12");
        } else if (FileNameUtil.isIcoType(extName)) {
            glyph = new FileIcoSVGGlyph("12");
        } else if (FileNameUtil.isTtfType(extName)) {
            glyph = new FileTtfSVGGlyph("12");
        } else if (FileNameUtil.isShType(extName)) {
            glyph = new FileShSVGGlyph("12");
        } else if (FileNameUtil.isPyType(extName)) {
            glyph = new FilePySVGGlyph("12");
        } else if (FileNameUtil.isJavaType(extName)) {
            glyph = new FileJavaSVGGlyph("12");
        } else if (FileNameUtil.isPlistType(extName)) {
            glyph = new FilePlistSVGGlyph("12");
        } else if (FileNameUtil.isVbType(extName)) {
            glyph = new FileVbSVGGlyph("12");
        } else if (FileNameUtil.isVbsType(extName)) {
            glyph = new FileVbsSVGGlyph("12");
        } else if (FileNameUtil.isWarType(extName)) {
            glyph = new FileWarSVGGlyph("12");
        } else if (FileNameUtil.isTsxType(extName)) {
            glyph = new FileTsxSVGGlyph("12");
        } else if (FileNameUtil.isCsvType(extName)) {
            glyph = new FileCSVGGlyph("12");
        } else if (FileNameUtil.isCType(extName)) {
            glyph = new FileCSVGGlyph("12");
        } else if (FileNameUtil.isYmlType(extName)) {
            glyph = new FileYmlSVGGlyph("12");
        } else if (FileNameUtil.isBinType(extName)) {
            glyph = new FileBinSVGGlyph("12");
        } else if (FileNameUtil.isPsdType(extName)) {
            glyph = new FilePsdSVGGlyph("12");
        } else if (FileNameUtil.isTxtType(extName)
                || FileNameUtil.isTextType(extName)
                || FileNameUtil.isLogType(extName)) {
            glyph = new FileTextSVGGlyph("12");
        } else if (FileNameUtil.isImageType(extName)) {
            glyph = new FileImageSVGGlyph("12");
        } else if (FileNameUtil.isCompressType(extName)) {
            glyph = new FileCompressSVGGlyph("12");
        } else if (FileNameUtil.isTerminalType(extName)) {
            glyph = new FileTerminalSVGGlyph("12");
        } else if (FileNameUtil.isLuaType(extName)) {
            glyph = new FileLuaSVGGlyph("12");
        } else if (FileNameUtil.isKtType(extName)) {
            glyph = new FileKtSVGGlyph("12");
        } else if (FileNameUtil.isAsmType(extName)) {
            glyph = new FileAsmSVGGlyph("12");
        } else if (FileNameUtil.isLessType(extName)) {
            glyph = new FileLessSVGGlyph("12");
        } else if (FileNameUtil.isProtobufType(extName)) {
            glyph = new FileProtobufSVGGlyph("12");
        } else if (FileNameUtil.isPropertiesType(extName)) {
            glyph = new FilePropertiesSVGGlyph("12");
        } else if (FileNameUtil.isKmkType(extName)) {
            glyph = new FileKmkSVGGlyph("12");
        } else if (FileNameUtil.isIcnsType(extName)) {
            glyph = new FileIcnsSVGGlyph("12");
        } else if (FileNameUtil.isCsType(extName)) {
            glyph = new FileCsSVGGlyph("12");
        } else if (FileNameUtil.isSvgType(extName)) {
            glyph = new FileSvgSVGGlyph("12");
        } else if (FileNameUtil.isCerType(extName)) {
            glyph = new FileCerSVGGlyph("12");
        } else if (FileNameUtil.isCfgType(extName)) {
            glyph = new FileCfgSVGGlyph("12");
        } else if (FileNameUtil.isChmType(extName)) {
            glyph = new FileChmSVGGlyph("12");
        } else if (FileNameUtil.isClassType(extName)) {
            glyph = new FileClassSVGGlyph("12");
        } else if (FileNameUtil.isComType(extName)) {
            glyph = new FileComSVGGlyph("12");
        } else if (FileNameUtil.isConfigType(extName)) {
            glyph = new FileConfSVGGlyph("12");
        } else if (FileNameUtil.isCppType(extName)) {
            glyph = new FileCppSVGGlyph("12");
        } else if (FileNameUtil.isDbType(extName)) {
            glyph = new FileDbSVGGlyph("12");
        } else if (FileNameUtil.isEpubType(extName)) {
            glyph = new FileEpubSVGGlyph("12");
        } else if (FileNameUtil.isGradleType(extName)) {
            glyph = new FileGradleSVGGlyph("12");
        } else if (FileNameUtil.isInfType(extName)) {
            glyph = new FileInfSVGGlyph("12");
        } else if (FileNameUtil.isOcxType(extName)) {
            glyph = new FileOcxSVGGlyph("12");
        } else if (FileNameUtil.isPycType(extName)) {
            glyph = new FilePycSVGGlyph("12");
        } else if (FileNameUtil.isDsstoreType(extName)) {
            glyph = new FileDsstoreSVGGlyph("12");
        } else if (FileNameUtil.isClojureType(extName)) {
            glyph = new FileClojureSVGGlyph("12");
        } else if (FileNameUtil.isCoffeeScriptType(extName)) {
            glyph = new FileCoffeeScriptSVGGlyph("12");
        } else if (FileNameUtil.isCudaType(extName)) {
            glyph = new FileCudaSVGGlyph("12");
        } else if (FileNameUtil.isDiffType(extName)) {
            glyph = new FileDiffSVGGlyph("12");
        } else if (FileNameUtil.isDockerfileType(extName)) {
            glyph = new FileDockerfileSVGGlyph("12");
        } else if (FileNameUtil.isErlangType(extName)) {
            glyph = new FileErlangSVGGlyph("12");
        } else if (FileNameUtil.isFsharpType(extName)) {
            glyph = new FileFsharpSVGGlyph("12");
        } else if (FileNameUtil.isGoType(extName)) {
            glyph = new FileGoSVGGlyph("12");
        } else if (FileNameUtil.isGroovyType(extName)) {
            glyph = new FileGroovySVGGlyph("12");
        } else if (FileNameUtil.isHandlebarsType(extName)) {
            glyph = new FileHandlebarsSVGGlyph("12");
        } else if (FileNameUtil.isHlslType(extName)) {
            glyph = new FileHlslSVGGlyph("12");
        } else if (FileNameUtil.isJuliaType(extName)) {
            glyph = new FileJuliaSVGGlyph("12");
        } else if (FileNameUtil.isKconfigType(extName)) {
            glyph = new FileKconfigSVGGlyph("12");
        } else if (FileNameUtil.isLatexType(extName)) {
            glyph = new FileLatexSVGGlyph("12");
        } else if (FileNameUtil.isMakefileType(extName)) {
            glyph = new FileMakefileSVGGlyph("12");
        } else if (FileNameUtil.isObjectiveCType(extName)) {
            glyph = new FileObjectiveCSVGGlyph("12");
        } else if (FileNameUtil.isObjectiveCPPType(extName)) {
            glyph = new FileObjectiveCPPSVGGlyph("12");
        } else if (FileNameUtil.isPowershellType(extName)) {
            glyph = new FilePowershellSVGGlyph("12");
        } else if (FileNameUtil.isPugType(extName)) {
            glyph = new FilePugSVGGlyph("12");
        } else if (FileNameUtil.isRType(extName)) {
            glyph = new FileRSVGGlyph("12");
        } else if (FileNameUtil.isRazorType(extName)) {
            glyph = new FileRazorSVGGlyph("12");
        } else if (FileNameUtil.isRestructuredtextType(extName)) {
            glyph = new FileRestructuredtextSVGGlyph("12");
        } else if (FileNameUtil.isScssType(extName)) {
            glyph = new FileScssSVGGlyph("12");
        } else if (FileNameUtil.isShaderlabType(extName)) {
            glyph = new FileShaderlabSVGGlyph("12");
        } else if (FileNameUtil.isSwiftType(extName)) {
            glyph = new FileSwiftSVGGlyph("12");
        } else if (FileNameUtil.isTexType(extName)) {
            glyph = new FileTexSVGGlyph("12");
        } else if (FileNameUtil.isTwigType(extName)) {
            glyph = new FileTwigSVGGlyph("12");
        } else if (FileNameUtil.isVimType(extName)) {
            glyph = new FileVimSVGGlyph("12");
        } else if (FileNameUtil.isXslType(extName)) {
            glyph = new FileXslSVGGlyph("12");
        } else if (FileNameUtil.isSearchResultType(extName)) {
            glyph = new FileSearchResultSVGGlyph("12");
        } else if (FileNameUtil.isAsciidocType(extName)) {
            glyph = new FileAsciidoctorSVGGlyph("12");
        } else if (FileNameUtil.isBibtexType(extName)) {
            glyph = new FileBibtexSVGGlyph("12");
        } else if (FileNameUtil.isGitRebaseType(extName)) {
            glyph = new FileGitRebaseSVGGlyph("12");
        } else if (FileNameUtil.isSasType(extName)) {
            glyph = new FileSasSVGGlyph("12");
        } else if (FileNameUtil.isGitIgnoreType(extName)) {
            glyph = new FileGitIgnoreSVGGlyph("12");
        } else {
            glyph = new FileSVGGlyph("12");
        }
        return glyph;
    }
}
