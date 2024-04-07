package cn.oyzh.fx.plus.changelog;

import lombok.Data;

import java.util.List;

/**
 * 更新日志
 *
 * @author oyzh
 * @since 2024/4/7
 */
@Data
public class Changelog {

    /**
     * 日期
     */
    private String date;

    /**
     * 版本
     */
    private String version;

    /**
     * 问题处理
     */
    private List<String> bugfix;

    /**
     * 优化
     */
    private List<String> optimize;

    /**
     * 新功能
     */
    private List<String> features;
}
