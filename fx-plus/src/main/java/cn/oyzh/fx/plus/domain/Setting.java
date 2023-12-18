package cn.oyzh.fx.plus.domain;

import lombok.Data;

/**
 * zk设置
 *
 * @author oyzh
 * @since 2022/8/26
 */
@Data
public class Setting {

    /**
     * 主题
     */
    private String theme;

    /**
     * 应用退出
     * 0 到系统托盘
     * 1 每次询问
     * 2 直接关闭程序
     */
    private Integer exitMode;

    /**
     * 记住页面大小
     * 0|null 不记住
     * 1 记住
     */
    private Integer pageInfo;

    /**
     * 记住页面拉伸
     * 0 不记住
     * 1|null 记住
     */
    private Integer rememberPageResize;

    /**
     * 记住页面位置
     * 0|null 不记住
     * 1 记住
     */
    private Integer rememberPageLocation;

    /**
     * 标签策略
     * null或者ALL_CONNECT 代表所有连接
     * SINGLE_CONNECT 代表单个连接
     */
    private String tabStrategy;

    /**
     * 标签数量限制
     * 0代表无限，null或者1代表1个
     */
    private Integer tabLimit;

    /**
     * 标签策略是否限制全部连接
     *
     * @return 结果
     */
    public boolean isAllTabLimitStrategy() {
        return this.tabStrategy == null || "ALL_CONNECT".equals(this.tabStrategy);
    }

    /**
     * 标签策略是否限制单个连接
     *
     * @return 结果
     */
    public boolean isSingleTabLimitStrategy() {
        return "SINGLE_CONNECT".equals(this.tabStrategy);
    }

    /**
     * 获取标签数量限制
     *
     * @return 标签数量限制
     */
    public int getTabLimit() {
        return this.tabLimit == null ? 1 : this.tabLimit;
    }

    /**
     * 是否标签数量无限制
     *
     * @return 标签数量限制
     */
    public boolean isTabUnLimit() {
        return this.tabLimit != null && this.tabLimit == 0;
    }

    /**
     * 是否退出到系统托盘
     *
     * @return 结果
     */
    public boolean isExitTray() {
        return this.exitMode == null || this.exitMode == 0;
    }

    /**
     * 退出是否询问
     *
     * @return 结果
     */
    public boolean isExitAsk() {
        return this.exitMode != null && this.exitMode == 1;
    }

    /**
     * 是否直接退出
     *
     * @return 结果
     */
    public boolean isExitDirectly() {
        return this.exitMode != null && this.exitMode == 2;
    }

    /**
     * 是否记住页面大小
     *
     * @return 结果
     */
    public boolean isRememberPageSize() {
        return this.pageInfo != null && this.pageInfo == 1;
    }

    /**
     * 是否记住页面拉伸
     *
     * @return 结果
     */
    public boolean isRememberPageResize() {
        return this.rememberPageResize == null || this.rememberPageResize == 1;
    }

    /**
     * 是否记住页面位置
     *
     * @return 结果
     */
    public boolean isRememberPageLocation() {
        return this.rememberPageLocation != null && this.rememberPageLocation == 1;
    }
}
