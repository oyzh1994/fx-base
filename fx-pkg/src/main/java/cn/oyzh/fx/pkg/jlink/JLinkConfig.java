package cn.oyzh.fx.pkg.jlink;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.common.util.CollectionUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * jlink配置
 *
 * @author oyzh
 * @since 2024/06/17
 */
@Data
public class JLinkConfig {

    /**
     * vm类型
     */
    private String vm = "server";

    /**
     * 输出目录
     */
    private String output;

    /**
     * 压缩等级
     */
    private Integer compress = 2;

    /**
     * 打印过程日志
     */
    private boolean verbose = true;

    /**
     * 不无需主页面
     */
    private boolean noManPages = true;

    /**
     * 无需头文件
     */
    private boolean noHeaderFiles = true;

    /**
     * 去除debug文件
     */
    private boolean stripDebug = true;

    /**
     * 去除debug属性
     */
    private boolean stripJavaDebugAttributes = true;

    /**
     * 添加的模块
     */
    private List<String> addModules;

    /**
     * 排除文件
     */
    private List<String> excludeFiles;

    public void margeAddModules(Collection<String> addModules) {
        if (CollectionUtil.isNotEmpty(addModules)) {
            if (this.addModules == null || this.addModules.isEmpty()) {
                this.addModules = new ArrayList<>(addModules);
            } else {
                for (String addModule : addModules) {
                    if (!this.addModules.contains(addModule)) {
                        this.addModules.add(addModule);
                    }
                }
            }
        }
    }
}
