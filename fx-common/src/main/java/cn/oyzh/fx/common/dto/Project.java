package cn.oyzh.fx.common.dto;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.PropertiesFile;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

/**
 * 项目信息
 *
 * @author oyzh
 * @since 2020/9/14
 */
@Data
//@Lazy
//@Component
//@PropertySource(value = "/project.properties", encoding = "utf-8")
public class Project {

    /**
     * 名称
     */
//    @Value("${project.name:}")
    private String name;

    /**
     * 类型
     */
//    @Value("${project.type:build}")
    private String type;

    /**
     * 版本号
     */
//    @Value("${project.version:}")
    private String version;

    /**
     * 更新日期
     */
//    @Value("${project.updateDate:}")
    private String updateDate;

    /**
     * copyright
     */
//    @Value("${project.copyright:}")
    private String copyright;

    private static Project instance;

    public static Project load() {
        if (instance == null) {
            try {
                PropertiesFile propFile = new PropertiesFile("/project.properties");
                Project project = new Project();
                String name = propFile.getProperty("project.name");
                if (StrUtil.isNotBlank(name)) {
                    project.setName(name);
                }
                String type = propFile.getProperty("project.type");
                if (StrUtil.isNotBlank(type)) {
                    project.setType(type);
                }
                String version = propFile.getProperty("project.version");
                if (StrUtil.isNotBlank(version)) {
                    project.setVersion(version);
                }
                String updateDate = propFile.getProperty("project.updateDate");
                if (StrUtil.isNotBlank(updateDate)) {
                    project.setUpdateDate(updateDate);
                }
                String copyright = propFile.getProperty("project.copyright");
                if (StrUtil.isNotBlank(copyright)) {
                    project.setCopyright(copyright);
                }
                propFile.clear();
                 instance = project;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return instance;
    }
}
