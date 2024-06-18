package cn.oyzh.fx.pkg.config;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;

import java.net.URL;
import java.util.Properties;

/**
 * 项目信息处理器
 *
 * @author oyzh
 * @since 2024/6/18
 */
public class ProjectHandler implements PreHandler {

    private String projectFile = "project.properties";

    public ProjectHandler() {

    }

    public ProjectHandler(String projectFile) {
        this.projectFile = projectFile;
    }

    @Override
    public int order() {
        return PackOrder.HIGH_P4;
    }

    @Override
    public String name() {
        return "项目信息处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        URL url = ResourceUtil.getResource(this.projectFile);
        if (url == null) {
            return;
        }
        Properties properties = new Properties();
        properties.load(url.openStream());
        if (properties.containsKey("project.name")) {
            packConfig.setAppName(properties.getProperty("project.name"));
        }
        if (properties.containsKey("project.version")) {
            packConfig.setAppVersion(properties.getProperty("project.version"));
        }
        if (properties.containsKey("project.type")) {
            packConfig.setBuildType(properties.getProperty("project.type"));
        }
    }
}
