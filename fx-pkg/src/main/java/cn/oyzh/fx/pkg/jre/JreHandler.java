package cn.oyzh.fx.pkg.jre;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.pkg.ConfigParser;
import cn.oyzh.fx.pkg.ExtConfig;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.clip.filter.FileFilter;
import cn.oyzh.fx.pkg.packr.PackrConfig;

import java.io.File;
import java.util.List;

/**
 * jre裁剪工具
 *
 * @author oyzh
 * @since 2023/03/09
 */
public class JreHandler implements PreHandler, ConfigParser<JreConfig> {

    private FileFilter fileFilter;

    @Override
    public JreConfig parse(String configFile) {
        JreConfig config = JSONUtil.toBean(FileUtil.readUtf8String(configFile), JreConfig.class);
        this.fileFilter = new FileFilter();
        this.fileFilter.addExcludes(config.getExcludes());
        return config;
    }

    @Override
    public void handle(PackrConfig packrConfig, ExtConfig extConfig) {
        StaticLog.info("clip start.");
        long start = System.currentTimeMillis();
        String src = packrConfig.jdk;
        if (StrUtil.isBlank(src)) {
            src = extConfig.getJlinkJre();
        }
        File dest = FileUtil.mkdir(new File(FileUtil.getTmpDirPath(), "minimizeJre_" + UUID.randomUUID().toString(true)));
        FileUtil.copyContent(new File(src), dest, false);
        List<File> fileList = FileUtil.loopFiles(dest);
        for (File file : fileList) {
            if (!fileFilter.accept(file.getName())) {
                file.delete();
            }
        }
        long end = System.currentTimeMillis();
        StaticLog.info("clip end, used time: {}ms.", end - start);
    }
}
