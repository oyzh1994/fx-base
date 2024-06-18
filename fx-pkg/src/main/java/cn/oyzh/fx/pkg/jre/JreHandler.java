package cn.oyzh.fx.pkg.jre;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.filter.RegFilter;

import java.io.File;
import java.util.List;

/**
 * jre处理
 *
 * @author oyzh
 * @since 2024/06/17
 */
public class JreHandler implements PreHandler {

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        JreConfig jreConfig = packConfig.getJreConfig();
        if (jreConfig == null) {
            return;
        }
        String src;
        if (packConfig.getJlinkJre() != null) {
            src = packConfig.getJlinkJre();
        } else {
            src = packConfig.jrePath();
        }
        if (StrUtil.isBlank(src)) {
            throw new Exception("jre为空！");
        }
        RegFilter filter = new RegFilter(jreConfig.getExcludes());
        File dest = new File(FileUtil.getTmpDirPath(), "_minimize_jre_" + UUID.randomUUID().toString(true));
        FileUtil.copyContent(new File(src), dest, false);
        List<File> fileList = FileUtil.loopFiles(dest);
        for (File file : fileList) {
            if (!filter.apply(file.getName())) {
                FileUtil.del(file);
                StaticLog.info("文件:{}被过滤.", file.getName());
            }
        }
        // 设置最小化后的jre
        packConfig.setMinimizeJre(dest.getPath());
    }

    @Override
    public int order() {
        return PackOrder.HIGH_P1;
    }

    @Override
    public String name() {
        return "jre处理";
    }
}
