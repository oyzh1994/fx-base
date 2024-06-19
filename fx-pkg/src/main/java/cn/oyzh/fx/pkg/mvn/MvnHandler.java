package cn.oyzh.fx.pkg.mvn;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.RuntimeUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.SingleHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;


/**
 * @author oyzh
 * @since 2024/6/19
 */
public class MvnHandler implements PreHandler, SingleHandler {

    /**
     * 项目工程
     */
    private final String projectDir;

    /**
     * 依赖工程
     */
    private final List<String> dependencies;

    @Getter
    @Setter
    private boolean executed;

    public MvnHandler(String projectDir, List<String> dependencies) {
        this.dependencies = dependencies;
        this.projectDir = projectDir;
    }

    @Override
    public int order() {
        return PackOrder.HIGH_P2;
    }

    @Override
    public String name() {
        return "maven处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        if (this.executed) {
            return;
        }
        String mvnHome = System.getenv("MAVEN_HOME");
        if (StrUtil.isBlank(mvnHome)) {
            mvnHome = System.getenv("M2_HOME");
        }
        String mvnExe = mvnHome + "/bin/mvn.cmd";
        // 安装依赖工程
        if (CollUtil.isNotEmpty(this.dependencies)) {
            for (String dependency : this.dependencies) {
                String mvnCommand = mvnExe + " -X install -DskipTests";
                RuntimeUtil.execAndWait(mvnCommand, new File(dependency));
            }
        }
        // 打包项目工程
        String mvnCommand = mvnExe + " -X package -DskipTests";
        RuntimeUtil.execAndWait(mvnCommand, new File(this.projectDir));
        this.executed = true;
    }

}
