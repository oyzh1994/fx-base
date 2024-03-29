package cn.oyzh.fx.common.dto;

import lombok.Data;
import lombok.Getter;

import java.util.Properties;

/**
 * 项目信息
 *
 * @author oyzh
 * @since 2020/9/14
 */
@Getter
public class Project {

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    /**
     * 版本号
     */
    private String version;

    /**
     * 更新日期
     */
    private String updateDate;

    /**
     * copyright
     */
    private String copyright;

    public Project() {
        try {
            Properties properties = new Properties();
            properties.load(Project.class.getResourceAsStream("/project.properties"));
            this.name = properties.getProperty("project.name");
            this.type = properties.getProperty("project.type");
            this.version = properties.getProperty("project.version");
            this.copyright = properties.getProperty("project.copyright");
            this.updateDate = properties.getProperty("project.updateDate");
            properties.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
