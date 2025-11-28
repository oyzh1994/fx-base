package cn.oyzh.fx.pkg.pack;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
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

    private int order = PackOrder.ORDER_M8;

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
        // 压缩包
        File compressFile = packConfig.getCompressFile();
        if (compressFile != null && compressFile.exists()) {
            JulLog.info("最终产物是压缩文件，忽略处理");
        } else {// 正常构建
            List<File> files = FileUtil.getAllFiles(packConfig.getDest());
            for (File file : files) {
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
                FileUtil.renameFile(file, new File(file.getParent(), fileName), true);
                JulLog.info("最终产物名称:{} 处理后名称:{}", file.getName(), fileName);
            }

        }
    }
}
