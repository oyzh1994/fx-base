package cn.oyzh.fx.pkg.pack;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.config.PackConfig;

import java.io.File;
import java.util.List;

/**
 *
 * @author oyzh
 * @since 2025-11-28
 */
public class DestHandler implements PostHandler {

    private int order = PackOrder.ORDER_M7;

    @Override
    public int order() {
        return order;
    }

    @Override
    public void order(int order) {
        this.order = order;
    }

    @Override
    public String name() {
        return "最终产物处理器";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        // app-image格式仅处理压缩包
        if (StringUtil.equalsIgnoreCase(packConfig.packageType(), "app-image")) {
            File compressFile = packConfig.getCompressFile();
            if (compressFile != null && compressFile.exists()) {
                File targetFile = this.handler(packConfig, compressFile);
                packConfig.setCompressFile(targetFile);
            }
        } else {// msi、exe、dmg、pkg、rpm、deb
            List<File> files = FileUtil.getAllFiles(packConfig.getDest());
            for (File file : files) {
                if (file.isDirectory() || !file.exists()) {
                    continue;
                }
                String extName = FileNameUtil.extName(file.getName());
                if (!StringUtil.equalsAnyIgnoreCase(extName, "msi", "exe", "pkg", "dmg", "rpm", "deb")) {
                    continue;
                }
                this.handler(packConfig, file);
                break;
            }
        }
        //// 压缩包
        // File compressFile = packConfig.getCompressFile();
        // if (compressFile != null && compressFile.exists()) {
        //    String extName = FileNameUtil.extName(compressFile);
        //    if (StringUtil.equals(extName, "appimage")) {
        //        this.handler(packConfig, compressFile);
        //    } else {
        //        JulLog.warn("最终产物是压缩文件，忽略处理");
        //    }
        //} else {// 正常构建
        //    List<File> files = FileUtil.getAllFiles(packConfig.getDest());
        //    for (File file : files) {
        //        if (file.isDirectory() || !file.exists()) {
        //            continue;
        //        }
        //        this.handler(packConfig, file);
        //        break;
        //    }
        //}
    }

    /**
     * 执行处理
     *
     * @param packConfig 打包配置
     * @param file       文件
     * @throws Exception 异常
     * @return 重命名后的文件
     */
    private File handler(PackConfig packConfig, File file) throws Exception {
        String fileName = "";
        if (packConfig.getAppName() != null) {
            fileName += packConfig.getAppName();
        }
        if (packConfig.appVersion() != null) {
            fileName = fileName + "_v" + packConfig.appVersion();
        }
        if (packConfig.getPlatform() != null) {
            fileName = fileName + "_" + packConfig.getPlatform();
        }
        fileName = fileName + "_" + OSUtil.getArchName();
        if (packConfig.getBuildType() != null) {
            fileName = fileName + "_" + packConfig.getBuildType();
        }
        String extName = FileNameUtil.extName(file.getName());
        fileName = fileName + "." + extName;
        File finalFile = new File(file.getParent(), fileName);
        FileUtil.renameFile(file, finalFile, true);
        // 以防万一
        FileUtil.del(file);
        JulLog.info("最终产物名称:{} 处理后名称:{}", file.getName(), fileName);
        return finalFile;
    }
}
