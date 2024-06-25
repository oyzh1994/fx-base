package cn.oyzh.fx.common.dto;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 项目信息
 *
 * @author oyzh
 * @since 2020/9/14
 */
@Data
@Lazy
@Component
@PropertySource(value = "/project.properties", encoding = "utf-8")
public class Project {

    /**
     * 名称
     */
    @Value("${project.name:}")
    private String name;

    /**
     * 类型
     */
    @Value("${project.type:build}")
    private String type;

    /**
     * 版本号
     */
    @Value("${project.version:}")
    private String version;

    /**
     * 更新日期
     */
    @Value("${project.updateDate:}")
    private String updateDate;

    /**
     * copyright
     */
    @Value("${project.copyright:}")
    private String copyright;

}
