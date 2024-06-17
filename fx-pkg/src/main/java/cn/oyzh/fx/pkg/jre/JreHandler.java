package cn.oyzh.fx.pkg.jre;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.pkg.ConfigParser;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.filter.FileFilter;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;
import cn.oyzh.fx.pkg.filter.RegexpFilter;

import java.io.File;
import java.util.List;

/**
 * jre裁剪工具
 *
 * @author oyzh
 * @since 2023/03/09
 */
public class JreHandler implements PreHandler, ConfigParser<JreConfig> {

    // private FileFilter fileFilter;
    private RegexpFilter filter;

    @Override
    public JreConfig parse(String configFile) {
        JreConfig config = JSONUtil.toBean(FileUtil.readUtf8String(configFile), JreConfig.class);
        this.filter = new RegexpFilter(config.getExcludes());
        return config;
    }

    @Override
    public void handle(ExtPackrConfig packrConfig) {
        String src = packrConfig.getJlinkJre();
        if (StrUtil.isBlank(src)) {
            src = packrConfig.jdk;
        }
        File dest = FileUtil.mkdir(new File(FileUtil.getTmpDirPath(), "minimizeJre_" + UUID.randomUUID().toString(true)));
        FileUtil.copyContent(new File(src), dest, false);
        List<File> fileList = FileUtil.loopFiles(dest);
        for (File file : fileList) {
            if (!this.filter.apply(file.getName())) {
                file.delete();
            }
        }
        packrConfig.setMinimizeJre(dest.getPath());
    }

    @Override
    public int order() {
        return PackOrder.HIGH - 1;
    }

    @Override
    public String name() {
        return "jre处理";
    }
}
