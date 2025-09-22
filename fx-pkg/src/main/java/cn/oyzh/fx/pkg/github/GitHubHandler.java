package cn.oyzh.fx.pkg.github;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.pkg.PackCost;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.config.PackConfig;

import java.io.File;
import java.util.List;

/**
 * github处理器
 *
 * @author oyzh
 * @since 2025/09/22
 */
public class GitHubHandler implements PostHandler {

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
        return "github处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        String githubDist = (String) packConfig.getProperty(PackCost.GITHUB_DIST);
        if (githubDist != null) {
            JulLog.info("githubDist {}", githubDist);
            if (!FileUtil.exist(githubDist)) {
                JulLog.warn("githubDist 目录不存在，创建目录");
                FileUtil.mkdir(githubDist);
            }
            List<File> files = FileUtil.getAllFiles(packConfig.getDest());
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }
                File file1 = new File(githubDist, file.getName());
                boolean success = FileUtil.move(file, file1, true);
                if (success) {
                    JulLog.info("file:{} 移动到dist目录成功", file.getPath());
                } else {
                    JulLog.warn("file:{} 移动到dist目录失败", file.getPath());
                }
            }
        } else {
            JulLog.warn("githubDist 未找到参数设置");
        }
    }
}
