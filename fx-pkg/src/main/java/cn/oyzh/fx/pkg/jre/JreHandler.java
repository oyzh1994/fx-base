package cn.oyzh.fx.pkg.jre;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.SingleHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.filter.RegFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * jre处理
 *
 * @author oyzh
 * @since 2024/06/17
 */
public class JreHandler implements PreHandler, SingleHandler {

    private int order = PackOrder.ORDER_P4;

    public int order() {
        return order;
    }

    public void order(int order) {
        this.order = order;
    }

    private boolean executed;

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        if (this.executed) {
            return;
        }
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
        if (StringUtil.isBlank(src)) {
            throw new Exception("jre为空！");
        }
        RegFilter filter = new RegFilter(jreConfig.getExcludes());
        File dest = new File(FileUtil.getTmpDirPath(), "_minimize_jre_" + UUID.randomUUID().toString(true));
        FileUtil.copyContent(new File(src), dest, false);
        List<File> fileList = FileUtil.loopFiles(dest);
        List<Runnable> tasks = new ArrayList<>();
        for (File file : fileList) {
            // 异步执行
            tasks.add(() -> {
                if (!filter.apply(file.getName())) {
                    FileUtil.del(file);
                    JulLog.info("文件:{}被过滤.", file.getName());
                }
            });
        }
        // 执行业务
        ThreadUtil.submit(tasks);
        // 设置最小化后的jre
        packConfig.setMinimizeJre(dest.getPath());
        this.executed = true;
    }

    @Override
    public String name() {
        return "jre处理";
    }
}
