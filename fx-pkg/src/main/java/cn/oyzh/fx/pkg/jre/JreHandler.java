package cn.oyzh.fx.pkg.jre;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.pkg.ConfigParser;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;
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

    /**
     * 过滤器
     */
    private final RegFilter filter;

    public JreHandler(String configFile) {
        JreConfig config = JreConfigParser.parseConfig(configFile);
        this.filter = new RegFilter(config.getExcludes());
    }

    @Override
    public void handle(ExtPackrConfig packrConfig) {
        String src = packrConfig.getJlinkJre();
        if (StrUtil.isBlank(src)) {
            src = packrConfig.jdk;
        }
        File dest = new File(FileUtil.getTmpDirPath(), "_minimize_jre_" + UUID.randomUUID().toString(true));
        FileUtil.copyContent(new File(src), dest, false);
        List<File> fileList = FileUtil.loopFiles(dest);
        for (File file : fileList) {
            if (!this.filter.apply(file.getName())) {
                FileUtil.del(file);
                StaticLog.info("文件:{}被过滤.", file.getName());
            }
        }
        // 设置最小化后的jre
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
