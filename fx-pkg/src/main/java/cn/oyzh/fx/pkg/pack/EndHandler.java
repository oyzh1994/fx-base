package cn.oyzh.fx.pkg.pack;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.config.PackConfig;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class EndHandler implements PostHandler {

    private int order = PackOrder.ORDER_MIN;

    public int order() {
        return order;
    }

    public void order(int order) {
        this.order = order;
    }

    @Override
    public boolean unique() {
        return true;
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        // 删除打包目录
        if (packConfig.getCompressFile() != null) {
            FileUtil.del(packConfig.getDest());
        }
        // 删除jlink的jre目录
        if (packConfig.getJlinkJre() != null) {
            FileUtil.del(packConfig.getJlinkJre());
        }
        // 删除裁剪的jre目录
        if (packConfig.getMinimizeJre() != null) {
            FileUtil.del(packConfig.getMinimizeJre());
        }
        // 删除裁剪的主程序
        if (packConfig.getMinimizeManJar() != null) {
            FileUtil.del(packConfig.getMinimizeManJar());
        }
        // 删除解压的jar目录
        if (packConfig.getJarUnDir() != null) {
            FileUtil.del(packConfig.getJarUnDir());
        }
        // 删除临时的jpackage目录
        if (packConfig.getJpackageInput() != null) {
            FileUtil.del(packConfig.getJpackageInput());
        }
        long startTime = (Long) packConfig.getProperty("startTime");
        long endTime = System.currentTimeMillis();
        long cost = endTime - startTime;
        if (packConfig.getCompressFile() != null) {
            JulLog.info("打包执行结束, 总耗时:{}毫秒, 最终文件:{} 大小:{}Mb", cost, packConfig.outPath(), packConfig.getCompressFile().length() / 1024.0 / 1024);
        } else {
            JulLog.info("打包执行结束, 总耗时:{}毫秒, 最终文件目录:{}", cost, packConfig.outPath());
        }
    }

    @Override
    public String name() {
        return "最终处理";
    }
}
