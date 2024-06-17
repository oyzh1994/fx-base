package cn.oyzh.fx.pkg.pack;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class EndHandler implements PostHandler {

    @Override
    public boolean unique() {
        return true;
    }

    @Override
    public void handle(ExtPackrConfig packrConfig) throws Exception {
        // 删除打包目录
        if (packrConfig.getCompressFile() != null) {
            FileUtil.del(packrConfig.outDir);
        }
        // 删除jlink的jre目录
        if (packrConfig.getJlinkJre() != null) {
            FileUtil.del(packrConfig.getJlinkJre());
        }
        // 删除裁剪的jre目录
        if (packrConfig.getMinimizeJre() != null) {
            FileUtil.del(packrConfig.getMinimizeJre());
        }
        // 删除裁剪的主程序
        if (packrConfig.getMinimizeManJar() != null) {
            FileUtil.del(packrConfig.getMinimizeManJar());
        }
        // 删除解压的jar目录
        if (packrConfig.getJarUnDir() != null) {
            FileUtil.del(packrConfig.getJarUnDir());
        }
        long startTime = (Long) packrConfig.getProperty("startTime");
        long endTime = System.currentTimeMillis();
        long cost = endTime - startTime;
        if (packrConfig.getCompressFile() != null) {
            StaticLog.info("打包执行结束, 总耗时:{}毫秒, 最终文件:{} 大小:{}Mb", cost,packrConfig.outPath(), packrConfig.getCompressFile().length() / 1024.0 / 1024);
        } else {
            StaticLog.info("打包执行结束, 总耗时:{}毫秒, 最终文件目录:{}Mb", cost, packrConfig.outPath());

        }
    }

    @Override
    public int order() {
        return PackOrder.LOW_MAX;
    }

    @Override
    public String name() {
        return "最终处理";
    }
}
